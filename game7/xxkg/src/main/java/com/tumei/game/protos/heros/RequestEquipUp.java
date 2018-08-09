package com.tumei.game.protos.heros;

import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.modelconf.EquipcostConf;
import com.tumei.modelconf.EquiprefcostConf;
import com.tumei.modelconf.ItemConf;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.金币;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级装备
 */
@Component
public class RequestEquipUp extends BaseProtocol {
	public int seq;
	/**
	 * rmb
	 * 0: 装备强化
	 * 1: 装备精炼
	 * 2: 一键强化
	 */
	public int mode;
	/**
	 * 英雄位置 [1,6]表示英雄
	 * <p>
	 * index == 0 表示 非背包中的英雄
	 */
	public int index;
	/**
	 * 装备位置 [1,6]装备
	 * <p>
	 * index == 0 时 equip是eid
	 */
	public int equip;
	/**
	 * 精炼的时候选择进行精炼的物品.
	 */
	public int item;

	class ReturnEquipUp extends BaseProtocol {
		public int seq;
		public String result = "";
		/**
		 * 消耗物品类型
		 */
		public int pricetype;
		/**
		 * 消耗物品数量
		 */
		public long price;
		/**
		 * 暴击倍率
		 */
		public int ratio;
		/**
		 * 精炼返回当前精炼等级
		 */
		public int grade;
		/**
		 * 返回当前精炼经验值
		 */
		public int gradeexp;

		/**
		 * 返回一键强化的次数
		 */
		public int count;

		// 返回客户端节约的总金币数
		public long fee;
	}

