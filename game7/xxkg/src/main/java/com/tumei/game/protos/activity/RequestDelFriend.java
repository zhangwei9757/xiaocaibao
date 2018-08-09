package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.FriendsBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 * 请求删除好友
 *
 */
@Component
public class RequestDelFriend extends BaseProtocol {
    public int seq;
	public long id;

    class ReturnDelFriend extends BaseProtocol {
		public int seq;
		public String result = "";
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnDelFriend rl = new ReturnDelFriend();
		rl.seq = seq;

		FriendsBean fsb = user.getDao().findFriends(user.getUid());
		fsb.removeFriend(id, true);

        user.send(rl);
    }
}
