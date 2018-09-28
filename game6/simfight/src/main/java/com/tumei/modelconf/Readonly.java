package com.tumei.modelconf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/1/19 0019.
 *
 * 只读配置的预加载，可以在运行时异步加载
 *
 */
@Service
public class Readonly {
    private static Readonly _instance = null;

    public static Readonly getInstance() {
        return _instance;
    }

	private static final Log log = LogFactory.getLog(Readonly.class);

	@Autowired
	@Qualifier("confTemplate")
	private MongoTemplate mongoTemplate;

	class Conf {

		public List<ResonanceConf> resonances;

		public HashMap<Integer, HeroConf> heros = new HashMap<>();
		public HashMap<Integer, HeroSkillConf> skills = new HashMap<>();
		public HashMap<Integer, EquipConf> equips = new HashMap<>();
		public HashMap<Integer, StateupConf> stateups = new HashMap<>();
		public HashMap<Integer, AwakenConf> awakens = new HashMap<>();
		public HashMap<Integer, MaskConf> masks = new HashMap<>();
		public HashMap<Integer, LineupConf> lineups = new HashMap<>();
		public HashMap<Integer, ArtpartConf> artparts = new HashMap<>();
		public HashMap<Integer, ArtifactConf> artifacts = new HashMap<>();
		public List<ArtpartstupConf> artpartstups = new ArrayList<>();
		// 圣物
		public HashMap<Integer, HolyConf> holys = new HashMap<>();
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
				List<LineupConf> bean = mongoTemplate.findAll(LineupConf.class);
				for (LineupConf ib : bean) {
					lineups.put(ib.key, ib);
				}
			}
			{
				List<ArtpartConf> bean = mongoTemplate.findAll(ArtpartConf.class);
				for (ArtpartConf ib : bean) {
					artparts.put(ib.key, ib);
				}
			}
			{
				List<ArtifactConf> bean = mongoTemplate.findAll(ArtifactConf.class);
				for (ArtifactConf ib : bean) {
					artifacts.put(ib.key, ib);
				}
			}
			artpartstups = mongoTemplate.findAll(ArtpartstupConf.class);
			artpartstups.sort((a, b) -> {
				if (a.key < b.key) {
					return -1;
				} else if (a.key > b.key) {
					return 1;
				}
				return 0;
			});

			resonances = mongoTemplate.findAll(ResonanceConf.class);
			resonances.sort((a, b) -> {
				if (a.key < b.key) {
					return -1;
				} else if (a.key > b.key) {
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

	private Conf conf;

	@PostConstruct
	public void init() {
		refresh();
        _instance = this;
	}

	/**
	 * 带锁刷新只读表到内存中
	 */
	public void refresh() {
		log.info("--- refresh readonly config db items...");
		Conf _conf = new Conf();
		_conf.initialize();
		this.conf = _conf;
		log.info("--- finish refresh readonly config db items...");
	}

	public EquipConf findEquip(Integer id) {
		return conf.equips.get(id);
	}

	/**
	 * 查找指定的英雄信息
	 * @param id 英雄ID
	 * @return
	 * 		返回英雄具体信息
	 */
    public HeroConf findHero(Integer id) {
        return conf.heros.get(id);
    }

	/**
	 * 查找指定英雄对应的技能
	 * @param id 英雄id
	 * @return
	 * 	技能相关的所有信息
	 */
	public HeroSkillConf findSkill(Integer id) {
		return conf.skills.get(id);
	}


	/**
	 * 查找指定的英雄境界提升
	 * @param level 境界
	 * @return
	 * 		返回英雄具体信息
	 */
	public StateupConf findStateup(int level) {
		return conf.stateups.get(level);
	}

	/**
	 * 觉醒
	 *
	 * @param level
	 * @return
	 */
	public AwakenConf findAwaken(int level) {
		return conf.awakens.get(level);
	}

	/**
	 * 根据当前的等级，与mode来决定共鸣
	 *
	 * @param level 等级
	 * @param mode 0 装备强化共鸣，1 装备精炼共鸣，2 宝物强化共鸣，3 宝物精炼共鸣
	 * @return
	 *
	 * 共鸣效果
	 */
	public int[] findResonance(int level, int mode) {
		switch (mode) {
			case 0:
			{
				int[] selected = null;
				for (ResonanceConf rb : conf.resonances) {
					if (rb.equstr.length == 0) {
						return selected;
					}
					if (level >= rb.equstr[0][0]) {
						selected = rb.equstr[1];
					} else {
						break;
					}
				}
				return selected;
			}
			case 1:
			{
				int[] selected = null;
				for (ResonanceConf rb : conf.resonances) {
					if (rb.equref.length == 0) {
						return selected;
					}
					if (level >= rb.equref[0][0]) {
						selected = rb.equref[1];
					} else {
						break;
					}
				}
				return selected;
			}
			case 2:
			{
				int[] selected = null;
				for (ResonanceConf rb : conf.resonances) {
					if (rb.trestr.length == 0) {
						return selected;
					}
					if (rb != null && level >= rb.trestr[0][0]) {
						selected = rb.trestr[1];
					} else {
						break;
					}
				}
				return selected;
			}
			case 3:
			{
				int[] selected = null;
				for (ResonanceConf rb : conf.resonances) {
					if (rb.treref.length == 0) {
						return selected;
					}
					if (level >= rb.treref[0][0]) {
						selected = rb.treref[1];
					} else {
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
	* @param mask [1,...]
	* @return
	*/
	public MaskConf findMask(int mask) {
		return conf.masks.getOrDefault(mask, null);
	}

	public LineupConf findLineup(int key) {
		return conf.lineups.getOrDefault(key, null);
	}

	/**
	 * 获取神器部件配置
	 * @param key 部件id
	 * @return
	 */
	public ArtpartConf findArtpart(int key) {
		return conf.artparts.getOrDefault(key, null);
	}

	/**
	 * 获取神器配置
	 * @param key 神器id
	 * @return
	 */
	public ArtifactConf findArtifact(int key) {
		return conf.artifacts.getOrDefault(key, null);
	}

	public ArtpartstupConf findArtpartup(int level) {
		if (level >= conf.artpartstups.size()) {
			return null;
		}

		return conf.artpartstups.get(level);
	}
	public HolyConf findHoly(int id) {
		return conf.holys.getOrDefault(id, null);
	}
	public LegendHero findLegendHero(int id) {
		return conf.legendHeros.getOrDefault(id, null);
	}
}
