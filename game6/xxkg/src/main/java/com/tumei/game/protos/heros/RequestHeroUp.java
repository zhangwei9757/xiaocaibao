package com.tumei.game.protos.heros;

import com.google.common.base.Strings;
import com.tumei.common.Readonly;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.*;
import com.tumei.websocket.SessionUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级英雄
 */
@Component
public class RequestHeroUp extends BaseProtocol {
	public int seq;
	/**
	 * 英雄唯一id
	 */
	public int hid;
	/**
	 * 0: 等级提升
	 * 1: 突破
	 * 2: 境界 点击一次
	 * 3: 境界 一键提升
	 *
	 */
	public int mode;

	class ReturnHeroUp extends BaseProtocol {
		public int seq;
		/**
		 * 直冲天命消耗的封印石个数
		 */
		public int stones;
		/**
		 * 最后的天命和天命等级
		 */
		public int fate;
		public int fateexp;

		public List<Integer> heros = new ArrayList<>();

		/**
		 * 结果
		 */
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnHeroUp rci = new ReturnHeroUp();
		rci.seq = seq;

		long uid = user.getUid();
		HerosBean hsb = user.getDao().findHeros(uid);

		RoleBean rb = user.getDao().findRole(uid);
		Optional<HeroBean> opt = Arrays.stream(hsb.getHeros()).filter((HeroBean hb) -> hb.getHid() == hid).findFirst();

		opt.ifPresent(hb -> {
			if (mode == 0) { // 升级
				int level = hb.getLevel();
				if (level >= rb.getLevel()) {
					rci.result = ErrCode.英雄等级不能超过战队等级.name();
					return;
				}

				HeroConf hc = Readonly.getInstance().findHero(hb.getId());
				int pinzhi = hc.quality;
				if (hb.isLord()) {
					pinzhi = 5;
				}

				HeroupConf hub = Readonly.getInstance().findHeroup(level);
				if (hub == null) {
					rci.result = "英雄已经达到最大等级";
					return;
				} else {
					int coin = 0;
					switch (pinzhi)
					{
						case 1:
							coin = hub.green;
							break;
						case 2:
							coin = hub.blue;
							break;
						case 3:
							coin = hub.purple;
							break;
						case 4:
							coin = hub.orange;
							break;
						case 5:
							coin = hub.red;
							break;
						default:
							user.info("品质:" + pinzhi + " 没有对应的解释,只能是[1,5].");
							return;
					}

					if (coin == 0) {
						rci.result = "没有对应的品质,无法知道英雄升级所需金币";
						return;
					} else {
						PackBean pb = user.getDao().findPack(uid);
						if (!pb.contains(金币, coin)) {
							rci.result = ErrCode.金币不足.name();
							return;
						} else {
							user.payItem(金币, coin, "英雄刷新");
							hb.setLevel(level + 1);
							ActivityBean ab = user.getDao().findActivity(uid);
							ab.commitHeroLevel(level + 1);
						}
					}
				}
			} else if (mode == 1) { // 突破
				int grade = hb.getGrade();

				HerobreakConf hub = Readonly.getInstance().findHerobreak(grade);
				if (hub == null) {
					rci.result = "英雄已经达到最大突破等级";
					return;
				} else {
					int cost1 = hub.cost1;
					int cost2 = hub.cost2;
					int cost3 = hub.cost6;

					if (hb.isLord()) {
						cost1 = hub.cost4;
						cost2 = hub.cost5;
						cost3 = hub.cost7;
					}

					if (hb.getLevel() < hub.level) {
						rci.result = ErrCode.英雄等级不足.name();
					} else {
						PackBean pb = user.getDao().findPack(uid);
						if (!pb.contains(金币, cost1)) {
							rci.result = ErrCode.金币不足.name();
						}

						if (!pb.contains(突破石, cost2)) {
							rci.result = "突破石不足";
						}

						if (!hb.isLord() && !pb.hasHeros(hb.getId(), hub.cost3)) {
							rci.result = "没有对应的初始状态的英雄";
						}

						if (cost3 > 0 && !pb.contains(突破玉, cost3)) {
							rci.result = "突破玉不足";
						}

						if (Strings.isNullOrEmpty(rci.result)) {
							user.payItem(金币, cost1, "突破");
							user.payItem(突破石, cost2, "突破");
							if (cost3 > 0) {
								user.payItem(突破玉, cost3, "突破");
							}

							if (!hb.isLord()) {
								rci.heros = pb.payHero(hb.getId(), hub.cost3, "突破");
							}
							hb.setGrade(grade + 1);
						}
					}
				}
			} else if (mode == 2) { // 提升天命，一次
				promoteFate(hb, user, uid, rci, false);
			} else if (mode == 3) { // 提升天命，直到升级，或者石头不够
				promoteFate(hb, user, uid, rci, true);
			} else {
				rci.result = ErrCode.未知参数.name();
				return;
			}
		});

		user.send(rci);
	}

	private void promoteFate(HeroBean hb, GameUser user, long uid, ReturnHeroUp rci, boolean _continue) {
		int fate = hb.getFate();
		StateupConf sc = Readonly.getInstance().findStateup(fate);
		if (sc == null) {
			rci.result = "英雄已经达到最大天命等级";
			return;
		} else {
			PackBean pb = user.getDao().findPack(uid);
			do {
				if (!pb.contains(封印石, sc.click)) {
					rci.result = ErrCode.封印石不足.name();
					break;
				}
				user.payItem(封印石, sc.click, "提升天命");
				rci.stones += sc.click;

				int fexp = hb.getFateexp() + sc.click;
				if (fexp >= sc.cost) { // 满值必定升级
					hb.setFate(fate + 1);
					hb.setFateexp(0);
					break;
				} else { // 判断直升机率
					int r = (int)(fexp * 100f / sc.cost);
					int ratio = sc.chance[r/25];

					if ((RandomUtil.getBetween(1, 100)) <= ratio) {
						// 记录一下补偿，重生的时候需要归还
						int diff = sc.cost - fexp;
						if (diff > 0) {
							hb.setFateCost(hb.getFateCost() + diff);
						}

						// 可以直接提升天命
						hb.setFate(fate + 1);
						hb.setFateexp(0);
						break;
					} else {
						hb.setFateexp(fexp);
					}
				}
			} while (_continue);

			rci.fate = hb.getFate();
			rci.fateexp = hb.getFateexp();
		}
	}
}
