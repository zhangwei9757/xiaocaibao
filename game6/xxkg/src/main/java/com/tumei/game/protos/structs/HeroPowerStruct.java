package com.tumei.game.protos.structs;

import com.tumei.common.fight.EquipStruct;
import com.tumei.model.beans.HeroBean;
import com.tumei.model.beans.EquipBean;

/**
 * Created by Administrator on 2017/3/6 0006.
 */
public class HeroPowerStruct {
	public int id;
	public int level;
	public int grade;
	public int skin;
	public EquipStruct[] equips = new EquipStruct[6];

	public HeroPowerStruct() {}
	public HeroPowerStruct(HeroBean hb) {
		id = hb.getId();
		level = hb.getLevel();
		grade = hb.getGrade();

		if (!hb.isLord() && hb.getGift() >= 26) {
			skin = 1;
		}

		EquipBean[] ebs = hb.getEquips();
		for (int i = 0; i < ebs.length; ++i) {
			if (ebs[i] != null) {
				equips[i] = ebs[i].createEquipStruct();
			}
		}
	}
}
