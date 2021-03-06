package com.tumei.game.protos.scene;

import com.tumei.common.Readonly;
import com.tumei.dto.battle.DirectHeroStruct;
import com.tumei.dto.battle.FightResult;
import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.game.services.RankService;
import com.tumei.groovy.contract.IBattle;
import com.tumei.model.HerosBean;
import com.tumei.model.SceneBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.RaidConf;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.金币;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestSceneFight extends BaseProtocol {
	public int seq;

	/**
	 * 0: 普通挑战副本
	 * 1: 扫荡
	 */
	public int mode;

	class ReturnSceneFight extends BaseProtocol {
		public int seq;
		public String result = "";
		/**
		 * 战报
		 */
		public String data = "";

		/**
		 * win = 1 表示本次挑战胜利
		 */
		public int win;

		public int energy;

		/**
		 * 通关后的奖励
		 */
		public List<AwardBean> awards = new ArrayList<>();

		public List<AwardBean> finalawards = new ArrayList<>();
	}


	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnSceneFight rl = new ReturnSceneFight();
		rl.seq = seq;

		SceneBean sb = user.getDao().findScene(user.getUid());
		sb.flush();

		RaidConf rc = Readonly.getInstance().findRaid(sb.getScene());

		if (sb.getEnergy() < rc.cost) {
			rl.result = ErrCode.挂机加速可快速获得挑战能量.name();
			user.send(rl);
			return;
		}

		HerosBean hsb = user.getDao().findHeros(user.getUid());

		int ratio;
		if (mode == 0) { 	// 正常挑战
			ratio = 100;
		} else {			// 扫荡
			ratio = 20;
		}

		List<DirectHeroStruct> arg = rc.makeSceneBattle(ratio);
		IBattle battle = GameServer.getInstance().getBattleSystem();
		FightResult r = battle.doSceneBattle(hsb.createHerosStruct(), arg, rc.condition, false, rc.glory[0], rc.glory[1], rc.glory[2], rc.glory[3]);

		rl.data = r.data;

		if (r.win == 1) { // 只有胜利才能增加自己的金币与经验
			rl.win = 1;
			sb.setEnergy(sb.getEnergy() - rc.cost);
			if (mode == 0) {
				int s = sb.getScene() + 1;
				sb.setScene(s);

				if (rc.chest.length != 0) {
					rl.finalawards.addAll(user.addItems(rc.chest, "副本"));
				}

				rl.awards.addAll(user.addItems(rc.reward1, "副本"));
				rl.awards.addAll(user.addItems(rc.reward2, "副本"));
				rl.awards.addAll(user.addItems(rc.reward3, "副本"));
				rl.awards.addAll(user.addItems(rc.reward4, "副本"));
				rl.awards.addAll(user.addItems(rc.reward5, "副本"));

				RankService.getInstance().putScene(user.getUid(), s);
			}
			else if (mode == 1) { // 扫荡额外掉落
				VipConf vc = Readonly.getInstance().findVip(user.getVip());
				int gold = rc.gold / 3600 * rc.cd * vc.sceneadd; // 扫荡获取的金币
				rl.awards.addAll(user.addItem(金币, gold, false, "副本扫荡"));
//				user.warn("获得金币：" + gold);

				if (vc.sceneadd >= 1 && RandomUtil.getBetween(1, 100) <= rc.addrate) {
					// 1. drop和sweep合并后，随机一组奖励出来
					List<Integer> a = new ArrayList<>();
					for (int n : rc.drop) {
						a.add(n);
					}
					for (int n : rc.sweep) {
						a.add(n);
					}
					int id = RandomUtil.getInList(a);
					int count = vc.sceneadd;
					List<AwardBean> awds = user.addItem(id, 1, true, "副本扫荡");
					if (count > 1) {
						for (AwardBean ab : awds) {
							user.addItem(ab.id, ab.count * (count - 1), false, "副本扫荡");
							ab.count *= count;
						}
					}

					rl.awards.addAll(awds);
				}
			}
		}

		rl.energy = sb.getEnergy();

		user.send(rl);
	}
}
