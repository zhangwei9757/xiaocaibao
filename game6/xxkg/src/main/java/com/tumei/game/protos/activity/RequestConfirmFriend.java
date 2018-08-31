package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.FriendsBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 * 请求好友转正
 *
 */
@Component
public class RequestConfirmFriend extends BaseProtocol {
    public int seq;
	public long id;
	/**
	 * 0:答应
	 * 1:拒绝
	 */
	public int flag;

    class ReturnConfirmFriend extends BaseProtocol {
		public int seq;
		public String result = "";
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnConfirmFriend rl = new ReturnConfirmFriend();
		rl.seq = seq;

		FriendsBean fsb = user.getDao().findFriends(user.getUid());
		rl.result = fsb.confirmFriend(id, user.getPower(), flag);

        user.send(rl);
    }
}
