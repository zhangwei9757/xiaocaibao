package com.tumei.game.protos.treasure;

import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.game.services.RankService;
import com.tumei.game.services.TreasureRankService;
import com.tumei.model.RuneBean;
import com.tumei.model.TreasureBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 *
 */
@Component
public class RequestTreasureInfo extends BaseProtocol {
	public int seq;

	class ReturnTreasureInfo extends BaseProtocol {
		public int seq;

		// 当前宝箱的类型 1：铜 	2：银 	3：金
		public int box;

		// 今日一共挖掘的次数，前三次免费
		public int digCount;
		// 今日一共的刷新次数,前10次免费
		public int flushCount;

		// 积分
		public int score;
		// 排名, -1是未上榜
		public int rank;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnTreasureInfo rl = new ReturnTreasureInfo();
		rl.seq = seq;

		if (user.getLevel() < Defs.神秘宝藏等级) {
			user.send(rl);
			return;
		}

		TreasureBean tb = user.getDao().findTreasure(user.getUid());
		tb.flush();

		rl.box = tb.getCurrent();
		rl.digCount = tb.getDigCount();
		rl.flushCount = tb.getFlushCount();
		rl.score = tb.getScore();
		rl.rank = TreasureRankService.getInstance().getRank(user.getUid());

		user.send(rl);
	}
}
