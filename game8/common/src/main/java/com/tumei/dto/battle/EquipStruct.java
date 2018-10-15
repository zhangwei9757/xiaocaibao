package com.tumei.dto.battle;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by Administrator on 2017/2/20 0020.
 */
public class EquipStruct {
	/**
	 * 装备id
	 */
	@Field("id")
	public int id;

	/**
	 * 强化等级
	 */
	public int level = 1;

	/**
	 * 精炼等级
	 */
	public int grade;

	/**
	 * 觉醒等级
	 */
	public int wake;

	public EquipStruct() {}

	public EquipStruct(EquipStruct es) {
		this.id = es.id;
		this.level = es.level;
		this.grade = es.grade;
		this.wake = es.wake;
	}

	@Override
	public String toString() {
		return "EquipStruct{" + "id=" + id + ", level=" + level + ", grade=" + grade + ", wake=" + wake + '}';
	}
}
