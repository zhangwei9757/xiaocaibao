package com.tumei.game.protos.activity;

import com.tumei.game.services.FriendService;
import com.tumei.game.GameUser;
import com.tumei.model.FriendsBean;
import com.tumei.model.beans.FriendBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by leon on 2016/12/31.
 * 请求好友信息
 *
 */
@Component
public class RequestFriendsInfo extends BaseProtocol {
    public int seq;

    class ReturnFriendsInfo extends BaseProtocol {
		public int seq;
		public List<FriendBean> friends = new ArrayList<>();
		public List<FriendBean> ready = new ArrayList<>();
		// FriendBean里的Mode = 2表示今日已经申请过
		public List<FriendBean> recommands = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnFriendsInfo rl = new ReturnFriendsInfo();
		rl.seq = seq;

		FriendsBean fsb = user.getDao().findFriends(user.getUid());
		fsb.flush();
		fsb.copyFriends(rl.friends);
		fsb.copyReady(rl.ready);

		long uid = user.getUid();

		rl.recommands = FriendService.getInstance().randomFriends(10, uid);
		for (FriendBean fb : rl.recommands) {
			fb.mode = fsb.isFriendStatus(fb.id);
		}

        user.send(rl);
    }
}