	public int getCost(int quality, int level) {
		EquipcostConf ecc = Readonly.getInstance().findEquipCost(level);
		int gold = 0;
		if (ecc != null) {
			switch (quality) {
				case 1:
					gold = ecc.gr;
					break;
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
		return gold;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnEquipUp rci = new ReturnEquipUp();
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

		if (Defs.isTreasureID(eb.getId())) {
			rci.result = "宝物无法使用本协议强化";
			user.send(rci);
			return;
		}

		ItemConf ic = Readonly.getInstance().findItem(eb.getId());

		if (mode == 0) {
			RoleBean rb = user.getDao().findRole(uid);
			int limit = rb.getLevel() * 2;
			if (eb.getLevel() >= limit) {
				rci.result = "强化等级达到上限";
				user.send(rci);
				return;
			}
			int gold = getCost(ic.quality, eb.getLevel());

			if (gold > 0) {
				if (!pb.contains(金币, gold)) {
					rci.result = ErrCode.金币不足.name();
					user.send(rci);
					return;
				}

				user.payItem(金币, gold, "装备强化:" + ic.good);
				rci.pricetype = 金币;
				rci.price = gold;

				// 根据vip计算暴击机率
				VipConf vc = Readonly.getInstance().findVip(rb.getVip());

				int r = RandomUtil.getRandom() % 100;
				if (r < vc.critrate) {
					rci.ratio = 2;
					int left = limit - eb.getLevel();
					if (rci.ratio > left) {
						rci.ratio = left;
					}
				}
				else if (r < vc.criteff) {
					rci.ratio = 3;
					int left = limit - eb.getLevel();
					if (rci.ratio > left) {
						rci.ratio = left;
					}
				}
				else {
					rci.ratio = 1;
				}

				long fee = 0;
				for (int i = 1; i < rci.ratio; ++i) {
					fee += getCost(ic.quality, eb.getLevel() + i);
				}
				if (fee > 0) {
					eb.setFee(eb.getFee() + fee);
				}

				rci.fee = eb.getFee();
				eb.setLevel(eb.getLevel() + rci.ratio);
				user.pushDailyTask(7, 1);
			} else {
				rci.result = "强化等级达到上限";
				user.send(rci);
				return;
			}
		}
		else if (mode == 1) {
			// 所需材料
			if (item < 30 || item > 33) {
				rci.result = ErrCode.未知参数.name();
				user.send(rci);
				return;
			}

			EquiprefcostConf ecc = Readonly.getInstance().findEquipRefCost(eb.getGrade());

			int count = 0;
			if (ecc != null) {
				switch (ic.quality) {
					case 1:
						count = ecc.gr;
						break;
					case 2:
						count = ecc.bl;
						break;
					case 3:
						count = ecc.pu;
						break;
					case 4:
						count = ecc.or;
						break;
					case 5:
						count = ecc.re;
						break;
				}
			}

			if (count <= 0) {
				rci.result = "强化等级达到上限";
				user.send(rci);
				return;
			}

			count -= eb.getGradeexp();

			if (count > 0) {
				int per = 5;
				switch (item) {
					case 30:
						per = 5;
						break;
					case 31:
						per = 10;
						break;
					case 32:
						per = 25;
						break;
					case 33:
						per = 50;
						break;
				}

				int point = pb.getItemCount(item);

				// 精炼石个数
				int itemCount = pb.getItemCount(item);

				for (int i = 0; i < point; ++i) {
					count -= per;
					eb.setGradeexp(eb.getGradeexp() + per);
					rci.price += 1;

					if (count <= 0) {
						// 升级
						eb.setGrade(eb.getGrade() + 1);
						eb.setGradeexp(-count);
						break;
					}

					if (rci.price >= itemCount) {
						break;
					}
				}

				if (rci.price > 0) {
					user.payItem(item, rci.price, "装备精炼:" + ic.good);
				}

				// 检查当前的经验是否足够支持再次升级
				while (true) {
					ecc = Readonly.getInstance().findEquipRefCost(eb.getGrade());
					count = 0;
					if (ecc != null) {
						switch (ic.quality) {
							case 1:
								count = ecc.gr;
								break;
							case 2:
								count = ecc.bl;
								break;
							case 3:
								count = ecc.pu;
								break;
							case 4:
								count = ecc.or;
								break;
							case 5:
								count = ecc.re;
								break;
						}
					}

					if (count <= 0 || eb.getGradeexp() < count) {
						break;
					}

					eb.setGradeexp(eb.getGradeexp() - count);
					eb.setGrade(eb.getGrade() + 1);
				}

				rci.pricetype = item;
				rci.grade = eb.getGrade();
				rci.gradeexp = eb.getGradeexp();
			}
		}
		else if (mode == 2) { // 一键强化
			RoleBean rb = user.getDao().findRole(uid);
			int limit = rb.getLevel() * 2;
			if (eb.getLevel() >= limit) {
				rci.result = "强化等级达到上限";
				user.send(rci);
				return;
			}
			rci.pricetype = 金币;
//			user.info("一键强化之前金币: " + pb.getCoin());

			int pdt = 0;
			VipConf vc = Readonly.getInstance().findVip(rb.getVip());
			long goldHas = pb.getCoin();

			while (true) {
				if (eb.getLevel() >= limit) {
					break;
				}

				long gold = getCost(ic.quality, eb.getLevel());
				if (gold > 0) {
					if ((rci.price + gold) > goldHas) {
						break;
					}
					rci.price += gold;

					// 根据vip计算暴击机率

					int r = RandomUtil.getRandom() % 100;
					int e = 1;
					if (r < vc.critrate) {
						e = 2;
						int left = limit - eb.getLevel();
						if (left < e) {
							e = left;
						}
					}
					else if (r < vc.criteff) {
						e = 3;
						int left = limit - eb.getLevel();
						if (left < e) {
							e = left;
						}
					}

					long fee = 0;
					for (int i = 1; i < e; ++i) {
						fee += getCost(ic.quality, eb.getLevel() + i);
					}
					if (fee > 0) {
						eb.setFee(eb.getFee() + fee);
					}

					int ee = e + eb.getLevel();
					if (ee > limit) {
						ee = limit;
					}

					eb.setLevel(ee);
					++pdt;
					rci.count += 1;
				}
			}

			rci.fee = eb.getFee();
			if (rci.price > 0) {
				user.payItem(金币, rci.price, "装备一键:" + ic.good);
			}
			user.pushDailyTask(7, pdt);
			rci.grade = eb.getLevel();
//			user.info("一键强化最终使用金币: " + rci.price);
		}
		user.send(rci);
	}
}
