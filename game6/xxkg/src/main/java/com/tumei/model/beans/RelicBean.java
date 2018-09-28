package com.tumei.model.beans;

import com.tumei.common.fight.RelicStruct;

/**
 * Created by Leon on 2018/3/20.
 */
public class RelicBean {
	/**
	 * 圣物id
	 */
	public int id;

	public int star;

	public int exp;

	public int level = 1;

	// 4个英雄 对应12个属性
	public int[] attrs = new int[12];

	// 传奇英雄id
	public int hero;
	// 传奇英雄等级
	public int hlvl = 1;
	// 觉醒等级
	public int hwlvl;
	// 炼化次数
	public int count;

	public RelicBean() { }

	public RelicBean(int _id) {
		id = _id;
	}

	public RelicStruct createRelicStruct() {
		RelicStruct es = new RelicStruct();
		es.id = this.id;
		es.star = this.star;
		es.exp = this.exp;
		es.level = this.level;
		for (int i = 0; i < 12; ++i) {
			es.attrs[i] = this.attrs[i];
		}
		es.hero = this.hero;
		es.hlvl = this.hlvl;
		es.hwlvl = this.hwlvl;

		return es;
	}
}
