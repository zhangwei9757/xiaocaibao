package com.tumei.common.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Leon on 2017/4/19 0019.
 *
 * 计算战斗力时使用的结构
 *
 */
public class PowerStruct {
	private long uid;

	/**
	 * 时装
	 */
	private int fasion;

	private List<HeroStruct> heros = new ArrayList<HeroStruct>();

	private HashMap<Integer, Integer> buffs = new HashMap<Integer, Integer>();

	private List<ArtifactStruct> arts = new ArrayList<>();

	private int[] lineups = new int[6];

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public int getFasion() {
		return fasion;
	}

	public void setFasion(int fasion) {
		this.fasion = fasion;
	}

	public List<HeroStruct> getHeros() {
		return heros;
	}

	public void setHeros(List<HeroStruct> heros) {
		this.heros = heros;
	}

	public HashMap<Integer, Integer> getBuffs() {
		return buffs;
	}

	public void setBuffs(HashMap<Integer, Integer> buffs) {
		this.buffs = buffs;
	}

	public int[] getLineups() {
		return lineups;
	}

	public void setLineups(int[] lineups) {
		this.lineups = lineups;
	}

	public List<ArtifactStruct> getArts() {
		return arts;
	}

	public void setArts(List<ArtifactStruct> arts) {
		this.arts = arts;
	}
}
