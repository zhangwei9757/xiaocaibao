package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.game.protos.notifys.NotifyRedPoint;
import com.tumei.model.FriendsBean;
import com.tumei.model.PackBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 * 请求添加好友
 *
 */
@Component
public class RequestFriendRecv extends BaseProtocol {
    public int seq;
	public long id;

    class ReturnFriendRecv extends BaseProtocol {
		public int seq;
		public String result = "";
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnFriendRecv rl = new ReturnFriendRecv();
		rl.seq = seq;

		FriendsBean fsb = user.getDao().findFriends(user.getUid());
		fsb.flush();
		if (!fsb.getAward(id)) {
			rl.result = "今日领取已达上限";
		} else {
			PackBean pb = user.getDao().findPack(user.getUid());
			pb.flushSpirit(2);
		}

        user.send(rl);
    }
}
