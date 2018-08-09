package com.tumei.game.protos.scene;

import com.tumei.GameConfig;
import com.tumei.common.DaoGame;
import com.tumei.game.GameUser;
import com.tumei.model.RoleBean;
import com.tumei.model.WarBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.AddtimeConf;
import com.tumei.websocket.WebSocketUser;
import com.tumei.model.PackBean;
import com.tumei.model.SceneBean;
import com.tumei.common.Readonly;
import com.tumei.common.utils.ErrCode;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestSceneFast extends BaseProtocol {
	public int seq;

	public class ReturnSceneFast extends BaseProtocol {
		public int seq;
		public String result = "";
		/**
		 * 同步此时的能量
		 */
		public int energy;
		/**
		 * 消耗的钻石
		 */
		public int gem;
		/**
		 * 普通事件次数
		 */
		public int event;
		/**
		 * 特殊事件次数
		 */
		public int special;
		/**
		 * 通关后的奖励
		 */
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnSceneFast rl = new ReturnSceneFast();
		rl.seq = seq;

		SceneBean sb = user.getDao().findScene(user.getUid());
		sb.flush();

		PackBean pb = user.getDao().findPack(user.getUid());

		int scount = sb.getSpeedCount();

		RoleBean rb = user.getDao().findRole(user.getUid());
		if (scount >= Readonly.getInstance().findVip(rb.getVip()).speedup) {
			rl.result = "今日副本加速次数已达上限";
			user.send(rl);
			return;
		}

		AddtimeConf adc = Readonly.getInstance().findAddtime(scount);
		rl.gem = 1100;
		if (adc != null) {
			rl.gem = adc.cost;
		}

		if (!pb.contains(钻石, rl.gem)) {
			rl.result = ErrCode.钻石不足.name();
			user.send(rl);
			return;
		}
		user.payItem(钻石, rl.gem, "快速战斗(" + sb.getScene() + ")关");

		sb.addSpeedCount(1);
		sb.harvest(user, GameConfig.getInstance().getSceneFastTime(), rl);
		rl.energy = sb.updateEnergy(GameConfig.getInstance().getSceneFastRecover());


		WarBean wb = DaoGame.getInstance().findWar(user.getUid());
		wb.generateEmergy(user.getLevel());

		user.send(rl);
	}
}
