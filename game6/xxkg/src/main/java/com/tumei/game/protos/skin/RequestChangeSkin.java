package com.tumei.game.protos.skin;

import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.RoleBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 换时装
 */
@Component
public class RequestChangeSkin extends BaseProtocol {
	public int seq;
	public int skin;

	class ReturnChangeSkin extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnChangeSkin rci = new ReturnChangeSkin();
		rci.seq = seq;

		long uid = user.getUid();
		RoleBean rb = user.getDao().findRole(uid);
		HerosBean hsb = user.getDao().findHeros(uid);

		if (skin == 0) {
			if (rb.getSex() == 0) {
				rb.setIcon(90010 + (rb.getGrade() - 2) * 10);
			}
			else {
				rb.setIcon(90110 + (rb.getGrade() - 2) * 10);
			}
		}
		else if (!hsb.getSkins().containsKey(skin)) {
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
