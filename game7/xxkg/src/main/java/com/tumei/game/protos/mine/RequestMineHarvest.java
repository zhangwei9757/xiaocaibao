package com.tumei.game.protos.mine;

import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
@Component
public class RequestMineHarvest extends BaseProtocol {
	public int seq;

	public static class Return extends BaseProtocol {
		public int seq;

		public String result = "";

		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameServer.getInstance().getMineSystem().harvest((GameUser)session, this);
	}
}
