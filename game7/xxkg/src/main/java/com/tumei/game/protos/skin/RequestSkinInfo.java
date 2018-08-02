package com.tumei.game.protos.skin;

import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.SkinStruct;
import com.tumei.model.HerosBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求充值信息
 */
@Component
public class RequestSkinInfo extends BaseProtocol {
	public int seq;

	class ReturnSkinInfo extends BaseProtocol {
		public int seq;

		public HashMap<Integer, SkinStruct> skins;

		public int skin;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnSkinInfo rci = new ReturnSkinInfo();
		rci.seq = seq;

		long uid = user.getUid();
		HerosBean hsb = user.getDao().findHeros(uid);

		rci.skins = hsb.getSkins();
		rci.skin = hsb.getSkin();

		user.send(rci);
	}
}
