package com.tumei.game.services;

import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.RemoteService;
import com.tumei.common.fight.PowerStruct;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameServer;
import com.tumei.game.protos.structs.RankStruct;
import com.tumei.model.*;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.ArenarewardConf;
import com.tumei.modelconf.RobotConf;
import com.tumei.modelconf.RobotConfRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

/**
 * Created by Administrator on 2017/4/11 0011.
 * <p>
 * 竞技场排名
 */
@Service
public class RankService {
	final static Log log = LogFactory.getLog(RankService.class);

	private static RankService _instance;

	public static RankService getInstance() {
		return _instance;
	}

	@Autowired
	private DaoService dao;

	@Autowired
	private Readonly readonly;

	@Autowired
	private RemoteService remoteService;

	@Autowired
	private RankBeanRepository rankBeanRepository;

	@Autowired
	private RobotConfRepository robotConfRepository;

	/**
	 * 排名
	 */
	private List<RankBean> ranks = new LinkedList<>();

	/**
	 * 玩家列表
	 */
	private Map<Long, RankBean> users = new HashMap<>();

	/**
	 * 发生变动的排名
	 */
	private Set<Long> changes = new HashSet<>();

	@Autowired
	private AllRankBeanRepository allRankBeanRepository;

	private AllRankBean allRankBean;

	private boolean scoreDirty;

	@PostConstruct
	void init() {
		_instance = this;

		allRankBean = allRankBeanRepository.findById(1L);
		if (allRankBean == null) {
			allRankBean = new AllRankBean(1L);
		}

		{
			log.warn("开始初始化竞技场数据，启动的时候从数据库中读取，每隔5分钟进行一次保存");
			List<RankBean> rk = rankBeanRepository.findAll(new Sort("rank"));
			synchronized (this) {
				if (rk.size() == 0) { // Role.Ranks 为空的时候，需要创建3000个机器人
					List<RobotConf> rcs = robotConfRepository.findAll(new Sort("key"));
					int start = 0;
					for (RobotConf rc : rcs) {
						for (int i = start; i < rc.rank; ++i) {
							RankBean rb = new RankBean(i, i);
							rb.setName(readonly.randomName());
							HeroBean[] hbs = rb.getFormation();
							List<Integer> heros = readonly.randHerosByList(6, rc.quality);
							rb.setIcon(heros.get(0));
							rb.setLevel(rc.level);

							for (int j = 0; j < 6; ++j) {
								HeroBean hb = new HeroBean();
								hb.setId(heros.get(j));
								hb.setHid(j);
								hb.setLevel(rc.level);
								hb.setGrade(rc.grade);
								hbs[j] = hb;
							}

							PowerStruct ts = HerosBean.createTeamStructForRobot(hbs);
							long power = remoteService.callPower(ts);
							rb.setPower(power);

							rk.add(rb);
							rankBeanRepository.save(rb);
						}
						start = rc.rank;
					}
				}

				ranks.addAll(rk);
				for (RankBean rb : rk) {
					users.put(rb.getId(), rb);
				}
			}
			log.info("---- 竞技场数据完成");

		}
	}

	// 1000秒刷新一次
	@Scheduled(fixedRate = 1000000)
	void update() {
		// save all changed ranks
		try {
			saveChanges();
		} catch (Exception ex) {
			log.error("竞技场排名服务器，保存失败.");
		}
	}

	// 每晚8点发放竞技场奖励
	@Scheduled(cron = "0 0 22 * * *")
	void arenaSchedule() {
		List<Long> rk = new ArrayList<>();
		synchronized (this) {
			ranks.forEach( r -> {
				rk.add(r.getId());
			});
		}

		// 根据名字发放奖励
		for (int i = 0; i < rk.size(); ++i) {
			ArenarewardConf ac = Readonly.getInstance().getArenaRewards(i);
			if (ac != null) {
				String awards = "";
				for (int rr : ac.rankreward) {
					awards += rr + ",";
				}

				GameServer.getInstance().sendAwardMail(rk.get(i), "竞技场排名奖励", "竞技场排名" + (i+1), awards);
			}
		}
	}

