package com.tumei.game.protos.rune;

import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.game.services.RankService;
import com.tumei.model.RuneBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 锤子敲 9个 地洞
 *
 *
 */
@Component
public class RequestRuneInfo extends BaseProtocol {
	public int seq;

	class ReturnRuneInfo extends BaseProtocol {
		public int seq;

		// 锤子个数
		public int hammer;
		// 是否有首次免费
		public int isFree;
		// 剩余重置次数
		public int count;
		// 剩余改命次数
		public int revert;
		public int level;
		// 最佳奖励, 如果AwardBean的Count小于0，表示该奖励已经领取
		public List<AwardBean> best;
		// 0未打开
		// 1物品奖励图标
		// 2打折
		// 3怪物
		// 4钥匙
		// 5第一个最佳奖励
		// 6第二个最佳奖励
		// 9砸过没打开
		public int[] objects = new int[9];
		// 阵营
		public int group;
		// 积分
		public List<Integer> scores;

		// 上周阵营排行, 如果为0表示没有奖励可领取，否则[1,4]表示有奖励可领取，并且具体排名
		public int rank;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnRuneInfo rl = new ReturnRuneInfo();
		rl.seq = seq;
		if (user.getLevel() < Defs.符文副本等级) {
			user.send(rl);
			return;
		}

		RuneBean rb = user.getDao().findRune(user.getUid());

		rb.flush(user.getVip());

		rl.hammer = rb.getHammer();
		rl.isFree = (rb.getUsedCount() == 0) ? 1 : 0;

		VipConf vc = Readonly.getInstance().findVip(user.getVip());
		rl.count = vc.fuwenre - rb.getUsedCount();
		rl.revert = rb.getRevert();
		rl.level = rb.getLevel();

		rl.objects = Arrays.copyOf(rb.getObjects(), rb.getObjects().length);
		rl.best = rb.getBest();

		rl.group = rb.getGroup();
		rl.scores = RankService.getInstance().getGroupScores();

		rl.rank = RankService.getInstance().getLastGroupRank(rb.getLastGroup());

		user.send(rl);
	}
}
