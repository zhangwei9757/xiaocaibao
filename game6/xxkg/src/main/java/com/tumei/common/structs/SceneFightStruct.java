package com.tumei.common.structs;

import com.tumei.common.Readonly;
import com.tumei.common.fight.ArtifactStruct;
import com.tumei.common.fight.HeroStruct;
import com.tumei.common.fight.DirectHeroStruct;
import com.tumei.common.utils.RandomUtil;
import com.tumei.modelconf.*;

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
	private List<HeroStruct> left = new ArrayList<>();
	private List<DirectHeroStruct> right = new ArrayList<>();

	/**
	 * 战斗的时候，有临时提升的buff
	 */
	private Map<Integer, Integer> buffs = new HashMap<>();

	private List<ArtifactStruct> arts = new ArrayList<>();

	private int[] lineups = new int[6];

	// 胜利条件
	private int condition = 0;

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

	public void mergeBuffs(Map<Integer, Integer> _buffs) {
		_buffs.forEach((k, v) -> {
			this.buffs.merge(k, v, (a, b) -> a + b);
		});
	}

	/**
	 * 实时模拟小战斗
	 * @param rc
	 */
	public void fillRight(RaidConf rc, int ratio) {
		List<Integer> heros = Readonly.getInstance().randHeros(6);
		for (int h : heros) {
			DirectHeroStruct shs = new DirectHeroStruct();
			shs.hero = h;
			shs.life = (long)(rc.hp * (ratio / 100.0));
			shs.attack = (int)(rc.attack * (ratio / 100.0));
			shs.def = (int)(rc.defence1 * (ratio / 100.0));
			shs.mdef = (int)(rc.defence2 * (ratio / 100.0));
			shs.critical = (int)(rc.crit * (ratio / 100.0));
			shs.aim = (int)(rc.hit * (ratio / 100.0));
			shs.antiCrit = (int)(rc.critoff * (ratio / 100.0));
			shs.dodge = (int)(rc.dog * (ratio / 100.0));
			shs.enHarm = (int)(rc.increase * (ratio / 100.0));
			shs.overHarm = -(int)(rc.reduce * (ratio / 100.0));
			right.add(shs);
		}
	}

	/**
	 * 副本数据填充
	 * @param rc
	 */
	public void fillRightByGuard(RaidConf rc) {
		int i = 0;
		for (int h : rc.guard) {
			if (h != 0) {
				int ratio = rc.details[i];
				DirectHeroStruct shs = new DirectHeroStruct();
				shs.hero = h;
				shs.life = (long)(rc.hp * (ratio / 100.0));
				shs.attack = (int)(rc.attack * (ratio / 100.0));
				shs.def = (int)(rc.defence1 * (ratio / 100.0));
				shs.mdef = (int)(rc.defence2 * (ratio / 100.0));
				shs.critical = (int)(rc.crit * (ratio / 100.0));
				shs.aim = (int)(rc.hit * (ratio / 100.0));
				shs.antiCrit = (int)(rc.critoff * (ratio / 100.0));
				shs.dodge = (int)(rc.dog * (ratio / 100.0));
				shs.enHarm = (int)(rc.increase * (ratio / 100.0));
				shs.overHarm = -(int)(rc.reduce * (ratio / 100.0));
				right.add(shs);
			} else {
				right.add(null);
			}
			++i;
		}
	}

	/**
	 * 矿区内单人战斗，副本数据填充, 但是英雄选择随机，仅固定传入一个英雄
	 * @param rc
	 */
	public void fillRightInMine(RaidConf rc, int mainHero) {
		List<Integer> heros = Readonly.getInstance().randHeros(rc.guard.length);
		heros.set(0, mainHero);

		int i = 0;
		for (int h : rc.guard) {
			if (h != 0) {
				int ratio = rc.details[i];
				DirectHeroStruct shs = new DirectHeroStruct();
				shs.hero = heros.get(i);
				shs.life = (long)(rc.hp * (ratio / 100.0));
				shs.attack = (int)(rc.attack * (ratio / 100.0));
				shs.def = (int)(rc.defence1 * (ratio / 100.0));
				shs.mdef = (int)(rc.defence2 * (ratio / 100.0));
				shs.critical = (int)(rc.crit * (ratio / 100.0));
				shs.aim = (int)(rc.hit * (ratio / 100.0));
				shs.antiCrit = (int)(rc.critoff * (ratio / 100.0));
				shs.dodge = (int)(rc.dog * (ratio / 100.0));
				shs.enHarm = (int)(rc.increase * (ratio / 100.0));
				shs.overHarm = -(int)(rc.reduce * (ratio / 100.0));
				right.add(shs);
			} else {
				right.add(null);
			}
			++i;
		}
	}



	/**
	 * 远征填充
	 * @param rc
	 */
	public void fillRightByGuard(FireraidConf rc) {
		int i = 0;
		for (int h : rc.guard) {
			if (h != 0) {
				int ratio = rc.details[i];
				DirectHeroStruct shs = new DirectHeroStruct();
				shs.hero = h;
				shs.life = (long)(rc.hp * (ratio / 100.0));
				shs.attack = (int)(rc.attack * (ratio / 100.0));
				shs.def = (int)(rc.defence1 * (ratio / 100.0));
				shs.mdef = (int)(rc.defence2 * (ratio / 100.0));
				shs.critical = (int)(rc.crit * (ratio / 100.0));
				shs.aim = (int)(rc.hit * (ratio / 100.0));
				shs.antiCrit = (int)(rc.critoff * (ratio / 100.0));
				shs.dodge = (int)(rc.dog * (ratio / 100.0));
				shs.enHarm = (int)(rc.increase * (ratio / 100.0));
				shs.overHarm = -(int)(rc.reduce * (ratio / 100.0));
				right.add(shs);
			} else {
				right.add(null);
			}
			++i;
		}
	}

	/**
	 * 日常副本填充
	 * @param rc
	 */
	public void fillRightByDailyScene(DailyraidConf rc) {
		int i = 0;
		for (int h : rc.defend) {
			if (h != 0) {
				int ratio = rc.details[i];
				DirectHeroStruct shs = new DirectHeroStruct();
				shs.hero = h;
				shs.life = (long)(rc.hp * (ratio / 100.0));
				shs.attack = (int)(rc.attack * (ratio / 100.0));
				shs.def = (int)(rc.defence1 * (ratio / 100.0));
				shs.mdef = (int)(rc.defence2 * (ratio / 100.0));
				shs.critical = (int)(rc.crit * (ratio / 100.0));
				shs.aim = (int)(rc.hit * (ratio / 100.0));
				shs.antiCrit = (int)(rc.critoff * (ratio / 100.0));
				shs.dodge = (int)(rc.dog * (ratio / 100.0));
				shs.enHarm = (int)(rc.increase * (ratio / 100.0));
				shs.overHarm = -(int)(rc.reduce * (ratio / 100.0));
				right.add(shs);
			} else {
				right.add(null);
			}
			++i;
		}
	}


	/**
	 * 根据公会副本4个不同的关卡，填充当前对手
	 * @param index [1,4]
	 * @param rc
	 */
	public void fillRightByGuildScene(int index, GuildraidConf rc) {
		int[] defend = new int[0];
		switch (index) {
			case 1:
				defend = rc.sect1;
				break;
			case 2:
				defend = rc.sect2;
				break;
			case 3:
				defend = rc.sect3;
				break;
			case 4:
				defend = rc.sect4;
				break;
		}

		for (int h : defend) {
			if (h != 0) {
				int ratio = 100;
				DirectHeroStruct shs = new DirectHeroStruct();
				shs.hero = h;
				shs.life = (long)(rc.hp * (ratio / 100.0));
				shs.attack = (int)(rc.attack * (ratio / 100.0));
				shs.def = (int)(rc.defence1 * (ratio / 100.0));
				shs.mdef = (int)(rc.defence2 * (ratio / 100.0));
				shs.critical = (int)(rc.crit * (ratio / 100.0));
				shs.aim = (int)(rc.hit * (ratio / 100.0));
				shs.antiCrit = (int)(rc.critoff * (ratio / 100.0));
				shs.dodge = (int)(rc.dog * (ratio / 100.0));
				shs.enHarm = (int)(rc.increase * (ratio / 100.0));
				shs.overHarm = -(int)(rc.reduce * (ratio / 100.0));
				right.add(shs);
			} else {
				right.add(null);
			}
		}
	}

	public void fillRightByRune(FuwenraidConf rc) {
		int i = 0;

		int idx = RandomUtil.getRandom() % rc.guard.length;
		int[] guard = rc.guard[idx];

		for (int h : guard) {
			if (h != 0) {
				int ratio = 100;
				DirectHeroStruct shs = new DirectHeroStruct();
				shs.hero = h;
				shs.life = (long)(rc.hp * (ratio / 100.0));
				shs.attack = (int)(rc.attack * (ratio / 100.0));
				shs.def = (int)(rc.defence1 * (ratio / 100.0));
				shs.mdef = (int)(rc.defence2 * (ratio / 100.0));
				shs.critical = (int)(rc.crit * (ratio / 100.0));
				shs.aim = (int)(rc.hit * (ratio / 100.0));
				shs.antiCrit = (int)(rc.critoff * (ratio / 100.0));
				shs.dodge = (int)(rc.dog * (ratio / 100.0));
				shs.enHarm = (int)(rc.increase * (ratio / 100.0));
				shs.overHarm = -(int)(rc.reduce * (ratio / 100.0));
				right.add(shs);
			} else {
				right.add(null);
			}
			++i;
		}
	}

}