	// 每周一0点进行更新
	@Scheduled(cron = "0 0 0 ? * MON")
	void schedule() {
		try {
			int[] rs = new int[4];
			int[] ranks = new int[4];
			// 符文副本的积分清掉
			synchronized (allRankBean) {
				rs[0] = (allRankBean.getGroup1());
				rs[1] = (allRankBean.getGroup2());
				rs[2] = (allRankBean.getGroup3());
				rs[3] = (allRankBean.getGroup4());

				// 根据4个阵营进行排序
				for (int i = 0; i < 4; ++i) {
					int idx = 1;
					for (int j = 0; j < 4; ++j) {
						if (rs[j] > rs[i]) {
							++idx;
						}
					}
					ranks[i] = idx;
				}

				allRankBean.flush(ranks);
				allRankBeanRepository.save(allRankBean);
			}
		} catch (Exception e) {
			log.error("周一清理 符文副本 阵营排名 错误.");
		}
	}

	@PreDestroy
	void dispose() {
		log.warn("服务退出的时候保存当前竞技场排名");
		// save all changed ranks
		try {
			saveChanges();
		} catch (Exception ex) {
			log.error("竞技场排名服务器，保存失败.");
		}
	}

	public int submitScore(int index, int score) {
		int s = 0;
		synchronized (allRankBean) {
			s = allRankBean.submit(index, score);
			scoreDirty = true;
		}
		return s;
	}

	public int getGroupScore(int index) {
		synchronized (allRankBean) {
			switch (index) {
				case 1:
					return allRankBean.getGroup1();
				case 2:
					return allRankBean.getGroup2();
				case 3:
					return allRankBean.getGroup3();
				case 4:
					return allRankBean.getGroup4();
			}
			return 0;
		}
	}

	public List<Integer> getGroupScores() {
		List<Integer> rtn = new ArrayList<>();
		synchronized (allRankBean) {
			rtn.add(allRankBean.getGroup1());
			rtn.add(allRankBean.getGroup2());
			rtn.add(allRankBean.getGroup3());
			rtn.add(allRankBean.getGroup4());
		}
		return rtn;
	}

	/**
	 * 获取上周阵营排序
	 * @param lastGroup
	 * @return
	 * 0 || [1,4]
	 */
	public int getLastGroupRank(int lastGroup) {
		if (lastGroup <= 0 || lastGroup > 4) {// 没有上周的阵营
			return 0;
		}

//		synchronized (allRankBean) {
			return allRankBean.getRanks()[lastGroup - 1];
//		}
	}


	/**
	 * 根据变动列表，将发生变化的数据保存
	 */
	void saveChanges() {
		synchronized (this) {
			for (Long index : changes) {
				RankBean rb = users.get(index);
				rankBeanRepository.save(rb);
			}
			changes.clear();
		}
		synchronized (allRankBean) {
			if (scoreDirty) {
				allRankBeanRepository.save(allRankBean);
				scoreDirty = false;
			}
		}
	}

	/**
	 * 根据玩家id，获取当前排名
	 *
	 * @param id
	 * @return
	 */
	public RankBean getRank(long id) {
		boolean _flush = false;
		RankBean rb = null;
		synchronized (this) {
			rb = users.getOrDefault(id, null);
			if (rb == null) {
				rb = new RankBean(id, ranks.size());

				ranks.add(rb);
				users.put(id, rb);
				_flush = true;
			}

			// TODO. for test 每次都提交新的数据到竞技场
			if (id > 10000) {
				if (rb.flush()) {
					_flush = true;
				}
				_flush = true;
			}

			if (_flush) {
				/**
				 * 拉取数据，构造具体排名信息
				 */
				{
					RoleBean role = dao.findRole(id);
					rb.setIcon(role.getIcon());
					rb.setName(role.getNickname());
					rb.setLevel(role.getLevel());

					HerosBean hsb = dao.findHeros(id);
					rb.setFormation(hsb.getHeros());
					rb.setFashion(hsb.getFakeHero());

					PowerStruct ts = hsb.createTeamStruct();
					long power = remoteService.callPower(ts);
					rb.setPower(power);
				}

				changes.add(rb.getId());
			}
		}

		return rb;
	}

	/**
	 * 获取指定玩家的最高历史排行
	 *
	 * @param uid
	 * @return
	 */
	public synchronized int getPeekRank(long uid) {
		RankBean rb = users.getOrDefault(uid, null);
		if (rb != null) {
			return rb.getPeek();
		}
		return 99998;
	}

