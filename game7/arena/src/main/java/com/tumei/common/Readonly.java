package com.tumei.common;

import com.tumei.common.utils.RandomUtil;
import com.tumei.configs.IReadonly;
import com.tumei.modelconf.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/1/19 0019.
 *
 * 只读配置的预加载，可以在运行时异步加载
 *
 */
@Service
public class Readonly extends IReadonly {
    private static Readonly _instance = null;

    public static Readonly getInstance() {
        return _instance;
    }

	private static Log log = LogFactory.getLog(Readonly.class);

	public static final int reward_interval = 600;

	class Conf {

		public List<NameConf> names;
		public List<ArenalistConf> arenalists;
		public List<TrmonsterConf> trmonsterConfs;
		public List<TopRankConf> topRankConfs;
		public List<SarenarewardConf> sarenarewardConfs;

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
	@Override
	public void refresh() {
		log.info("--- refresh readonly config db items...");

		super.refresh();

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

	public  List<Integer> randHerosByList(int count, int[] grades) {
		List<Integer> objs = bc.heros.keySet().stream().filter(key -> {
			if (key >= 90000) {
				return false;
			}

			HeroConf hc = bc.heros.get(key);
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
