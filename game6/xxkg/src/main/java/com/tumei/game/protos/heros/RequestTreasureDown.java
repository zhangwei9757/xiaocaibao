package com.tumei.game.protos.heros;

import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.modelconf.ItemConf;
import com.tumei.common.Readonly;
import com.tumei.modelconf.TreasurecostConf;
import com.tumei.modelconf.TreasurerefcostConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import com.tumei.common.utils.Defs;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级装备
 */
@Component
public class RequestTreasureDown extends BaseProtocol {
	public int seq;

	/**
	 * 装备在背包中的唯一id
	 */
	public int eid;

	class ReturnTreasureDown extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnTreasureDown rci = new ReturnTreasureDown();
		rci.seq = seq;

		long uid = user.getUid();
		PackBean pb = user.getDao().findPack(uid);

		EquipBean eb = pb.getEquips().get(eid);
		if (Defs.isEquipID(eb.getId())) {
			rci.result = "装备无法重生";
			user.send(rci);
			return;
		}

		if (!pb.contains(钻石, 50)) {
			rci.result = ErrCode.钻石不足.name();
			user.send(rci);
			return;
		}

		ItemConf ic = Readonly.getInstance().findItem(eb.getId());

		// 1 返还强化使用的金币
		int gold = 0;//eb.getGradeexp();
		for (int i = 1; i < eb.getLevel(); ++i) {
			TreasurecostConf ecc = Readonly.getInstance().findTreasureCost(i);

			switch (ic.quality) {
				case 2:
					gold += ecc.bl;
					break;
				case 3:
					gold += ecc.pu;
					break;
				case 4:
					gold += ecc.or;
					break;
				case 5:
					gold += ecc.re;
					break;
			}
		}

		user.info("返回的宝物经验:" + gold);
		// 返回宝物经验
		while (gold >= 2500) {
			if (gold >= 10000) {
				rci.awards.addAll(user.addItem(发光的宝物, 1, false, "分解宝物"));
				gold -= 10000;
			} else if (gold >= 2500) {
				rci.awards.addAll(user.addItem(粗糙的宝物, 1, false, "分解宝物"));
				gold -= 2500;
			}
		}

		// 3 返还精炼过程中的损耗
		int cost1 = 0;
		int cost2 = 0;
		int cost3 = 0;
		for (int i = 0; i < eb.getGrade(); ++i) {
			TreasurerefcostConf ecc = Readonly.getInstance().findTreasureRefCost(i);
			cost1 += ecc.cost1;
			cost2 += ecc.cost2;
			cost3 += ecc.cost3;
		}

		if (cost1 > 0) {
			rci.awards.addAll(user.addItem(宝物精炼石, cost1, false, "分解宝物"));
		}

		if (cost2 > 0) {
			rci.awards.addAll(user.addItem(金币, cost2, false, "分解宝物"));
		}
		if (cost3 > 0) {
			rci.awards.addAll(user.addItem(eb.getId(), cost3, false, "分解宝物"));
		}

		eb.setLevel(1);
		eb.setGrade(0);
		eb.setGradeexp(0);
		// 宝物重生 2500钻
		user.payItem(钻石, Defs.重生宝物, "重生宝物");

		user.send(rci);
	}
}
