package com.tumei.common.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/20 0020.
 *
 * 战斗信息
 *
 */
public class SceneFightStruct {
	private long uid;

	private List<HeroStruct> left = new ArrayList<HeroStruct>();

	private List<DirectHeroStruct> right = new ArrayList<DirectHeroStruct>();

	/**
	 * 战斗的时候，有临时提升的buff
	 */
	private Map<Integer, Integer> buffs = new HashMap<Integer, Integer>();

	private List<ArtifactStruct> arts = new ArrayList<>();

	private int[] lineups = new int[6];

	private int condition;

	public SceneFightStruct() {}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public List<HeroStruct> getLeft() {
		return left;
	}

	public void setLeft(List<HeroStruct> left) {
		this.left = left;
	}

	public List<DirectHeroStruct> getRight() {
		return right;
	}

	public void setRight(List<DirectHeroStruct> right) {
		this.right = right;
	}

	public Map<Integer, Integer> getBuffs() {
		return buffs;
	}

	public void setBuffs(Map<Integer, Integer> buffs) {
		this.buffs = buffs;
	}

	public int[] getLineups() {
		return lineups;
	}

	public void setLineups(int[] lineups) {
		this.lineups = lineups;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public List<ArtifactStruct> getArts() {
		return arts;
	}

	public void setArts(List<ArtifactStruct> arts) {
		this.arts = arts;
	}
}
