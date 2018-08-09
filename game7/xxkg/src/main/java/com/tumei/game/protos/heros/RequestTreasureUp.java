package com.tumei.game.protos.heros;

import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.modelconf.EquipConf;
import com.tumei.modelconf.ItemConf;
import com.tumei.modelconf.TreasurecostConf;
import com.tumei.modelconf.TreasurerefcostConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级装备
 */
@Component
public class RequestTreasureUp extends BaseProtocol {
	public int seq;
	/**
	 * rmb
	 * 0: 宝物升级
	 * 1: 宝物精炼
	 */
	public int mode;
	/**
	 * 英雄位置 [1,6]表示英雄
	 * <p>
	 * index == 0 表示 非背包中的英雄
	 */
	public int index;
	/**
	 * 宝物位置 [5,6] 宝物
	 * <p>
	 * index == 0 时 equip是eid
	 */
	public int equip;
	/**
	 * 强化的时候，放入的强化材料，即其他宝物
	 * 精炼的时候，传入一个原始
	 */
	public List<Integer> strongs;

	class ReturnTreasureUp extends BaseProtocol {
		public int seq;
		public String result = "";

		/**
		 * 强化返回当前精炼等级
		 */
		public int level;

		/**
		 * 返回当前强化经验值
		 */
		public int exp;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnTreasureUp rci = new ReturnTreasureUp();
		rci.seq = seq;

		long uid = user.getUid();
		PackBean pb = user.getDao().findPack(uid);

		EquipBean eb = null;
		if (index == 0) {
			eb = pb.getEquips().get(equip);
		}
		else {
			HerosBean hsb = user.getDao().findHeros(uid);
			EquipBean[] eqs = hsb.getHeros()[index - 1].getEquips();
			eb = eqs[equip - 1];
		}

		if (Defs.isEquipID(eb.getId())) {
			rci.result = "装备无法使用本协议强化";
			user.send(rci);
			return;
		}

		ItemConf ic = Readonly.getInstance().findItem(eb.getId());

		if (mode == 0) {
			int a = 0; // 粗糙
			int b = 0; // 高级
			for (int item : strongs) {
				if (item >= 0) {
					if (!pb.containsTreasure(item, 0)) {
						rci.result = "宝物强化提供的材料不存在";
						user.send(rci);
						return;
					}
				}
				else {
					if (-item == 粗糙的宝物) {
						++a;
					}
					else {
						++b;
					}
				}
			}

			if (a > 0) {
				if (!pb.contains(粗糙的宝物, a)) {
					rci.result = "宝物强化提供的材料不存在";
					user.send(rci);
					return;
				}
			}

			if (b > 0) {
				if (!pb.contains(发光的宝物, b)) {
					rci.result = "宝物强化提供的材料不存在";
					user.send(rci);
					return;
				}
			}

			int total = 0;
			for (int item : strongs) {
				if (item >= 0) {
					EquipBean eeb = pb.payTreasure(item, "强化");
					if (eeb != null) {
						EquipConf ec = Readonly.getInstance().findEquip(eeb.getId());
						total += ec.exp;
					}
				}
				else {
					item = -item;
					pb.payItem(item, 1, "强化宝物");
					EquipConf ec = Readonly.getInstance().findEquip(item);
					total += ec.exp;
				}
			}

			total += eb.getGradeexp();
			eb.setGradeexp(0);

			while (total > 0) {
				TreasurecostConf ecc = Readonly.getInstance().findTreasureCost(eb.getLevel());

				int gold = 0;
				if (ecc != null) {
					switch (ic.quality) {
//				case 1:
//					gold = ecc.gr;
//					break;
						case 2:
							gold = ecc.bl;
							break;
						case 3:
							gold = ecc.pu;
							break;
						case 4:
							gold = ecc.or;
							break;
						case 5:
							gold = ecc.re;
							break;
					}
				}

				if (gold > 0 && total >= gold) {
					eb.setLevel(eb.getLevel() + 1);
					total -= gold;
				}
				else {
					eb.setGradeexp(total);
					total = 0;
				}
			}

			rci.level = eb.getLevel();
			rci.exp = eb.getGradeexp();
			user.pushDailyTask(6, 1);
		}
		else if (mode == 1) {
			TreasurerefcostConf ecc = Readonly.getInstance().findTreasureRefCost(eb.getGrade());

			if (ecc == null || ecc.cost1 == 0) {
				rci.result = "已经达到宝物精炼上限";
				user.send(rci);
				return;
			}

			if (!pb.contains(Defs.宝物精炼石, ecc.cost1)) {
				rci.result = "缺少宝物精炼石";
				user.send(rci);
				return;
			}
			if (!pb.contains(金币, ecc.cost2)) {
				rci.result = ErrCode.金币不足.name();
				user.send(rci);
				return;
			}

			if (strongs != null) {
				if (strongs.size() != ecc.cost3) {
					rci.result = "进阶需要的宝物不足";
					user.send(rci);
					return;
				}

				for (int ii = 0; ii < strongs.size(); ++ii) {
					if (!pb.containsTreasure(strongs.get(ii), eb.getId())) {
						rci.result = "进阶需要的宝物不对";
						user.send(rci);
						return;
					}
				}
			}

			user.payItem(Defs.宝物精炼石, ecc.cost1, "宝物进阶");
			user.payItem(Defs.金币, ecc.cost2, "宝物进阶");

			if (strongs != null) {
				for (int ii = 0; ii < strongs.size(); ++ii) {
					pb.payTreasure(strongs.get(ii), "进阶");
				}
			}

			eb.setGrade(eb.getGrade() + 1);
		}

		user.send(rci);
	}
}
