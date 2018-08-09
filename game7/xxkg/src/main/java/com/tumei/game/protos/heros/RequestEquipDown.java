package com.tumei.game.protos.heros;

import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.modelconf.EquaddConf;
import com.tumei.modelconf.EquipcostConf;
import com.tumei.modelconf.EquiprefcostConf;
import com.tumei.modelconf.ItemConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级装备
 */
@Component
public class RequestEquipDown extends BaseProtocol {
	public int seq;

	/**
	 * 装备在背包中的唯一id
	 */
	public List<Integer> eids;

	class ReturnEquipDown extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnEquipDown rci = new ReturnEquipDown();
		rci.seq = seq;

		long uid = user.getUid();
		PackBean pb = user.getDao().findPack(uid);

		for (int eid : eids) {
			EquipBean eb = pb.getEquips().get(eid);
			if (Defs.isTreasureID(eb.getId())) {
				continue;
			}

			ItemConf ic = Readonly.getInstance().findItem(eb.getId());
			pb.payTreasure(eid, "分解装备:" + ic.good);

			// 1 返回装备对应的声望
			rci.awards.addAll(user.addItem(ic.price[0], ic.price[1], false, "分解装备:" + ic.good));

			// 2 返还强化使用的金币
			long gold = 0;
			for (int i = 1; i < eb.getLevel(); ++i) {
				EquipcostConf ecc = Readonly.getInstance().findEquipCost(i);

				switch (ic.quality) {
					case 1:
						gold += ecc.gr;
						break;
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
			// 返回金币
			if (gold > 0) {
				gold -= eb.getFee();
				eb.setFee(0);
				rci.awards.addAll(user.addItem(金币, gold, false, "分解装备:" + ic.good));
			}

			// 3 返还精炼过程中使用的金币
			int count = 0;//eb.getGradeexp();
			for (int i = 0; i < eb.getGrade(); ++i) {
				EquiprefcostConf ecc = Readonly.getInstance().findEquipRefCost(i);

				switch (ic.quality) {
					case 1:
						count += ecc.gr;
						break;
					case 2:
						count += ecc.bl;
						break;
					case 3:
						count += ecc.pu;
						break;
					case 4:
						count += ecc.or;
						break;
					case 5:
						count += ecc.re;
						break;
				}
			}

			int a1 = 0;
			int a2 = 0;
			int a3 = 0;
			int a4 = 0;

			while (count > 0) {
				if (count >= 50) {
					++a1;
					count -= 50;
				}
				else if (count >= 25) {
					++a2;
					count -= 25;
				}
				else if (count >= 10) {
					++a3;
					count -= 10;
				}
				else {
					++a4;
					count -= 5;
				}
			}

			if (a1 > 0) {
				rci.awards.addAll(user.addItem(极品精炼石, a1, false, "分解装备"));
			}
			if (a2 > 0) {
				rci.awards.addAll(user.addItem(高级精炼石, a2, false, "分解装备"));
			}
			if (a3 > 0) {
				rci.awards.addAll(user.addItem(中级精炼石, a3, false, "分解装备"));
			}
			if (a4 > 0) {
				rci.awards.addAll(user.addItem(初级精炼石, a4, false, "分解装备"));
			}

			if (eb.getWake() > 0) {
				EquaddConf eac = Readonly.getInstance().findEquipaddConf(eb.getWake());
				if (eac != null) {
					if (ic.quality == 4) {
						rci.awards.addAll(user.addItems(eac.orange, "分解装备"));
					} else if (ic.quality == 5) {
						rci.awards.addAll(user.addItems(eac.red, "分解装备"));
					}
				}
			}

		}

		user.send(rci);
	}
}
