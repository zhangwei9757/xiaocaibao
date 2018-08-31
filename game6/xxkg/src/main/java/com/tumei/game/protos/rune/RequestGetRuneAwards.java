package com.tumei.game.protos.rune;

import com.tumei.common.Readonly;
import com.tumei.game.GameUser;
import com.tumei.game.services.RankService;
import com.tumei.model.RuneBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 锤子敲 9个 地洞
 *
 *
 */
@Component
public class RequestGetRuneAwards extends BaseProtocol {
	public int seq;

	class ReturnGetRuneAwards extends BaseProtocol {
		public int seq;

		public String result = "";

		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnGetRuneAwards rl = new ReturnGetRuneAwards();
		rl.seq = seq;

		RuneBean rb = user.getDao().findRune(user.getUid());

		rb.flush(user.getVip());

		int rank = RankService.getInstance().getLastGroupRank(rb.getLastGroup());

		if (rank == 0) {
			rl.result = "阵营奖励过期或者已经领取";
		} else {
			int[][] rwd = Readonly.getInstance().findFuwen(1).rankreward;
			rl.awards.addAll(user.addItems(rwd[rank - 1], "周符文排行奖励"));
		}

		rb.setLastGroup(0);
		user.send(rl);
	}
}
