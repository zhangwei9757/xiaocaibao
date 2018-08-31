package com.tumei.game.protos.misc;

import com.google.common.base.Strings;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求充值信息
 */
@Component
public class RequestChooseSex extends BaseProtocol {
	public int seq;
	// 0 男性
	// 1 女性
	public int sex;
	public String name = "";

	class ReturnChooseSex extends BaseProtocol {
		public int seq;
		public String result = "";
		public HeroBean hb;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnChooseSex rl = new ReturnChooseSex();
		rl.seq = seq;

		name = name.trim();
		if (Strings.isNullOrEmpty(name)) {
			rl.result = "昵称不能为空白符";
			user.send(rl);
			return;
		}
		if (name.length() < 1) {
			rl.result = "昵称不能长度不足";
			user.send(rl);
			return;
		}
		if (name.toLowerCase().startsWith("tm_")) {
			rl.result = "系统使用的名称";
			user.send(rl);
			return;
		}

		RoleBean rb = user.getDao().findRole(user.getUid());

		if (!user.getDao().changeName(user.getUid(), name, rb.getNickname())) {
			rl.result = "重复的昵称";
			user.send(rl);
			return;
		} else {
			rb.setNickname(name);
			user.setName(name);
		}

		rl.hb = user.initRole(sex);
		if (rl.hb == null) {
			rl.result = "无法再次选择性别";
			user.send(rl);
			return;
		}

		user.send(rl);
	}
}
