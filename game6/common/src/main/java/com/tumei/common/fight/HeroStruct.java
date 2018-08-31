package com.tumei.common.fight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/2/20 0020.
 * <p>
 * 战斗中的英雄
 */
public class HeroStruct {
	/**
	 * 英雄id, 真实id
	 */
	public int hero;
	/**
	 * 领主可以使用时装,变身成其他角色, 0表示没有时装
	 */
	public int skin;
	/**
	 * 皮肤等级
	 */
	public int skinLevel = 1;
	/**
	 * 英雄等级
	 */
	public int level = 1;
	/**
	 * 英雄突破
	 */
	public int grade = 0;
	/**
	 * 天命等级
	 */
	public int fate = 1;
	/**
	 * 觉醒等级: 部分英雄具有
	 */
	public int gift = 0;

	public int[] giftrunes = {0, 0, 0, 0};

	public boolean assist;

	/**
	 * 装备与宝物
	 */
	public List<EquipStruct> equipStructs = new ArrayList<>();

	public HeroStruct() {
	}

	public HeroStruct(int hid) {
		hero = hid;
	}

	public HeroStruct(HeroStruct hs) {
		this.hero = hs.hero;
		this.skin = hs.skin;
		this.skinLevel = hs.skinLevel;
		this.level = hs.level;
		this.grade = hs.grade;
		this.fate = hs.fate;
		this.gift = hs.gift;
		this.giftrunes = hs.giftrunes;
		this.assist = hs.assist;
		hs.equipStructs.forEach((es) -> {
			if (es != null) {
				this.equipStructs.add(new EquipStruct(es));
			} else {
				this.equipStructs.add(null);
			}
		});
	}

	@Override
	public String toString() {
		return "HeroStruct{" + "hero=" + hero + ", skin=" + skin + ", skinLevel=" + skinLevel + ", level=" + level + ", grade=" + grade + ", fate=" + fate + ", gift=" + gift + ", giftrunes=" + Arrays.toString(giftrunes) + ", assist=" + assist + ", equipStructs=" + equipStructs + '}';
	}
}
