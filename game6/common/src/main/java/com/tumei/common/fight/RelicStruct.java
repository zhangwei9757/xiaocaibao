package com.tumei.common.fight;

/**
 * Created by Leon on 2018/3/20.
 */
public class RelicStruct {
	/**
	 * 圣物id
	 */
	public int id;

	public int star;

	public int exp;

	public int level;

	// 4个英雄 对应12个属性
	public int[] attrs = new int[12];

	// 传奇英雄id
	public int hero;
	// 传奇英雄等级
	public int hlvl;
	// 技能等级
	public int hslvl;
	// 觉醒等级
	public int hwlvl;

	public RelicStruct() {}

	public RelicStruct(RelicStruct es) {
		this.id = es.id;
		this.star = es.star;
		this.exp = es.exp;
		this.level = es.level;
		for (int i = 0; i < 12; ++i) {
			this.attrs[i] = es.attrs[i];
		}
		this.hero = es.hero;
		this.hlvl = es.hlvl;
		this.hslvl = es.hslvl;
		this.hwlvl = es.hwlvl;
	}
}
