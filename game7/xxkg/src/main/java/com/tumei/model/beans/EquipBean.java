package com.tumei.model.beans;

import com.tumei.dto.battle.EquipStruct;

/**
 * Created by Administrator on 2017/3/3 0003.
 *
 * 装备信息
 */
public class EquipBean {
	/**
	 * 数据id
	 */
	private int id;
	/**
	 * 指派id
	 */
	private int eid;
	/**
	 * 等级
	 */
	private int level = 1;
	/**
	 * 精炼
	 */
	private int grade;
	/**
	 * 精炼当前经验
	 */
	private int gradeexp;

	/**
	 * 觉醒等级
	 */
	private int wake;

	/**
	 * 觉醒等级对应的经验
	 */
	private int wexp;

	/**
	 * 强化暴击的返还
	 */
	private long fee;

	public EquipBean() {}

	public EquipBean(int _id) { id = _id; }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEid() {
		return eid;
	}

	public void setEid(int eid) {
		this.eid = eid;
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

	public int getGradeexp() {
		return gradeexp;
	}

	public void setGradeexp(int gradeexp) {
		this.gradeexp = gradeexp;
	}

	public long getFee() {
		return fee;
	}

	public void setFee(long fee) {
		this.fee = fee;
	}

	public int getWake() {
		return wake;
	}

	public void setWake(int wake) {
		this.wake = wake;
	}

	public int getWexp() {
		return wexp;
	}

	public void setWexp(int wexp) {
		this.wexp = wexp;
	}

	public boolean isInitStatus() {
		if (level != 1 || grade != 0 || gradeexp != 0 || wake != 0 || wexp != 0) {
			return false;
		}
		return true;
	}

	public EquipStruct createEquipStruct() {
		EquipStruct es = new EquipStruct();
		es.id = getId();
		es.level = getLevel();
		es.grade = getGrade();
		es.wake = getWake();
		return es;
	}

	public EquipBean clone() {
		EquipBean eb = new EquipBean();

		eb.id = id;
		eb.eid = eid;
		eb.level = level;
		eb.grade = grade;
		eb.gradeexp = gradeexp;
		eb.wake = wake;
		eb.wexp = wexp;

		return eb;
	}

	@Override
	public String toString() {
		return "EquipBean{" + "id=" + id + ", eid=" + eid + ", level=" + level + ", grade=" + grade + ", gradeexp=" + gradeexp + ", wake=" + wake + ", wexp=" + wexp + ", fee=" + fee + '}';
	}
}
