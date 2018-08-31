package com.tumei.common.fight;

import java.util.*;

/**
 * Created by Administrator on 2017/2/20 0020.
 *
 * 战斗信息
 *
 */
public class GroupFightStruct {
	private long uid;
	private List<HeroStruct> left = new ArrayList<HeroStruct>();
	private List<DirectHeroStruct> right = new ArrayList<DirectHeroStruct>();

	/**
	 * 战斗的时候，有临时提升的buff
	 */
	private Map<Integer, Integer> buffs = new HashMap<Integer, Integer>();
	private List<ArtifactStruct> arts = new ArrayList<>();
	private int[] lineups = new int[6];

	public GroupFightStruct() {}

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

	public void setRightArray(DirectHeroStruct[] _right) {
		Collections.addAll(this.right, _right);
	}

	public Map<Integer, Integer> getBuffs() {
		return buffs;
	}

	public void setBuffs(Map<Integer, Integer> buffs) {
		this.buffs = buffs;
	}

	public List<ArtifactStruct> getArts() {
		return arts;
	}

	public void setArts(List<ArtifactStruct> arts) {
		this.arts = arts;
	}

	public int[] getLineups() {
		return lineups;
	}

	public void setLineups(int[] lineups) {
		this.lineups = lineups;
	}
}
