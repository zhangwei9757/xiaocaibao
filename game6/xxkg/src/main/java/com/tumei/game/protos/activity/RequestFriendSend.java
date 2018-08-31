package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.game.protos.notifys.NotifyRedPoint;
import com.tumei.model.FriendsBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 * 请求添加好友
 *
 */
@Component
public class RequestFriendSend extends BaseProtocol {
    public int seq;
	public long id;

    class ReturnFriendSend extends BaseProtocol {
		public int seq;
		public String result = "";
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnFriendSend rl = new ReturnFriendSend();
		rl.seq = seq;

		FriendsBean fsb = user.getDao().findFriends(user.getUid());
		fsb.flush();
		if (!fsb.send(id)) {
			rl.result = "赠送活力次数达到上限";
		} else {
			NotifyRedPoint nrp = new NotifyRedPoint();
			nrp.infos.put(101, 1);
			user.getServer().send(id, nrp);

			// 日常任务
			user.pushDailyTask(4, 1);
		}

        user.send(rl);
    }
}
