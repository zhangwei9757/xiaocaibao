package com.tumei.game.protos.activity;

import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DbriderConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tumei.common.utils.Defs.*;
import static com.tumei.common.utils.ErrCode.钻石不足;

/**
 * Created by leon on 2016/12/31.
 * 请求 夺宝
 */
@Component
public class RequestDuoBaoRider extends BaseProtocol {
	public int seq;

	/**
	 * 0: 普通
	 * 1: 使用寻宝令
	 */
	public int mode;

	class ReturnDuoBaoRider extends BaseProtocol {
		public int seq;

		// 夺宝后等级,可能发生变化也可能不
		public int level;

		// 是否使用钻石，如果返回钻石为0，表示有道具，先使用的道具 藏宝图
		public int gem;

		public List<AwardBean> awards = new ArrayList<>();

		/**
		 * 每次随机的箱子
		 */
		public List<Integer> box = new ArrayList<>();

		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;
		ReturnDuoBaoRider rl = new ReturnDuoBaoRider();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushDb();

		long time = ab.flushDbCouple();
		// 13  夺宝奇兵钻石由60改为1200
		int gem = Defs.夺宝奇兵;
		if (time > 0) {
			if (ab.getCoupleType() == 1) {
				gem = (int) (Defs.夺宝奇兵 * 0.8);// 0.8
			}
			else {
				gem = (int) (Defs.夺宝奇兵 * 0.9);// 0.9
			}
		}

		PackBean pb = user.getDao().findPack(user.getUid());

		if (mode == 1) {
			if (ab.getDbLevel() < 3) {
				if (!pb.contains(夺宝令, 1)) {
					rl.result = "没有足够的寻宝令";
					user.send(rl);
					return;
				}
			}
		}


		if (!pb.contains(夺宝图, 1)) {
			if (!pb.contains(钻石, gem)) {
				rl.result = 钻石不足.name();
				user.send(rl);
				return;
			}
			else {
				user.payItem(钻石, gem, "进行夺宝");
				rl.gem = gem;
			}
		}
		else {
			user.payItem(夺宝图, 1, "进行夺宝");
		}

		if (mode == 1) {
			if (ab.getDbLevel() < 3) {
				user.payItem(夺宝令, 1, "进行夺宝");
				ab.setDbLevel(3);
			}
		}

		int level = ab.getDbLevel();
		DbriderConf dc = Readonly.getInstance().findDbRiderConf(level);

		// 计算宝箱数量
		int total = 0;
		int count = 0;
		int r = RandomUtil.getRandom() % 100;
		for (int ii : dc.num) {
			++count;
			total += ii;
			if (r <= total) {
				break;
			}
		}

		// q1~q4选择count个箱子
		List<Integer> n = new ArrayList<>();
		for (int i = 1; i < 5; ++i) {
			n.add(i);
		}
		Collections.shuffle(n);

		for (int i = 0; i < count; ++i) {
			int idx = n.get(i);
			int[][] q = null;
			switch (idx) {
				case 1:
					q = dc.q1;
					break;
				case 2:
					q = dc.q2;
					break;
				case 3:
					q = dc.q3;
					break;
				case 4:
					q = dc.q4;
					break;
			}
			rl.box.add(idx);

			total = 0;
			r = RandomUtil.getRandom() % 100;
			for (int[] qr : q) {
				total += qr[3];
				if (r <= total) {
					int c = RandomUtil.getBetween(qr[1], qr[2]);
					rl.awards.addAll(user.addItem(qr[0], c, true, "进行夺宝"));
					break;
				}
			}
		}

		if (level < 4) {
			if (dc.rate <= 100 && dc.rate >= RandomUtil.getRandom() % 100) {
				++level;
				ab.setDbLevel(level);
			}
		}
		else {
			level = 1;
			ab.setDbLevel(level);
		}

		user.addItem(夺宝积分, 5, false, "进行夺宝");
		rl.level = ab.getDbLevel();
		user.send(rl);
	}
}
