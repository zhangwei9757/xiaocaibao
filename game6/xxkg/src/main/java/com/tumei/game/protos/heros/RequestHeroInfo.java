package com.tumei.game.protos.heros;

import com.tumei.game.GameUser;
import com.tumei.websocket.SessionUser;
import com.tumei.model.HerosBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求充值信息
 */
@Component
public class RequestHeroInfo extends BaseProtocol {
	public int seq;

	class ReturnHeroInfo extends BaseProtocol {
		public int seq;

		/**
		 * 英雄基础信息
		 */
		public HerosBean heros;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnHeroInfo rci = new ReturnHeroInfo();
		rci.seq = seq;

		long uid = user.getUid();
		rci.heros = user.getDao().findHeros(uid);

		user.send(rci);
	}
}
