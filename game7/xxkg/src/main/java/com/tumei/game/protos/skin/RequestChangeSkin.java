package com.tumei.game.protos.skin;

import com.tumei.common.DaoGame;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.RoleBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 切换时装
 */
@Component
public class RequestChangeSkin extends BaseProtocol {
	public int seq;
	public int skin;

	class Return extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		Return rci = new Return();
		rci.seq = seq;

		long uid = user.getUid();
		RoleBean rb = DaoGame.getInstance().findRole(uid);
		HerosBean hsb = DaoGame.getInstance().findHeros(uid);

		if (skin == 0) {
			rb.setIcon(90010 + (rb.getGrade() - 2) * 10 + rb.getSex() * 100);
		}
		else if (!hsb.hasSkin(skin)) {
			rci.result = "指定的时装不存在";
			user.send(rci);
			return;
		}

		hsb.setSkin(skin);

		if (skin != 0) {
			rb.setIcon(hsb.getFakeHero());
		}
		hsb.updateBuffs();

		user.send(rci);
	}
}
