package com.tumei.game.protos.activity;

import com.google.common.base.Strings;
import com.tumei.common.DaoService;
import com.tumei.game.GameUser;
import com.tumei.model.FriendsBean;
import com.tumei.model.beans.FriendBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 * 请求好友信息
 *
 */
@Component
public class RequestSearchFriend extends BaseProtocol {
    public int seq;
	// id > 0 优先查询id, id <= 0 name不为空则查询name,否则返回错误
	public long id;
	public String name = "";

    class ReturnSearchFriend extends BaseProtocol {
		public int seq;
		public String result = "";
		public FriendBean friend;
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnSearchFriend rl = new ReturnSearchFriend();
		rl.seq = seq;

		if (!user.judegeProtocolInterval(this, 5)) {
			rl.result = "不能频繁搜索!";
			user.send(rl);
			return;
		}

		if (id <= 0) {
			if (Strings.isNullOrEmpty(name)) {
				rl.result = "查询条件为空";
				user.send(rl);
				return;
			}
			// 根据昵称查询id;
			id = DaoService.getInstance().findByName(name);
		}

		if (id <= 0) {
			rl.result = "不存在的角色";
			user.send(rl);
			return;
		}

		String n = DaoService.getInstance().findById(id);
		if (n != null) {

			FriendBean fb = new FriendBean(id, 0);
			rl.friend = fb;

			FriendsBean fsb = user.getDao().findFriends(user.getUid());
			fsb.flush();
			fb.mode = fsb.isFriendStatus(id);
		} else {
			rl.result = "无法查询到指定的玩家";
		}

        user.send(rl);
    }
}
