package com.tumei.game.protos.rune;

import com.tumei.game.GameUser;
import com.tumei.game.services.RankService;
import com.tumei.model.RuneBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.FuwenraidConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 测试充值协议
 */
@Component
public class RequestRuneStep extends BaseProtocol {
	public int seq;

	class ReturnRuneStep extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
		public int score;
		public int groupScore;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnRuneStep rl = new ReturnRuneStep();
		rl.seq = seq;

		RuneBean rb = user.getDao().findRune(user.getUid());

		int level = rb.getLevel();
		if (!rb.step(false)) {
			rl.result = "没有钥匙,无法进入下一层";
			user.send(rl);
			return;
		}

		FuwenraidConf fc = Readonly.getInstance().findFuwen(level);
		// 1. 提交阵营积分
		rl.groupScore = RankService.getInstance().submitScore(rb.getGroup(), fc.score);
		// 2. 获得通关奖励, vip6以上获得所有奖励
		if (user.getVip() >= 6) {
			rl.awards.addAll(user.addItems(fc.clear, "符文通关:" + level));
		} else {
			int[] tmp = Arrays.copyOf(fc.clear, fc.clear.length - 2);
			rl.awards.addAll(user.addItems(tmp, "符文通关:" + level));
		}
		rl.score = fc.score;

		user.send(rl);
	}
}
