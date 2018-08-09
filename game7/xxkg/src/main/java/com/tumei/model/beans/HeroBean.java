package com.tumei.model.beans;

import com.tumei.dto.battle.EquipStruct;
import com.tumei.dto.battle.HeroStruct;
import com.tumei.common.utils.Defs;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/3/3 0003.
 *
 * 英雄信息
 *
 */
public class HeroBean {
	/**
	 * 数据id
	 */
	private int id;
	/**
	 * 自增指派id
	 */
	private int hid;
	/**
	 * 等级
	 */
	private int level = 1;
	/**
	 * 突破
	 */
	private int grade;
	/**
	 * 天命
	 */
	private int fate = 1;
	/**
	 * 天命当前进度
	 */
	private int fateexp = 0;
	/**
	 * 觉醒
	 */
	private int gift = 1;
	/**
	 * 在某一个觉醒等级时，当前添加的符文
	 */
	private int[] giftrunes = new int[4];

	private EquipBean[] equips = new EquipBean[6];

	/**
	 * 封印石直升的补偿
	 */
	private int fateCost = 0;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getHid() {
		return hid;
	}

	public void setHid(int hid) {
		this.hid = hid;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public int getFate() {
		return fate;
	}

	public void setFate(int fate) {
		this.fate = fate;
	}

	public int getGift() {
		return gift;
	}

	public void setGift(int gift) {
		this.gift = gift;
	}

	public EquipBean[] getEquips() {
		return equips;
	}

	public void setEquips(EquipBean[] equips) {
		this.equips = equips;
	}

	public int getFateexp() {
		return fateexp;
	}

	public void setFateexp(int fateexp) {
		this.fateexp = fateexp;
	}

	public int[] getGiftrunes() {
		return giftrunes;
	}

	public void setGiftrunes(int[] giftrunes) {
		this.giftrunes = giftrunes;
	}

	public int getFateCost() {
		return fateCost;
	}

	public void setFateCost(int fateCost) {
		this.fateCost = fateCost;
	}

	public boolean isInitStatus() {
		if (Arrays.stream(giftrunes).anyMatch(r -> (r != 0))) {
			return false;
		}
		return (level == 1 && grade == 0 && fate == 1 && gift == 1);
	}

	@Override
	public String toString() {
		return "HeroBean{" + "id=" + id + ", hid=" + hid + ", level=" + level + ", grade=" + grade + ", fate=" + fate + ", fateexp=" + fateexp + ", gift=" + gift + ", giftrunes=" + Arrays.toString(giftrunes) + ", equips=" + Arrays.toString(equips) + ", fateCost=" + fateCost + '}';
	}

	/**
	 * 重置英雄
	 */
	public void reset() {
		level = 1;
		grade = 0;
		fate = 1;
		gift = 1;
		for (int i = 0; i < 4; ++i) {
			giftrunes[i] = 0;
		}
	}

	/**
	 * 是否领主英雄
	 *
	 * @return
	 */
	public boolean isLord() {
		return Defs.isLordID(this.id);
	}

	/***
	 * 根据英雄变量创建 传送到 fighter服务器的结构
	 *
	 * @return
	 */
	public HeroStruct createHeroStruct() {
		HeroStruct hs = new HeroStruct();
		hs.hero = getId();
		hs.level = getLevel();
		hs.grade = getGrade();
		hs.fate = getFate();
		hs.gift = getGift();
		hs.giftrunes = getGiftrunes();

		EquipBean[] ebs = getEquips();
		for (int i = 0; i < ebs.length; ++i) {
			EquipBean eb = ebs[i];
			if (eb != null) {
				EquipStruct es = eb.createEquipStruct();
				hs.equipStructs.add(es);
			} else {
				hs.equipStructs.add(null);
			}
		}

		return hs;
	}

	/**
	 * 复制
	 *
	 * @return
	 */
	public HeroBean clone() {
		HeroBean ot = new HeroBean();

		ot.id = id;
		ot.hid = hid;
		ot.level = level;
		ot.grade = grade;
		ot.fate = fate;
		ot.fateexp = fateexp;
		ot.gift = gift;
		ot.giftrunes = Arrays.copyOf(giftrunes, giftrunes.length);

		for (int i = 0; i < 6; ++i) {
			if (equips[i] != null) {
				ot.equips[i] = equips[i].clone();
			}
		}

		return ot;
	}
}
