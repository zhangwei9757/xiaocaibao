package com.tumei.configs;

import com.tumei.modelconf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Leon on 2018/3/21.
 */
public class IReadonly {

	@Autowired
	@Qualifier("confTemplate")
	protected MongoTemplate mongoTemplate;

	protected class BattleConf {
		// 英雄
		public HashMap<Integer, HeroConf> heros = new HashMap<>();
		// 英雄技能
		public HashMap<Integer, HeroSkillConf> skills = new HashMap<>();
		// 装备
		public HashMap<Integer, EquipConf> equips = new HashMap<>();
		// 境界
		public HashMap<Integer, StateupConf> stateups = new HashMap<>();
		// 觉醒
		public HashMap<Integer, AwakenConf> awakens = new HashMap<>();
		// 队形强化
		public List<LineupConf> lineups;
		// 装备共鸣
		public List<ResonanceConf> resonances;
		// 皮肤
		public HashMap<Integer, MaskConf> masks = new HashMap<>();
		// 圣物
		public HashMap<Integer, HolyConf> holys = new HashMap<>();
		// 传奇英雄
		public HashMap<Integer, LegendHero> legendHeros = new HashMap<>();

		public void initialize() {
			{
				List<HeroConf> bean = mongoTemplate.findAll(HeroConf.class);
				for (HeroConf ib : bean) {
					heros.put(ib.key, ib);
				}
			}
			{
				List<EquipConf> bean = mongoTemplate.findAll(EquipConf.class);
				for (EquipConf ib : bean) {
					equips.put(ib.key, ib);
				}
			}
			{
				List<HeroSkillConf> bean = mongoTemplate.findAll(HeroSkillConf.class);
				for (HeroSkillConf ib : bean) {
					skills.put(ib.key, ib);
				}
			}
			{
				List<StateupConf> bean = mongoTemplate.findAll(StateupConf.class);
				for (StateupConf ib : bean) {
					stateups.put(ib.key, ib);
				}
			}
			{
				List<AwakenConf> bean = mongoTemplate.findAll(AwakenConf.class);
				for (AwakenConf ib : bean) {
					awakens.put(ib.key, ib);
				}
			}
			{
				List<MaskConf> bean = mongoTemplate.findAll(MaskConf.class);
				for (MaskConf ib : bean) {
					masks.put(ib.key, ib);
				}
			}
			{
				resonances = mongoTemplate.findAll(ResonanceConf.class);
				resonances.sort((o1, o2) -> {
					if (o1.key < o2.key) {
						return -1;
					}
					else if (o1.key > o2.key) {
						return 1;
					}
					return 0;
				});
			}

			lineups = mongoTemplate.findAll(LineupConf.class);
			lineups.sort((o1, o2) -> {
				if (o1.key < o2.key) {
					return -1;
				}
				else if (o1.key > o2.key) {
					return 1;
				}
				return 0;
			});

			{
				List<HolyConf> bean = mongoTemplate.findAll(HolyConf.class);
				for (HolyConf ib : bean) {
					holys.put(ib.key, ib);
				}
			}
			{
				List<LegendHero> bean = mongoTemplate.findAll(LegendHero.class);
				for (LegendHero ib : bean) {
					legendHeros.put(ib.key, ib);
				}
			}
		}
	}

	// 战斗需要的配置
	protected BattleConf bc;

	public void refresh() {
		BattleConf _bc = new BattleConf();
		_bc.initialize();
		this.bc = _bc;
	}

	/**
	 * 查找指定的英雄信息
	 *
	 * @param id 英雄ID
	 * @return 返回英雄具体信息
	 */
	public HeroConf findHero(int id) {
		return bc.heros.get(id);
	}

	public  EquipConf findEquip(int id) {
		return bc.equips.get(id);
	}

	/**
	 * 查找指定英雄对应的技能
	 *
	 * @param id 英雄id
	 * @return 技能相关的所有信息
	 */
	public  HeroSkillConf findSkill(int id) {
		return bc.skills.get(id);
	}

	/**
	 * 查找指定的英雄境界提升
	 *
	 * @param level 境界 [1...]
	 * @return 返回英雄具体信息
	 */
	public  StateupConf findStateup(int level) {
		return bc.stateups.get(level);
	}

	/**
	 * 觉醒
	 *
	 * @param level 当前觉醒等级
	 * @return
	 */
	public  AwakenConf findAwaken(int level) {
		return bc.awakens.get(level);
	}

	/**
	 * 根据当前的等级，与mode来决定共鸣
	 *
	 * @param level 等级
	 * @param mode  0 装备强化共鸣，1 装备精炼共鸣，2 宝物强化共鸣，3 宝物精炼共鸣
	 * @return 共鸣效果
	 */
	public  int[] findResonance(int level, int mode) {
		switch (mode) {
			case 0: {
				int[] selected = null;
				for (ResonanceConf rb : bc.resonances) {
					if (level >= rb.equstr[0][0]) {
						selected = rb.equstr[1];
					}
					else {
						break;
					}
				}
				return selected;
			}
			case 1: {
				int[] selected = null;
				for (ResonanceConf rb : bc.resonances) {
					if (level >= rb.equref[0][0]) {
						selected = rb.equref[1];
					}
					else {
						break;
					}
				}
				return selected;
			}
			case 2: {
				int[] selected = null;
				for (ResonanceConf rb : bc.resonances) {
					if (level >= rb.trestr[0][0]) {
						selected = rb.trestr[1];
					}
					else {
						break;
					}
				}
				return selected;
			}
			case 3: {
				int[] selected = null;
				for (ResonanceConf rb : bc.resonances) {
					if (level >= rb.treref[0][0]) {
						selected = rb.treref[1];
					}
					else {
						break;
					}
				}
				return selected;
			}
		}
		return null;
	}

	/**
	 * 时装
	 *
	 * @param mask [1,...]
	 * @return
	 */
	public  MaskConf findMask(int mask) {
		return bc.masks.getOrDefault(mask, null);
	}

	/**
	 * 阵形 站位 增幅
	 *
	 * @param index 站位 [0,5]
	 * @return
	 */
	public  LineupConf findLineup(int index) {
		return bc.lineups.get(index);
	}

	public HolyConf findHoly(int id) {
		return bc.holys.getOrDefault(id, null);
	}

	public LegendHero findLegendHero(int id) {
		return bc.legendHeros.getOrDefault(id, null);
	}

}
