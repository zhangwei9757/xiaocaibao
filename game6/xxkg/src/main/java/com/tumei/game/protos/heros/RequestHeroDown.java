package com.tumei.game.protos.heros;

import com.tumei.common.Readonly;
import com.tumei.modelconf.*;
import com.tumei.websocket.SessionUser;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.common.utils.ErrCode;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by Administrator on 2017/3/13 0013.
 * <p>
 * 英雄的 重生 分解
 */
@Component
public class RequestHeroDown extends BaseProtocol {
	public int seq;

	/**
	 * 0: 重生 级别=1 突破=0 天命=1 觉醒=0   突破返还石头和英雄 升级返还金币 天命返还封印石(只有天命对应的,经验条的不返还)
	 * 1: 分解 重生的基础上，将该英雄还原成灵魂碎片，红色武将返还部分红色武将精华
	 */
	public int mode;

	/**
	 * 背包中的英雄 hids, 战队中，助战中的英雄不可以进行此操作
	 */
	public List<Integer> hids;

	class ReturnHeroDown extends BaseProtocol {
		public int seq;

		public String result = "";
		/**
		 * 返还的东西
		 */
		public List<AwardBean> returns = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnHeroDown rci = new ReturnHeroDown();
		rci.seq = seq;

		switch (mode) {
			case 0:
				重生(user, rci);
				break;
			case 1:
				分解(user, rci);
				break;
			default:
				rci.result = ErrCode.未知参数.name();
				break;
		}
		user.send(rci);
	}

	private void 重生(GameUser user, ReturnHeroDown rci) {
		int hid = hids.get(0);
		long uid = user.getUid();
		PackBean pb = user.getDao().findPack(uid);
		HeroBean hb = pb.getHeros().getOrDefault(hid, null);
		if (hb == null) {
			rci.result = ErrCode.英雄不存在.name();
			return;
		}

		if (hb.isLord()) {
			rci.result = "领主不能重生";
			return;
		}

		if (!pb.contains(钻石, 50)) {
			rci.result = ErrCode.钻石不足.name();
			return;
		}

		if (!hb.isInitStatus()) {
			HeroConf hi = Readonly.getInstance().findHero(hb.getId());

			long gold = 0;
			int tupo = 0;
			int tupoyu = 0;
			int yingxiong = 0;
			int fengyin = 0;

			// 升级相关
			for (int i = 1; i < hb.getLevel(); ++i) {
				HeroupConf hc = Readonly.getInstance().findHeroup(i);

				int pinzhi = hi.quality;
				if (hb.isLord()) {
					pinzhi = 5;
				}

				switch (pinzhi) {
					case 1:
						gold += hc.green;
						break;
					case 2:
						gold += hc.blue;
						break;
					case 3:
						gold += hc.purple;
						break;
					case 4:
						gold += hc.orange;
						break;
					case 5:
						gold += hc.red;
						break;
				}
			}

			// 突破
			for (int i = 0; i < hb.getGrade(); ++i) {
				HerobreakConf hub = Readonly.getInstance().findHerobreak(i);
				tupo += hub.cost2;
				yingxiong += hub.cost3;
				tupoyu += hub.cost6;
			}

			// 天命
			for (int i = 1; i < hb.getFate(); ++i) {
				StateupConf sc = Readonly.getInstance().findStateup(i);
				fengyin += sc.cost;
			}

			fengyin -= hb.getFateCost();
			hb.setFateCost(0);

			rci.returns.addAll(user.addItem(金币, gold, false, "重生英雄:" + hi.name));
			rci.returns.addAll(user.addHero(hb.getId(), yingxiong, "重生英雄:" + hi.name));
			rci.returns.addAll(user.addItem(封印石, fengyin, false, "重生英雄:" + hi.name));
			rci.returns.addAll(user.addItem(突破石, tupo, false, "重生英雄:" + hi.name));
			rci.returns.addAll(user.addItem(突破玉, tupoyu, false, "重生英雄:" + hi.name));

			user.payItem(钻石, 50, "重生英雄:" + hi.name);
			hb.reset();
		}
		else {
			rci.result = ErrCode.此英雄已经是初始状态.name();
		}
	}

	private void 分解(GameUser user, ReturnHeroDown rci) {
		for (int hid : hids) {
			long uid = user.getUid();
			PackBean pb = user.getDao().findPack(uid);
			HeroBean hb = pb.getHeros().getOrDefault(hid, null);
			if (hb == null) {
				continue;
			}

			HeroConf hi = Readonly.getInstance().findHero(hb.getId());

			long gold = 0;
			int tupo = 0;
			int tupoyu = 0;
			int yingxiong = 0;
			int fengyin = 0;

			// 升级相关
			for (int i = 1; i < hb.getLevel(); ++i) {
				HeroupConf hc = Readonly.getInstance().findHeroup(i);

				int pinzhi = hi.quality;
				if (hb.isLord()) {
					pinzhi = 5;
				}

				switch (pinzhi) {
					case 1:
						gold += hc.green;
						break;
					case 2:
						gold += hc.blue;
						break;
					case 3:
						gold += hc.purple;
						break;
					case 4:
						gold += hc.orange;
						break;
					case 5:
						gold += hc.red;
						break;
				}
			}

			// 突破
			for (int i = 0; i < hb.getGrade(); ++i) {
				HerobreakConf hub = Readonly.getInstance().findHerobreak(i);
				tupo += hub.cost2;
				yingxiong += hub.cost3;
				tupoyu += hub.cost6;
			}

			// 天命
			for (int i = 1; i < hb.getFate(); ++i) {
				StateupConf sc = Readonly.getInstance().findStateup(i);
				fengyin += sc.cost;
			}

			fengyin -= hb.getFateCost();
			hb.setFateCost(0);

			rci.returns.addAll(user.addItem(金币, gold, false, "分解英雄:" + hi.name));
			rci.returns.addAll(user.addHero(hb.getId(), yingxiong, "分解英雄:" + hi.name));
			rci.returns.addAll(user.addItem(封印石, fengyin, false, "分解英雄:" + hi.name));
			rci.returns.addAll(user.addItem(突破石, tupo, false, "分解英雄:" + hi.name));
			rci.returns.addAll(user.addItem(突破玉, tupoyu, false, "分解英雄:" + hi.name));
			rci.returns.addAll(user.addItems(hi.soul, false, "分解英雄:" + hi.name));
			// TODO 返还部分武将精华

			pb.getHeros().remove(hid);
		}
	}
}
