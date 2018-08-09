package com.tumei.game.protos.heros;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级英雄
 */
@Component
public class RequestHeroAdjust extends BaseProtocol {
	public int seq;
	/**
	 * rmb
	 * 0: 表示从背包中添加英雄到战队中，此时src是背包中的英雄的hid, dst是目的索引[1,6] 六个位置, 有可能dst位置上有英雄，此时就将其
	 * 交换下来，放到背包中。不会有单独卸载到背包的情况，不符合策划案需求。
	 * 1: 表示战队中的2个英雄互相交换: 此时src与dst都是表示[1,6]的位置索引，保证此时2个位置都有英雄，不可能有无英雄的情况，否则战队
	 * 阵形会出现空洞，此不符合策划案需求。
	 *
	 * 注意:
	 * 同id的英雄不能有2个或以上存在战队中
	 * 领主英雄不能交换到背包中
	 *
	 * 援军操作，与上面的战队操作不同
	 * 2: 援军增加: src是背包中的英雄hid，dst是援军的位置[1,6]
	 * 3: 援军卸载: src是援军的位置[1,6]
	 */
	public int mode;
	public int src;
	public int dst;

	/**
	 * 上阵人数等级，从1个开始
	 */
	@JsonIgnore
	private static int[] levels = new int[]{5, 9, 12, 23, 30, 40, 50, 60, 70, 80, 90};

	class ReturnHeroAdjust extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<Integer> eids = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnHeroAdjust rci = new ReturnHeroAdjust();
		rci.seq = seq;

		long uid = user.getUid();
		HerosBean hsb = user.getDao().findHeros(uid);

		{
			if (mode == 0) { // 与背包进行交流
				--dst;
				HeroBean[] hbs = hsb.getHeros();

				PackBean pb = user.getDao().findPack(uid);
				HeroBean tmp = pb.findHero(src);
				if (tmp == null) {
					rci.result = "您没有英雄:[" + src + "].";
				}
				else {
					HeroBean dt = hbs[dst];

					// 助战中不能用
					if (Arrays.stream(hsb.getAssists()).filter(hb -> (hb != null && hb.getId() == tmp.getId())).count() > 0) {
						rci.result = "上阵的英雄已经存在于助战英雄列表中";
						user.send(rci);
						return;
					}

					if (Arrays.stream(hbs).filter(hb -> (hb != null && (hb.getId() == tmp.getId()) && (dt != hb))).count() > 0) {
						rci.result = "无法上阵已存在的英雄";
						user.send(rci);
						return;
					}

					if (dt != null) {
						if (dt.isLord()) {
							rci.result = "领主英雄必须在场";
							user.send(rci);
							return;
						}

						// 英雄放回到背包中的时候，需要将装备信息留下
						EquipBean[] eqs = tmp.getEquips();
						tmp.setEquips(dt.getEquips());
						dt.setEquips(eqs);
						pb.returnHero(dt);
					}
					else {
						// 纯增加英雄需要判断当前英雄等级对应的个数
						RoleBean rb = user.getDao().findRole(uid);
						int level = rb.getLevel();
						int count = 1;
						for (int lv : levels) {
							if (level >= lv) {
								++count;
							}
							else {
								break;
							}
						}

						if (Arrays.stream(hbs).filter(hb -> hb != null).count() >= count) {
							rci.result = ErrCode.战队等级不足.name();
							user.send(rci);
							return;
						}

						// 相当于初次开启的位置，初次上阵英雄，可以有一套绿色装备
						EquipBean[] ebs = tmp.getEquips();

						ebs[0] = pb.buildEquip(10010);
						ebs[1] = pb.buildEquip(10020);
						ebs[2] = pb.buildEquip(10030);
						ebs[3] = pb.buildEquip(10040);

						rci.eids.add(ebs[0].getEid());
						rci.eids.add(ebs[1].getEid());
						rci.eids.add(ebs[2].getEid());
						rci.eids.add(ebs[3].getEid());
					}
					hbs[dst] = tmp;
					pb.removeHero(src);
				}
			}
			else if (mode == 1) { // 无背包，战队之间的交换
				--src;
				--dst;
				HeroBean[] hbs = hsb.getHeros();
				if (hbs[src] == null || hbs[dst] == null) {
					rci.result = "英雄操作非法，不能交换空位";
				}
				else {
					HeroBean tmp = hbs[src];
					hbs[src] = hbs[dst];
					hbs[dst] = tmp;
				}
			}
			else if (mode == 2) { // 增加援军
				--dst;

//				if (Arrays.stream(hsb.getAssists()).filter(hb -> hb != null).count() < 6) {
//					rci.result = "战队等级不足，无法上阵英雄";
//					user.send(rci);
//					return;
//				}

				HeroBean[] hbs = hsb.getAssists();
				PackBean pb = user.getDao().findPack(uid);
				HeroBean tmp = pb.getHeros().getOrDefault(src, null);
				if (tmp == null) {
					rci.result = "您没有英雄:[" + src + "].";
				}
				else {
					HeroBean dt = hbs[dst];

					// 助战中不能用
					if (Arrays.stream(hsb.getHeros()).filter(hb -> (hb != null && hb.getId() == tmp.getId())).count() > 0) {
						rci.result = "上阵的英雄已经存在于英雄战队中";
						user.send(rci);
						return;
					}

					if (Arrays.stream(hbs).filter(hb -> (hb != null && (hb.getId() == tmp.getId()) && (dt != hb))).count() > 0) {
						rci.result = "无法使用已存在的英雄助战";
						user.send(rci);
						return;
					}


					if (dt != null) {
						pb.getHeros().put(dt.getHid(), dt);
					}
					else {
						RoleBean rb = user.getDao().findRole(uid);
						int level = rb.getLevel();
						int count = 1;
						for (int lv : levels) {
							if (level >= lv) {
								++count;
							}
							else {
								break;
							}
						}

						count -= 6;

						if (Arrays.stream(hbs).filter(hb -> hb != null).count() >= count) {
							rci.result = ErrCode.战队等级不足.name();
							user.send(rci);
							return;
						}
					}
					hbs[dst] = tmp;
					pb.getHeros().remove(src);
				}
			}
			else if (mode == 3) { // 卸载援军
				--src;
				HeroBean[] hbs = hsb.getAssists();
				if (hbs[src] != null) {
					HeroBean tmp = hbs[src];
					PackBean pb = user.getDao().findPack(uid);
					pb.getHeros().put(tmp.getHid(), tmp);
					hbs[src] = null;
				}
			}
		}

		user.send(rci);
	}
}
