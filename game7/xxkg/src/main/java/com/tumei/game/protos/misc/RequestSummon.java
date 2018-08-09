package com.tumei.game.protos.misc;

import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.WebSocketUser;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.SummonBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.HeroConf;
import com.tumei.common.Readonly;
import com.tumei.modelconf.SummonConf;
import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.传奇令;
import static com.tumei.common.utils.Defs.召唤令;
import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级英雄
 */
@Component
public class RequestSummon extends BaseProtocol {
	public int seq;
	/**
	 * rmb
	 * 0: 普通单次召唤
	 * 1: 普通十连召唤
	 * 2: 中等单次
	 * 3: 中等十次
	 * 4: 高级
	 * 5: 高级十次
	 *
	 * 6: 幸运值达到1000后，直接指定兑换英雄
	 */
	public int mode;
	/**
	 * 幸运值兑换红色英雄的时候使用,指定英雄的id
	 */
	public int hero;

	class ReturnSummon extends BaseProtocol {
		public int seq;
		public String result = "";
		/**
		 * 本次是否免费, 0:免费，非零就是有花费，客户端自己计算花费
		 */
		public int fee;
		/**
		 * 抽奖后当前的幸运值
		 */
		public int lucky;

		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnSummon rs = new ReturnSummon();
		rs.seq = seq;

		long uid = user.getUid();
		SummonBean sb = user.getDao().findSummon(uid);
		sb.flushSummon(user);

		PackBean pb = user.getDao().findPack(uid);

