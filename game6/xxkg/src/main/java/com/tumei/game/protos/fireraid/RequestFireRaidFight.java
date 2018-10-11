package com.tumei.game.protos.fireraid;

import com.tumei.common.RemoteService;
import com.tumei.common.fight.FightResult;
import com.tumei.common.fight.SceneFightStruct;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.protos.structs.ChoiceStruct;
import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.SessionUser;
import com.tumei.game.GameUser;
import com.tumei.model.FireRaidBean;
import com.tumei.model.HerosBean;
import com.tumei.modelconf.FireraidConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 *
 * 远征
 *
 */
@Component
public class RequestFireRaidFight extends BaseProtocol {
	public int seq;
	/**
	 * 0: 正常调整当前关卡
	 * 1: 一键三星的方式挑战本章节
	 */
	public int mode;
	/**
	 * 难度: 1,2,3 3种难度
	 */
	public int hard;

	class ReturnFireRaidFight extends BaseProtocol {
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
		/**
		 * 胜利后的奖励
		 * 挑战胜利奖励，如果通关了本章节，还有章节奖励
		 */
		public List<AwardBean> awards = new ArrayList<>();
		public List<AwardBean> rewards = new ArrayList<>();
		/**
		 * 如果当前有没有选择的buff,则展示出来，里面一共有三组数据
		 * key表示属性，value是值，三对数据分为3星，6星，9星
		 */
		public List<ChoiceStruct> choise;

		public int[] ratio = new int[3];
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnFireRaidFight rl = new ReturnFireRaidFight();
		rl.seq = seq;


//		RoleBean rb = user.getDao().findRole(user.getUid());
		FireRaidBean frb = user.getDao().findFireRaid(user.getUid());
		frb.flush();

		if (frb.isFailed()) {
			rl.result = "请重置后才能挑战";
			user.send(rl);
			return;
		}

		FireraidConf fc0 = Readonly.getInstance().findFireraid(1);

		// 2. 根据当前 scene 填充右边
		if (mode == 0) {
			int scene = (frb.getScene() - 1) * 3 + hard;
			FireraidConf fc = Readonly.getInstance().findFireraid(scene);

			HerosBean hsb = user.getDao().findHeros(user.getUid());
			SceneFightStruct arg = new SceneFightStruct();
			arg.hss = hsb.createHerosStruct();
			arg.right = fc.makeFireRaid();
			arg.condition = fc.condition;

			frb.getBuffs().forEach((k, v) -> {
				arg.hss.buffs.merge(k , v, (a, b) -> a + b);
			});

			FightResult r = RemoteService.getInstance().callSceneFight(arg);

			rl.data = r.data;

			if (r.win == 1) {
				rl.win = 1;
				// 更新关卡对应的星数
				frb.upateStart(frb.getScene(), hard);

				// 更新buff选择
				if (frb.getScene() % 3 == 0) {
					rl.choise = frb.makeChoice();
				}

				// 1. 关卡前进一格
				frb.setScene(frb.getScene() + 1);
				// 2. 更新总星星
				int total = frb.getTotalStars() + hard;
				frb.setTotalStars(total);
				if (total > frb.getPeekStars()) {
					frb.setPeekStars(total);
					// 更新星星排名
					user.fixFireRaidRank(total, true);
				}

				// 3. 如果是3星难度，则看是否更新最高三星关卡
				if (hard == 3) {
					int peekScene = frb.getPeek3Stars();
					if (frb.getScene() > peekScene) {
						frb.setPeek3Stars(frb.getScene());
					}
				}

				// 4. 击杀奖励:
				// 4.1 计算奖励倍率
				int ratio = RandomUtil.getRandom() % 100;
				total = 0;
				int i = 0;
				for (; i < fc0.krewards[3].length; ++i) {
					total += fc0.krewards[3][i];
					if (ratio < total) {
						break;
					}
				}
				// 4.2 增加奖励
				rl.awards.addAll(user.addItems(fc0.krewards[hard - 1], i + 1, false, "远征:" + scene + " hard:" + hard));
				rl.ratio[0] = i + 1;

				// 5. 通关奖励
				if (fc.crewards.length >= 3) {
					int hard3 = hard;
					for (int j = 1; j < 3; ++j) {
						hard3 += frb.getStars().get(frb.getScene() - 2 - j);
					}
					user.info("本次章节结束总获得星:" + hard3 + ", 本次scene:" + frb.getScene()
						+ ", hard:" + hard);
					int index = 0;
					if (hard3 >= 9) {
						index = 2;
					} else if (hard3 >= 6) {
						index = 1;
					}
					rl.rewards.addAll(user.addItems(fc.crewards[index], "远征通关"));
				}

				// 6. 更新可用星数
				frb.setLeftStars(frb.getLeftStars() + hard);
			}
			else { // 失败则重置
				frb.setFailed(true);
			}

			user.pushDailyTask(5, 1);
		}
		else { // 直接胜利
			user.fixFireRaidRank(frb.getTotalStars(), false);
			int scene = frb.getScene();
			// 判断最高3星难度是否把本章节搞定了
			int fn = (scene - 1) / 3 * 3 + 3; // 这个是当前关卡所在章节的最后一关
			FireraidConf fc = Readonly.getInstance().findFireraid(fn * 3);
			if (frb.getPeek3Stars() <= fn) {
				rl.result = "本章最后一关未能三星通关，不能一键挑战";
			}
			else {
				rl.win = 1;
				// 1. 更新当前的关卡
				frb.setScene(fn + 1);
				for (int i = 1; i <= fn; ++i) {
					frb.upateStart(i, 3);
				}

				int count = fn + 1 - scene;
				// 2. 更新总星星数
				int total = frb.getTotalStars() + count * 3;
				frb.setTotalStars(total);
				if (total > frb.getPeekStars()) {
					frb.setPeekStars(total);
				}

				// 4. 击杀奖励:
				// 4.1 计算奖励倍率

				// 4.2 增加奖励
				for (int jj = 0; jj < count; ++jj) {
					int ratio = RandomUtil.getRandom() % 100;
					total = 0;
					int i = 0;
					for (; i < fc0.krewards[3].length; ++i) {
						total += fc0.krewards[3][i];
						if (ratio < total) {
							break;
						}
					}
					rl.awards.addAll(user.addItems(fc0.krewards[2], (i + 1), false, "远征扫荡"));
					rl.ratio[jj] = i + 1;
				}

				// 5. 通关奖励
				if (fc.crewards.length >= 3) {
					rl.rewards.addAll(user.addItems(fc.crewards[2], "远征扫荡通关"));
				}

				// buff选择
				rl.choise = frb.makeChoice();
				frb.setLeftStars(frb.getLeftStars() + 3 * count);
				user.pushDailyTask(5, 1);
			}
		}

		user.send(rl);
	}
}
