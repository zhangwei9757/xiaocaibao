package com.tumei.modelconf;

import com.tumei.common.utils.RandomUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

	private Log log = LogFactory.getLog(Readonly.class);

	public static final int reward_interval = 600;

	@Autowired
	@Qualifier("confTemplate")
	private MongoTemplate mongoTemplate;

	class Conf {

		public List<NameConf> names;
		public List<ArenalistConf> arenalists;
		public List<TrmonsterConf> trmonsterConfs;
		public List<TopRankConf> topRankConfs;
		public List<SarenarewardConf> sarenarewardConfs;

		public HashMap<Integer, HeroConf> heros = new HashMap<>();
		public HashMap<Integer, HeroSkillConf> skills = new HashMap<>();
		public HashMap<Integer, EquipConf> equips = new HashMap<>();
		public HashMap<Integer, MaskConf> masks = new HashMap<>();

		public void initialize() {

			names = mongoTemplate.findAll(NameConf.class);
			names.sort((o1, o2) -> {
				if (o1.key < o2.key) {
					return -1;
				}
				else if (o1.key > o2.key) {
					return 1;
				}
				return 0;
			});

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
				List<MaskConf> bean = mongoTemplate.findAll(MaskConf.class);
				for (MaskConf ib : bean) {
					masks.put(ib.key, ib);
				}
			}

			{
				arenalists = mongoTemplate.findAll(ArenalistConf.class);
				arenalists.sort((o1, o2) -> {
					if (o1.key < o2.key) {
						return -1;
					}
					else if (o1.key > o2.key) {
						return 1;
					}
					return 0;
				});
			}
			{
				trmonsterConfs = mongoTemplate.findAll(TrmonsterConf.class);
				trmonsterConfs.sort((o1, o2) -> {
					if (o1.key < o2.key) {
						return -1;
					}
					else if (o1.key > o2.key) {
						return 1;
					}
					return 0;
				});
			}
			{
				topRankConfs = mongoTemplate.findAll(TopRankConf.class);
				topRankConfs.sort((o1, o2) -> {
					if (o1.key < o2.key) {
						return -1;
					}
					else if (o1.key > o2.key) {
						return 1;
					}
					return 0;
				});
			}
			{
				sarenarewardConfs = mongoTemplate.findAll(SarenarewardConf.class);
				sarenarewardConfs.sort((o1, o2) -> {
					if (o1.key < o2.key) {
						return -1;
					}
					else if (o1.key > o2.key) {
						return 1;
					}
					return 0;
				});
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

	/**
	 * 随机名字
	 *
	 * @return
	 */
	public  String randomName() {
		int a = RandomUtil.getRandom() % conf.names.size();
		int b = RandomUtil.getRandom() % conf.names.size();
		int c = RandomUtil.getRandom() % conf.names.size();

		return conf.names.get(a).part1 + conf.names.get(b).part2 + conf.names.get(c).part3;
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
	* 时装
	* @param mask [1,...]
	* @return
	*/
	public MaskConf findMask(int mask) {
		return conf.masks.getOrDefault(mask, null);
	}

	public  List<Integer> randHerosByList(int count, int[] grades) {
		List<Integer> objs = conf.heros.keySet().stream().filter(key -> {
			if (key >= 90000) {
				return false;
			}

			HeroConf hc = conf.heros.get(key);
			for (int g : grades) {
				if (g == hc.quality) {
					return true;
				}
			}

			return false;
		}).collect(Collectors.toList());
		Collections.shuffle(objs);
		return objs.subList(0, count);
	}

	/**
	 * 根据排名获取竞技场的间隔
	 *
	 * @return
	 */
	public int getArenaInterval(int rank) {
		++rank;
		for (ArenalistConf ac : conf.arenalists) {
			if (rank >= ac.rank[0] && rank <= ac.rank[1]) {
				return ac.interval;
			}
		}

		return 40;
	}

	/**
	 * 返回天体赛具体key位置的配置怪物
	 * @param key  [1, 65], 一共66个配置位置
	 * @return
	 */
	public TrmonsterConf findMonsterConf(int key) {
		--key;
		if (key < 0 || key >= conf.trmonsterConfs.size()) {
			return null;
		}

		return conf.trmonsterConfs.get(key);
	}

	/**
	 * 返回天体赛每个分组的信息
	 * @param key  [1, 7] 王者到备战
	 * @return
	 */
	public TopRankConf findTopRankConf(int key) {
		--key;
		if (key < 0 || key >= conf.topRankConfs.size()) {
			return null;
		}

		return conf.topRankConfs.get(key);
	}

	public SarenarewardConf findArenalistConf(int key) {
		--key;
		if (key < 0 || key >= conf.sarenarewardConfs.size()) {
			return null;
		}

		return conf.sarenarewardConfs.get(key);
	}

}