		switch (mode) {
			case 0: // 单次初级召唤
			{
				// 1. 判断当前是不是处于冷却时间
				long last = sb.getLastSmallFree();
				// 10分钟即可恢复
				long now = System.currentTimeMillis() / 1000;
				int count = sb.getSmallFreeCount();
				if (count > 0 && now - last >= 600) {
					sb.setLastSmallFree(now);
					sb.setSmallFreeCount(count - 1);
					rs.fee = 0;
				} else {
					rs.fee = 1;
					if (!pb.contains(召唤令, 1)) {
						rs.result = ErrCode.没有足够的召唤令.name();
						break;
					} else {
						user.payItem(召唤令, 1, "小召唤");
					}
				}

				int quality = 1;
				int ratio = RandomUtil.getRandom() % 100;
				int total = 0;
				for (int i = 0; i < 5; ++i) {
					SummonConf sc = Readonly.getInstance().findSummon(i);
					total += sc.summon1;
					if (ratio < total) {
						quality = sc.quality;
						break;
					}
				}

				List<HeroConf> tmp = Readonly.getInstance().findHeroByQuality(quality);
				int hero = tmp.get((RandomUtil.getRandom() % tmp.size())).key;
				HeroBean hb = pb.addHero(hero, "单次初级召");
				rs.awards.add(new AwardBean(hero, 1, hb.getHid()));
				sb.setSmallCount(sb.getSmallCount() + 1);
				user.pushDailyTask(1, 1);
				break;
			}
			case 1: // 十次初级召唤
			{
				rs.fee = 1;
				if (!pb.contains(召唤令, 10)) {
					rs.result = ErrCode.没有足够的召唤令.name();
					break;
				} else {
					user.payItem(召唤令, 10, "十次小召唤");
				}

				for (int j = 0; j < 10; ++j) {
					int quality = 1;
					int ratio = RandomUtil.getRandom() % 100;
					int total = 0;
					for (int i = 0; i < 5; ++i) {
						SummonConf sc = Readonly.getInstance().findSummon(i);
						total += sc.summon1;
						if (ratio < total) {
							quality = sc.quality;
							break;
						}
					}

					List<HeroConf> tmp = Readonly.getInstance().findHeroByQuality(quality);
					int hero = tmp.get((RandomUtil.getRandom() % tmp.size())).key;
					HeroBean hb = pb.addHero(hero, "十连初级召");
					rs.awards.add(new AwardBean(hero, 1, hb.getHid()));
					sb.setSmallCount(sb.getSmallCount() + 1);
				}
				user.pushDailyTask(1, 10);
				break;
			}
			case 2: // 单次中级召唤
			{
				// 1. 判断当前是不是处于冷却时间
				long last = sb.getLastMiddleFree();
				// 24小时即可恢复
				long now = System.currentTimeMillis() / 1000;
				if (now - last >= 24 * 3600) {
					sb.setLastMiddleFree(now);
					rs.fee = 0;
				} else {
					int gem = (sb.getTodayCount() <= 0 ? 150 : 300);
					rs.fee = 1;
					if (!pb.contains(传奇令, 1)) {
						if (!pb.contains(钻石, gem)) {
							rs.result = ErrCode.钻石不足.name();
							break;
						} else {
							user.payItem(钻石, gem, "大召唤");
							sb.setTodayCount(sb.getTodayCount() + 1);
						}
					} else {
						user.payItem(传奇令, 1, "大召唤");
					}
				}

				int hero = 60010;
				if (last != 0) {
					int quality = 1;
					if ((sb.getMiddleCount() % 10) == 9) {
						quality = 4;
					} else {
						int ratio = RandomUtil.getRandom() % 100;
						int total = 0;
						for (int i = 0; i < 5; ++i) {
							SummonConf sc = Readonly.getInstance().findSummon(i);
							total += sc.summon2;
							if (ratio < total) {
								quality = sc.quality;
								break;
							}
						}
					}
					List<HeroConf> tmp = Readonly.getInstance().findHeroByQuality(quality);
					hero = tmp.get((RandomUtil.getRandom() % tmp.size())).key;
				}
				HeroBean hb = pb.addHero(hero, "单次中级召");
				rs.awards.add(new AwardBean(hero, 1, hb.getHid()));
				sb.setMiddleCount(sb.getMiddleCount() + 1);
				user.pushDailyTask(1, 1);
				user.pushDailyTask(2, 1);
				break;
			}
			case 3: // 十次中级召唤
			{
				int gem = 2800;
				rs.fee = 1;
				if (!pb.contains(传奇令, 10)) {
					if (!pb.contains(钻石, gem)) {
						rs.result = ErrCode.钻石不足.name();
						break;
					} else {
						user.payItem(钻石, gem, "十次大召唤");
					}
				} else {
					user.payItem(传奇令, 10, "十次大召唤");
				}

				for (int j = 0; j < 10; ++j) {
					int quality = 1;
					if ((sb.getMiddleCount() % 10) == 9) {
						quality = 4;
					} else {
						int ratio = RandomUtil.getRandom() % 100;
						int total = 0;
						for (int i = 0; i < 5; ++i) {
							SummonConf sc = Readonly.getInstance().findSummon(i);
							total += sc.summon2;
							if (ratio < total) {
								quality = sc.quality;
								break;
							}
						}
					}

					List<HeroConf> tmp = Readonly.getInstance().findHeroByQuality(quality);
					int hero = tmp.get((RandomUtil.getRandom() % tmp.size())).key;
					HeroBean hb = pb.addHero(hero, "十次中级召");
					rs.awards.add(new AwardBean(hero, 1, hb.getHid()));
					sb.setMiddleCount(sb.getMiddleCount() + 1);
				}
				user.pushDailyTask(1, 10);
				user.pushDailyTask(2, 10);
				break;
			}

			case 4: // 单次高级召唤
			{
				int gem = (sb.getAdvanceFreeCount() > 0 ? 0 : 268);
				if (gem > 0) {
					rs.fee = 1;
				}

				if (gem > 0) {
					if (!pb.contains(钻石, gem)) {
						rs.result = ErrCode.钻石不足.name();
						break;
					} else {
						user.payItem(钻石, gem, "高级召唤");
					}
				} else {
					sb.setAdvanceFreeCount(0);
				}

				SummonConf sci = Readonly.getInstance().findSummon(sb.getAdvanceIndex());
				int cc = 0;
				int lucky = 0;

				{
					int r = RandomUtil.getRandom() % 100;
					int total = 0;
					for (int rmp : sci.sh3[2]) {
						++cc;
						total += rmp;
						if (r < total) {
							break;
						}
					}
				}
				{
					int r = RandomUtil.getRandom() % 100;
					int total = 0;
					for (int rmp : sci.sh3[3]) {
						++lucky;
						total += rmp;
						if (r < total) {
							break;
						}
					}
				}

				cc = cc * 5;
				sb.addLucky(lucky * 5);

				int quality = 4;
				int ratio = RandomUtil.getRandom() % 100;
				int total = 0;
				for (int i = 0; i < 5; ++i) {
					SummonConf sc = Readonly.getInstance().findSummon(i);
					total += sc.summon3;
					if (ratio < total) {
						quality = sc.quality;
						break;
					}
				}

				int[] tmp = sci.sh3[quality - 4];
				int hero = tmp[(RandomUtil.getRandom() % tmp.length)];

				user.addItem(hero, cc, false, "召唤");
				rs.awards.add(new AwardBean(hero, cc, 0));
				sb.setAdvanceCount(sb.getAdvanceCount() + 1);
				user.pushDailyTask(1, 1);
				break;
			}
			case 5: // 十次高级召唤
			{
				int gem = 2500;
				rs.fee = 1;

				if (!pb.contains(钻石, gem)) {
					rs.result = ErrCode.钻石不足.name();
					break;
				} else {
					user.payItem(钻石, gem, "高级召唤");
				}

				SummonConf sci = Readonly.getInstance().findSummon(sb.getAdvanceIndex());

				for (int j = 0; j < 10; ++j) {
					int cc = 0;
					int lucky = 0;

					{
						int r = RandomUtil.getRandom() % 100;
						int total = 0;
						for (int rmp : sci.sh3[2]) {
							++cc;
							total += rmp;
							if (r < total) {
								break;
							}
						}
					}
					{
						int r = RandomUtil.getRandom() % 100;
						int total = 0;
						for (int rmp : sci.sh3[3]) {
							++lucky;
							total += rmp;
							if (r < total) {
								break;
							}
						}
					}

					cc = cc * 5;
					sb.addLucky(lucky * 5);

					int quality = 4;
					int ratio = RandomUtil.getRandom() % 100;
					int total = 0;
					for (int i = 3; i < 5; ++i) {
						SummonConf sc = Readonly.getInstance().findSummon(i);
						total += sc.summon3;
						if (ratio < total) {
							quality = sc.quality;
							break;
						}
					}

					int[] tmp = sci.sh3[quality - 4];
					int hero = tmp[(RandomUtil.getRandom() % tmp.length)];

					user.addItem(hero, cc, false, "召唤");
					rs.awards.add(new AwardBean(hero, cc, 0));
					sb.setAdvanceCount(sb.getAdvanceCount() + 1);
				}
				user.pushDailyTask(1, 10);
				break;
			}
			case 6:
			{
				if (sb.getLucky() >= 1000) {
					sb.setLucky(0);
					sb.setLuckyCount(sb.getLuckyCount() + 1);
					HeroBean hb = pb.addHero(this.hero, "幸运");
					rs.awards.add(new AwardBean(hb.getId(), 1, hb.getHid()));
				}

				break;
			}
		}

		rs.lucky = sb.getLucky();
		user.send(rs);
	}
}
