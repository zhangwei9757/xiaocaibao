package com.tumei.game.protos.structs;

import com.tumei.common.utils.Defs;
import com.tumei.model.RankBean;
import com.tumei.model.beans.HeroBean;

/**
 * Created by Administrator on 2017/3/6 0006.
 *
 *
 */
public class RankStruct {
	public Long id;
	/**
	 * 排名
	 */
	public int rank;
	/**
	 * 名字
	 */
	public String name = "";
	/**
	 * 头像
	 */
	public int icon;
	/**
	 * 战斗力
	 */
	public long power;
	/**
	 * 等级
	 */
	public int level;

	public HeroPowerStruct[] formation = new HeroPowerStruct[6];

	public RankStruct() {}

	public RankStruct(RankBean rb) {
		id = rb.getId();
		rank = rb.getRank();
		name = rb.getName();
		icon = rb.getIcon();
		power = rb.getPower();
		level = rb.getLevel();

		int i = 0;
		for (HeroBean hb : rb.getFormation()) {
			if (hb != null) {
				formation[i] = new HeroPowerStruct(hb);
				if (Defs.isLordID(hb.getId())) {
					formation[i].skin = rb.getFashion();
				}
			}
			++i;
		}
	}
}
