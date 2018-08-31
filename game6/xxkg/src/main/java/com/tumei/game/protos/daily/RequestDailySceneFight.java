package com.tumei.game.protos.daily;

import com.tumei.common.RemoteService;
import com.tumei.common.fight.FightResult;
import com.tumei.common.structs.SceneFightStruct;
import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.model.DailySceneBean;
import com.tumei.model.HerosBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DailyraidConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 *
 * 挑战日常副本
 */
@Component
public class RequestDailySceneFight extends BaseProtocol {
    public int seq;
	/**
	 * 配置表 Dailyraid中的key
	 */
	public int index;

    class ReturnDailySceneFight extends BaseProtocol {
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
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnDailySceneFight rl = new ReturnDailySceneFight();
		rl.seq = seq;

		if (user.getLevel() < Defs.日常副本等级) {
			rl.result = "领主等级不足";
			user.send(rl);
			return;
		}


		RoleBean rb = user.getDao().findRole(user.getUid());
		DailyraidConf fc = Readonly.getInstance().findDailyRaids(index);
		// 周 1， 3， 5
		LocalDate d = LocalDate.now();
		int weekday = d.getDayOfWeek().getValue();
		if (weekday % 2 == 0) {
			if (fc.mode % 2 != 0) {
				rl.result = "今天未开放此副本";
				user.send(rl);
				return;
			}
		} else {
			if (weekday != 7) {
				if (fc.mode % 2 == 0) {
					rl.result = "今天未开放此副本";
					user.send(rl);
					return;
				}
			}
		}

		DailySceneBean dsb = user.getDao().findDailyScene(user.getUid());
		dsb.flush();
		if (dsb.getScenes().containsKey(index)) {
			rl.result = "今天已经挑战过此副本";
			user.send(rl);
			return;
		}


		// 2. 根据当前scene 填充右边
		HerosBean hsb = user.getDao().findHeros(user.getUid());
		SceneFightStruct arg = new SceneFightStruct();
		arg.setUid(user.getUid());

		// 1. 填充左边
		hsb.fill(arg.getLineups(), arg.getBuffs(), arg.getLeft(), arg.getArts());

		// 2. 填充右边
		arg.fillRightByDailyScene(fc);

		FightResult r = RemoteService.getInstance().callSceneFight(arg);

		rl.data = r.data;

		if (r.win == 1) {
			rl.win = 1;

			rl.awards.addAll(user.addItems(fc.goods, "日常副本战斗"));
			dsb.getScenes().put(index, 1);
		}

		user.pushDailyTask(11, 1);
        user.send(rl);
    }
}
