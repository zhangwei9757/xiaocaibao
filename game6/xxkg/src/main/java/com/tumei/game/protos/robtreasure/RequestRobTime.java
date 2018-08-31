package com.tumei.game.protos.robtreasure;

import com.tumei.game.GameUser;
import com.tumei.game.services.RobService;
import com.tumei.model.RobBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestRobTime extends BaseProtocol {
	public int seq;

	class ReturnRobTime extends BaseProtocol {
		public int seq;
		public long time;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnRobTime rci = new ReturnRobTime();
		rci.seq = seq;

		RobBean rb = RobService.getInstance().updateFrags(user.getUid());
		if (rb != null) {
			rci.time = rb.getTime().getTime() / 1000;
		}

		user.send(rci);
	}
}
