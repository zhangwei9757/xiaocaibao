package com.tumei.model.beans;

import com.tumei.common.fight.ArtcomStruct;

/**
 * Created by Administrator on 2017/3/3 0003.
 *
 * 神器部件信息
 */
public class ArtifactComBean {
	/**
	 * 强化等级
	 */
	private int level = 1;

	/**
	 * 升星等级, 480个进度， 从0开始
	 */
	private int star = 0;

	public ArtifactComBean() {}

	public ArtcomStruct createStruct(int id) {
		ArtcomStruct as = new ArtcomStruct();
		as.id = id;
		as.level = this.level;
		as.star = this.star;
		return as;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	@Override
	public String toString() {
		return "ArtifactComBean{" + "level=" + level + ", star=" + star + '}';
	}
}
