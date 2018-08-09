package com.tumei.game.protos.activity;

import com.google.common.base.Strings;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.game.protos.notifys.NotifyRedPoint;
import com.tumei.model.FriendsBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 * 请求添加好友
 *
 */
@Component
public class RequestAddFriend extends BaseProtocol {
    public int seq;
	public long id;

    class ReturnAddFriend extends BaseProtocol {
		public int seq;
		public String result = "";
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnAddFriend rl = new ReturnAddFriend();
		rl.seq = seq;

		FriendsBean fsb = user.getDao().findFriends(user.getUid());
		fsb.flush();
		rl.result = fsb.applyFriend(id, user.getPower());
		if (Strings.isNullOrEmpty(rl.result)) {
			NotifyRedPoint nrp = new NotifyRedPoint();
			nrp.infos.put(100, 1);
			GameServer.getInstance().send(id, nrp);
		}

        user.send(rl);
    }
}
