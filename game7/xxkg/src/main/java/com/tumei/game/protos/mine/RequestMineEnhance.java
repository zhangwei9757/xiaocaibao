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
public class RequestMineEnhance extends BaseProtocol {
	public int seq;
	// 矿位置
	public int pos;
	// 矿等级
	public int level;
	// 延长模式
	public int mode;

	public static class Return extends BaseProtocol {
		public int seq;
		public int gem;

		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameServer.getInstance().getMineSystem().enhance((GameUser)session, this);
	}
}
