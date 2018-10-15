package com.tumei.game.protos.mine;

import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
@Component
public class RequestMineLeave extends BaseProtocol {
	public int seq;

	public static class Return extends BaseProtocol {
		public int seq;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameServer.getInstance().getMineSystem().leave((GameUser)session, this);
	}

}
