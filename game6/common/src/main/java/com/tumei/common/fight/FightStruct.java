package com.tumei.common.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/20 0020.
 * <p>
 * 请求模拟战斗的时候传入的双方数据
 */
public class FightStruct {
	private long uid;
	private List<HeroStruct> left = new ArrayList<HeroStruct>();
	private List<HeroStruct> right = new ArrayList<HeroStruct>();

	private Map<Integer, Integer> buffs = new HashMap<Integer, Integer>();
	private int[] lineups = new int[6];

	private Map<Integer, Integer> buffs2 = new HashMap<Integer, Integer>();
	private int[] lineups2 = new int[6];

	private List<ArtifactStruct> arts1 = new ArrayList<>();
	private List<ArtifactStruct> arts2 = new ArrayList<>();

	// 衰弱比例, 天梯赛中第一次遇到
	private int weak;

	// 胜利条件，远征
	private int condition;

	public FightStruct() { }

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
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

	public Map<Integer, Integer> getBuffs2() {
		return buffs2;
	}

	public void setBuffs2(Map<Integer, Integer> buffs2) {
		this.buffs2 = buffs2;
	}

	public int[] getLineups2() {
		return lineups2;
	}

	public void setLineups2(int[] lineups2) {
		this.lineups2 = lineups2;
	}

	public List<HeroStruct> getLeft() {
		return left;
	}

	public void setLeft(List<HeroStruct> left) {
		this.left = left;
	}

	public List<HeroStruct> getRight() {
		return right;
	}

	public void setRight(List<HeroStruct> right) {
		this.right = right;
	}

	public int getWeak() {
		return weak;
	}

	public void setWeak(int weak) {
		this.weak = weak;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public List<ArtifactStruct> getArts1() {
		return arts1;
	}

	public void setArts1(List<ArtifactStruct> arts1) {
		this.arts1 = arts1;
	}

	public List<ArtifactStruct> getArts2() {
		return arts2;
	}

	public void setArts2(List<ArtifactStruct> arts2) {
		this.arts2 = arts2;
	}
}
