package com.tumei.game.protos.misc;

import com.google.common.base.Strings;
import com.tumei.common.DaoGame;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
	public void onProcess(WebSocketUser session) {
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


		// 首次登录查看是否有测试期间的奖励
		int rmb = DaoGame.getInstance().checkOpenRmb(user.getUid());
		if (rmb > 0) {
			int grmb = rmb * 20;
			GameServer.getInstance().sendAwardMail(user.getUid(), "充值返钻", "游戏测试期间充值<color=red>" + rmb + "</color>元,返还<color=green>" + grmb + "</color>钻石", "10," + grmb);
		}

		// 先初始化用户信息，合区区服id，未合服则为0，没有则空
		user.initAccountInfo(0, ">>>>帐号id", ">>>>安装包的身份证");
		// 先初始化设备信息
		user.initDeviceInfo(1, "ios 11.0.1", "iphone", "iphoneXs", UUID.randomUUID().toString(), "127.0.0.1");
		// 注册行为日志，性别，如0，表示-女 1, 表示-男
		user.pushStaRegister_log(1 - sex, ">>>>职业");

		user.send(rl);
	}
}
