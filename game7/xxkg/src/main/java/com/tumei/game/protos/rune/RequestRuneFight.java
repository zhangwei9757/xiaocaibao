package com.tumei.game.protos.rune;

import com.tumei.common.Readonly;
import com.tumei.dto.battle.FightResult;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.RuneBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.FuwenraidConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 */
@Component
public class RequestRuneFight extends BaseProtocol {
	public int seq;

	class ReturnRuneFight extends BaseProtocol {
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
		 * 通关后的奖励
		 */
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnRuneFight rl = new ReturnRuneFight();
		rl.seq = seq;

		RuneBean rb = user.getDao().findRune(user.getUid());
		FuwenraidConf fc = Readonly.getInstance().findFuwen(rb.getLevel());
		HerosBean hsb = user.getDao().findHeros(user.getUid());
		FightResult r = GameServer.getInstance().getBattleSystem().doSceneBattle(hsb.createHerosStruct(), fc.makeRune(), 0, false, 0, 0, 0, 0);

		rl.data = r.data;

		if (r.win == 1) { // 只有胜利才能增加自己的金币与经验
			rl.win = 1;

			int idx = RandomUtil.getIntArrayDouble(fc.mosreward);
			rl.awards.addAll(user.addItem(fc.mosreward[idx], fc.mosreward[idx + 1], true, "符文战斗"));
		}

		user.send(rl);
	}
}
