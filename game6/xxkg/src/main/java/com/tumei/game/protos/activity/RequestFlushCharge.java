package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 * 请求刷新充值，对充值有疑问的可以强制刷新，目前是登录刷新，并不合适
 */
@Component
public class RequestFlushCharge extends BaseProtocol {
	public int seq;

	class Return extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;
		Return rl = new Return();
		rl.seq = seq;
		user.flushCharge();

		user.send(rl);
	}
}