	/**
	 * 找到前count个排名最高的玩家的记录
	 *
	 * @param count
	 * @return
	 */
	public synchronized List<RankStruct> getTopRanks(int count) {
		List<RankStruct> rtn = new ArrayList<>();

		if (count > ranks.size()) {
			count = ranks.size();
		}

		for (int i = 0; i < count; ++i) {
			rtn.add(new RankStruct(ranks.get(i)));
		}

		return rtn;
	}

	/**
	 * 根据排名获取对手
	 * <p>
	 * 首先获取前十名
	 *
	 * @param rank
	 * @return
	 */
	public synchronized List<RankStruct> getPeers(int rank) {
		List<RankStruct> rtn = new ArrayList<>();

		int X = Readonly.getInstance().getArenaInterval(rank);

		for (int i = 0; i < 10; ++i) {
			rtn.add(new RankStruct(ranks.get(i)));
		}

		for (int i = 9; i >= 1; --i) {
			int tmp = rank - X * i;
			if (tmp >= 10 && tmp < ranks.size()) {
				rtn.add(new RankStruct(ranks.get(tmp)));
			}
		}

		rtn.add(new RankStruct(ranks.get(rank)));

		int tmp = rank;
		for (int i = 1; i < 3; ++i) {
			tmp = tmp + X;
			if (tmp >= 10 && tmp < ranks.size()) {
				rtn.add(new RankStruct(ranks.get(tmp)));
			}
		}

		return rtn;
	}

	/**
	 * 对应玩家,查找在竞技场中他的排名-15 ~ +15名中的一个对手
	 * <p>
	 * 1. 不能返回玩家自己, 如果随机到自己，则返回前一名
	 * 2. 考虑玩家在前15名和后15名的极值情况，需要将区间做调整,否则返回的随机性太低
	 *
	 * @param lord 玩家id
	 * @return 返回的对手玩家信息
	 */
	public synchronized RankBean findNearest(long lord) {
		RankBean rb = users.get(lord);
		if (rb == null) {
			return null;
		}

		// 找到玩家的排名，然后随机从他周围的排名中取角色
		int min = rb.getRank() - 15;
		int max = rb.getRank();
		if (min < 0) {
			max -= min;
			min = 0;
		}

		if (max >= ranks.size()) {
			max = ranks.size() - 1;
		}

		int rank = (RandomUtil.getRandom() % (max - min)) + min;

		/**
		 * 如果随机得到的排名就是自己
		 */
		if (rank == rb.getRank()) {
			if (rank == 0) {
				rank = 1;
			}
			else {
				rank = rank - 1;
			}
		}

		return ranks.get(rank);
	}

	/**
	 * 进行交换
	 *
	 * @param uid
	 * @param id
	 * @param rank 别人的名次
	 * @return -1: 排名不符
	 * >=0: 排名变化导致钻石的提升
	 */
	public synchronized int exchange(long uid, long id, int rank) {
		RankBean other = users.get(id);
		if (other.getRank() != rank) {
			return -1;
		}

		RankBean self = users.get(uid);
		int selfRank = self.getRank();

		if (rank < selfRank) {
			other.setRank(selfRank);
			self.setRank(rank);

			// 列表也对应的进行交换
			ranks.set(selfRank, other);
			ranks.set(rank, self);

			changes.add(uid);
			changes.add(id);

			int diff = rank - self.getPeek();
			if (diff < 0) {
				self.setPeek(rank);
				return (-diff / 5) * 2;
			}
		}

		return 0;
	}

	/**
	 * 快速挑战必定胜利 但是要判断2个条件
	 * 1： 对方的排名符合实际
	 * 2： 我比对方排名要靠前
	 *
	 * @param uid
	 * @param id
	 * @param rank
	 * @return
	 */
	public synchronized boolean judge(long uid, long id, int rank) {
		RankBean self = users.get(uid);
		RankBean other = users.get(id);
		if (other.getRank() != rank) {
			return false;
		}

		if (self.getRank() >= rank) {
			return false;
		}

		return true;
	}

	public synchronized void dirty(long uid) {
		changes.add(uid);
	}
}
