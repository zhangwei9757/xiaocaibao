package com.tumei.game.services;

import com.tumei.common.Readonly;
import com.tumei.dto.db2proto.NameValue;
import com.tumei.game.GameServer;
import com.tumei.model.OpenRankBean;
import com.tumei.model.OpenRankBeanRepository;
import com.tumei.modelconf.OpenraceConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by Leon on 2017/12/13.
 *
 * 排行服务
 *
 * users 记录玩家ID 对应的所有信息
 *
 * charges, spends ... 是java的跳表，快速的插入的时候进行排序，快速的删除
 *
 *
 * 增加新的功能，在指定的时间段，进行排行统计
 *
 */
@Service
public class RankService {
	private final static Log log = LogFactory.getLog(RankService.class);

	private static RankService _instance;

	public static RankService getInstance() {
		return _instance;
	}

	@Autowired
	private Readonly readonly;

	@Autowired
	private LocalService localService;

	@Autowired
	private OpenRankBeanRepository openRankBeanRepository;

	private long day3;
	private long day5;
	private long day7;

	// 时间段排行统计开始
	private long begin;
	// 时间段排行统计结束
	private long end;

	/**
	 * 所有玩家的信息
	 */
	private HashMap<Long, OpenRankBean> users = new HashMap<>();

	// 累计一定的次数进行数据库写入
	private int cum;

	// 发生改变的内容
	private HashSet<Long> changes = new HashSet<>();
	/**
	 * 消费排行 3
	 */
	private ConcurrentSkipListMap<OpenRankBean, Integer> spends;
	/**
	 * 充值排行 3
	 */
	private ConcurrentSkipListMap<OpenRankBean, Integer> charges;
	/**
	 * 远征星级 5
	 */
	private ConcurrentSkipListMap<OpenRankBean, Integer> stars;
	/**
	 * 领主等级 5
	 */
	private ConcurrentSkipListMap<OpenRankBean, Integer> levels;
	/**
	 * 战斗力 7
	 */
	private ConcurrentSkipListMap<OpenRankBean, Integer> powers;
	/**
	 * 副本关卡 7
	 */
	private ConcurrentSkipListMap<OpenRankBean, Integer> scenes;

	// 初始化完成标识
	private boolean inited = false;

	/**
	 *
	 * 初始化
	 *
	 */
	@PostConstruct
	synchronized void init() {
		_instance = this;
		long tmp = localService.getOpenDate().getTime();
		LocalDateTime ldt = LocalDateTime.ofEpochSecond(tmp / 1000, 0, ZoneOffset.ofHours(8));
		LocalDate ld = ldt.toLocalDate();

		{
			LocalDateTime t3 = ld.atStartOfDay().plusDays(3);
			day3 = t3.toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
			log.warn("开服竞赛3日到期时间:" + t3);
		}
		{
			LocalDateTime t5 = ld.atStartOfDay().plusDays(5);
			day5 = t5.toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
			log.warn("开服竞赛5日到期时间:" + t5);
		}
		{
			LocalDateTime t7 = ld.atStartOfDay().plusDays(7);
			day7 = t7.toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
			log.warn("开服竞赛7日到期时间:" + t7);
		}

		// 充值
		charges = new ConcurrentSkipListMap<>((a1, a2) -> {
			if (a1.getCharge() > a2.getCharge()) {
				return -1;
			}
			else if (a1.getCharge() < a2.getCharge()) {
				return 1;
			}

			long aa1 = a1.getOpIdx(0);
			long aa2 = a2.getOpIdx(0);

			if (aa1 < aa2) {
				return -1;
			} else if (aa1 > aa2) {
				return 1;
			}

			return 0;
		});

		// 消费
		spends = new ConcurrentSkipListMap<>((a1, a2) -> {
			if (a1.getSpend() > a2.getSpend()) {
				return -1;
			}
			else if (a1.getSpend() < a2.getSpend()) {
				return 1;
			}

			long aa1 = a1.getOpIdx(1);
			long aa2 = a2.getOpIdx(1);

			if (aa1 < aa2) {
				return -1;
			} else if (aa1 > aa2) {
				return 1;
			}
			return 0;
		});

		// 等级
		levels = new ConcurrentSkipListMap<>((a1, a2) -> {
			if (a1.getLevel() > a2.getLevel()) {
				return -1;
			}
			else if (a1.getLevel() < a2.getLevel()) {
				return 1;
			}
			long aa1 = a1.getOpIdx(2);
			long aa2 = a2.getOpIdx(2);

			if (aa1 < aa2) {
				return -1;
			} else if (aa1 > aa2) {
				return 1;
			}
			return 0;
		});

		// 远征
		stars = new ConcurrentSkipListMap<>((a1, a2) -> {
			if (a1.getStar() > a2.getStar()) {
				return -1;
			}
			else if (a1.getStar() < a2.getStar()) {
				return 1;
			}

			long aa1 = a1.getOpIdx(3);
			long aa2 = a2.getOpIdx(3);

			if (aa1 < aa2) {
				return -1;
			} else if (aa1 > aa2) {
				return 1;
			}

			return 0;
		});

		// 副本
		scenes = new ConcurrentSkipListMap<>((a1, a2) -> {
			if (a1.getScene() > a2.getScene()) {
				return -1;
			}
			else if (a1.getScene() < a2.getScene()) {
				return 1;
			}
			long aa1 = a1.getOpIdx(4);
			long aa2 = a2.getOpIdx(4);

			if (aa1 < aa2) {
				return -1;
			} else if (aa1 > aa2) {
				return 1;
			}
			return 0;
		});

		// 战斗力
		powers = new ConcurrentSkipListMap<>((a1, a2) -> {
			if (a1.getPower() > a2.getPower()) {
				return -1;
			}
			else if (a1.getPower() < a2.getPower()) {
				return 1;
			}
			long aa1 = a1.getOpIdx(5);
			long aa2 = a2.getOpIdx(5);

			if (aa1 < aa2) {
				return -1;
			} else if (aa1 > aa2) {
				return 1;
			}
			return 0;
		});

		List<OpenRankBean> orbs = openRankBeanRepository.findAll();
		orbs.forEach((orb) ->
		{
			orb.updateOps(orb.hashCode());
			users.put(orb.getId(), orb);
			spends.put(orb, 1);
			charges.put(orb, 1);
			levels.put(orb, 1);
			stars.put(orb, 1);
			scenes.put(orb, 1);
			powers.put(orb, 1);
		});

		inited = true;
	}

	/**
	 * 计算3天的奖励
	 */
	public void calcday3() {
		List<OpenraceConf> ocs = readonly.findOpenraces();
		// 根据配置计算奖励:
		OpenraceConf last = ocs.get(ocs.size() - 1);
		int limit = last.key;

		List<Long> ids = new ArrayList<>();
		List<Long> ids2 = new ArrayList<>();

		synchronized (this) {
			// 充值
			int i = 0;
			Iterator<OpenRankBean> itr = charges.keySet().iterator();
			while (itr.hasNext() && i < limit)  {
				OpenRankBean orb = itr.next();
				ids.add(orb.getId());
				++i;
			}

			// 消费
			i = 0;
			itr = spends.keySet().iterator();
			while (itr.hasNext() && i < limit)  {
				OpenRankBean orb = itr.next();
				ids2.add(orb.getId());
				++i;
			}
		}

		// 发送奖励
		for (int i = 0; i < ids.size(); ++i) {
			if (i < 20) {
				GameServer.getInstance().sendAwardMail(ids.get(i), "开服竞赛", "充值榜第" + (i + 1) + "名", ocs.get(i).recrank);
			}
			else {
				GameServer.getInstance().sendAwardMail(ids.get(i), "开服竞赛", "充值榜第" + (i + 1) + "名", last.recrank);
			}
		}
		for (int i = 0; i < ids2.size(); ++i) {
			if (i < 20) {
				GameServer.getInstance().sendAwardMail(ids2.get(i), "开服竞赛", "消费榜第" + (i + 1) + "名", ocs.get(i).sperank);
			}
			else {
				GameServer.getInstance().sendAwardMail(ids2.get(i), "开服竞赛", "消费榜第" + (i + 1) + "名", last.sperank);
			}
		}
	}

	public void calcday5() {
		List<OpenraceConf> ocs = readonly.findOpenraces();
		// 根据配置计算奖励:
		OpenraceConf last = ocs.get(ocs.size() - 1);
		int limit = last.key;

		List<Long> ids = new ArrayList<>();
		List<Long> ids2 = new ArrayList<>();

		synchronized (this) {
			// 等级
			int i = 0;
			Iterator<OpenRankBean> itr = levels.keySet().iterator();
			while (itr.hasNext() && i < limit)  {
				OpenRankBean orb = itr.next();
				ids.add(orb.getId());
				++i;
			}

			// 远征
			i = 0;
			itr = stars.keySet().iterator();
			while (itr.hasNext() && i < limit)  {
				OpenRankBean orb = itr.next();
				ids2.add(orb.getId());
				++i;
			}
		}

		// 发送奖励
		for (int i = 0; i < ids.size(); ++i) {
			if (i < 20) {
				GameServer.getInstance().sendAwardMail(ids.get(i), "开服竞赛", "等级榜第" + (i + 1) + "名", ocs.get(i).levrank);
			}
			else {
				GameServer.getInstance().sendAwardMail(ids.get(i), "开服竞赛", "等级榜第" + (i + 1) + "名", last.levrank);
			}
		}
		for (int i = 0; i < ids2.size(); ++i) {
			if (i < 20) {
				GameServer.getInstance().sendAwardMail(ids2.get(i), "开服竞赛", "远征榜第" + (i + 1) + "名", ocs.get(i).firrank); }
			else {
				GameServer.getInstance().sendAwardMail(ids2.get(i), "开服竞赛", "远征榜第" + (i + 1) + "名", last.firrank);
			}
		}
	}

	public void calcday7() {
		List<OpenraceConf> ocs = readonly.findOpenraces();
		// 根据配置计算奖励:
		OpenraceConf last = ocs.get(ocs.size() - 1);
		int limit = last.key;

		List<Long> ids = new ArrayList<>();
		List<Long> ids2 = new ArrayList<>();

		synchronized (this) {
			// 战力
			int i = 0;
			Iterator<OpenRankBean> itr = powers.keySet().iterator();
			while (itr.hasNext() && i < limit)  {
				OpenRankBean orb = itr.next();
				ids.add(orb.getId());
				++i;
			}

			// 副本
			i = 0;
			itr = scenes.keySet().iterator();
			while (itr.hasNext() && i < limit)  {
				OpenRankBean orb = itr.next();
				ids2.add(orb.getId());
				++i;
			}
		}

		// 发送奖励
		for (int i = 0; i < ids.size(); ++i) {
			if (i < 20) {
				GameServer.getInstance().sendAwardMail(ids.get(i), "开服竞赛", "战力榜第" + (i + 1) + "名", ocs.get(i).powerrank);
			}
			else {
				GameServer.getInstance().sendAwardMail(ids.get(i), "开服竞赛", "战力榜第" + (i + 1) + "名", last.powerrank);
			}
		}
		for (int i = 0; i < ids2.size(); ++i) {
			if (i < 20) {
				GameServer.getInstance().sendAwardMail(ids2.get(i), "开服竞赛", "副本榜第" + (i + 1) + "名", ocs.get(i).raidrank);
			}
			else {
				GameServer.getInstance().sendAwardMail(ids2.get(i), "开服竞赛", "副本榜第" + (i + 1) + "名", last.raidrank);
			}
		}
	}

	/**
	 *
	 * 1. 每秒钟检查 三日，五日，七日奖励是否发送，如果没有，是否已经到时，并进行发送任务
	 *
	 * 2. 每隔一定的时间讲变动的数据进行保存
	 *
	 */
	@Scheduled(fixedDelay = 1000)
	void schedule() {
		if (!inited) {
			return;
		}

		long now = System.currentTimeMillis();
		if (!localService.getDay3() && now >= day3) {
			// 发送三日奖励
			calcday3();
			localService.setDay3();
		}

		if (!localService.getDay5() && now >= day5) {
			// 发送五日奖励
			calcday5();
			localService.setDay5();
		}

		if (!localService.getDay7() && now >= day7) {
			// 发送七日奖励
			calcday7();
			localService.setDay7();
		}

		// 20多分钟进行一次保存
		if (++cum >= 1000) {
			save();
		}
	}

	/**
	 *
	 * 保存: 将变动的数据刷新到数据库中
	 *
	 */
	synchronized void save() {
		changes.forEach((uid) -> {
			OpenRankBean orb = users.getOrDefault(uid, null);
			if (orb != null) {
				openRankBeanRepository.save(orb);
			}
		});
		changes.clear();
	}

	/**
	 * 玩家改名的时候调用,将信息刷进去，如果不存在，则新增玩家进入到榜单最后一名
	 *
	 * 登录的第一时间也会刷新玩家的基本信息
	 *
	 * @param uid   玩家id
	 * @param name  玩家昵称
	 *
	 *
	 */
	public synchronized void flushInfo(long uid, String name) {
//		if (System.currentTimeMillis() >= day7) {
//			return;
//		}

		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			orb.updateOps(System.currentTimeMillis());
			users.put(uid, orb);
			charges.put(orb, 1);
			spends.put(orb, 1);
			powers.put(orb, 1);
			scenes.put(orb, 1);
			stars.put(orb, 1);
			levels.put(orb, 1);
		}

		String old = orb.getName();
		if (old == null || !old.equals(name)) {
			orb.setName(name);
			changes.add(orb.getId());
		}
	}


	public synchronized void putCharge(long uid, int rmb) {
//		if (System.currentTimeMillis() >= day3) {
//			return;
//		}
		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			orb.setOpIdx(0);
			users.put(uid, orb);
		} else {
			charges.remove(orb);
		}

		orb.setCharge(orb.getCharge() + rmb);
		charges.put(orb, 1);
		changes.add(uid);
	}

	public synchronized void putSpend(long uid, int spend) {
//		if (System.currentTimeMillis() >= day3) {
//			return;
//		}

		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			orb.setOpIdx(1);
			users.put(uid, orb);
		} else {
			spends.remove(orb);
		}

		orb.setSpend(orb.getSpend() + spend);
		spends.put(orb, 1);
		changes.add(uid);
	}

	public synchronized void putLevel(long uid, int level) {
//		if (System.currentTimeMillis() >= day5) {
//			return;
//		}
		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			orb.setOpIdx(2);
			users.put(uid, orb);
		} else {
			levels.remove(orb);
		}

		if (orb.getLevel() != level) {
			orb.setLevel(level);
			levels.put(orb, 1);
			changes.add(uid);
		}
	}

	public synchronized void putStar(long uid, int star) {
//		if (System.currentTimeMillis() >= day5) {
//			return;
//		}
		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			orb.setOpIdx(3);
			users.put(uid, orb);
		} else {
			stars.remove(orb);
		}

		if (orb.getStar() != star) {
			orb.setStar(star);
			stars.put(orb, 1);
			changes.add(uid);
		}
	}

	public synchronized void putScene(long uid, int scene) {
//		if (System.currentTimeMillis() >= day7) {
//			return;
//		}
		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			orb.setOpIdx(4);
			users.put(uid, orb);
		} else {
			scenes.remove(orb);
		}

		orb.setScene(scene);
		scenes.put(orb, 1);
		changes.add(uid);
	}

	public synchronized void putPowers(long uid, long power) {
//		if (System.currentTimeMillis() >= day7) {
//			return;
//		}
		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			orb.setOpIdx(5);
			users.put(uid, orb);
		} else {
			powers.remove(orb);
		}

		orb.setPower(power);
		powers.put(orb, 1);
		changes.add(uid);
	}



	/**
	 * 获取指定的排行榜
	 *
	 * @param mode 指定需要获取的排行榜的模式
	 *
	 * @return
	 *
	 * 返回一个列表即为排行榜
	 *
	 */
	public List<NameValue> getRanks(int mode) {
		List<NameValue> rtn = new ArrayList<>();
		switch (mode) {
			case 1:
			{
				int i = 0;
				Iterator<OpenRankBean> itr = charges.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					rtn.add(new NameValue(orb.getName(), orb.getCharge()));
					++i;
				}
				break;
			}
			case 2:
			{
				int i = 0;
				Iterator<OpenRankBean> itr = spends.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					rtn.add(new NameValue(orb.getName(), orb.getSpend()));
					++i;
				}
				break;
			}
			case 3:
			{
				int i = 0;
				Iterator<OpenRankBean> itr = levels.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					rtn.add(new NameValue(orb.getName(), orb.getLevel()));
					++i;
				}
				break;
			}
			case 4:
			{
				int i = 0;
				Iterator<OpenRankBean> itr = stars.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					rtn.add(new NameValue(orb.getName(), orb.getStar()));
					++i;
				}
				break;
			}
			case 5:
			{
				int i = 0;
				Iterator<OpenRankBean> itr = scenes.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					rtn.add(new NameValue(orb.getName(), orb.getScene()));
					++i;
				}
				break;
			}
			case 6:
			{
				int i = 0;
				Iterator<OpenRankBean> itr = powers.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					rtn.add(new NameValue(orb.getName(), orb.getPower()));
					++i;
				}
				break;
			}
		}
		return rtn;
	}


	public boolean copyInfos(long uid, List<NameValue> _powers, List<NameValue> _scenes, List<NameValue> _levels, List<NameValue> _stars, List<NameValue> _charges, List<NameValue> _spends) {
		long now = System.currentTimeMillis();

		OpenRankBean self = users.getOrDefault(uid, null);
		if (self == null) {
			return false;
		}

		if (now < day3) {
			{
				int i = 0;
				int rank = -1;
				Iterator<OpenRankBean> itr = charges.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					_charges.add(new NameValue(orb.getName(), orb.getCharge() / 100));
					if (orb == self) {
						rank = i;
					}
					++i;
				}
				_charges.add(new NameValue(self.getName(), self.getCharge() / 100, rank));
			}


			{
				int i = 0;
				int rank = -1;
				Iterator<OpenRankBean> itr = spends.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					_spends.add(new NameValue(orb.getName(), orb.getSpend()));
					if (orb == self) {
						rank = i;
					}
					++i;
				}

				_spends.add(new NameValue(self.getName(), self.getSpend(), rank));
			}
		}

		{
			{
				int i = 0;
				int rank = -1;
				Iterator<OpenRankBean> itr = levels.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					_levels.add(new NameValue(orb.getName(), orb.getLevel()));
					if (orb == self) {
						rank = i;
					}
					++i;
				}

				_levels.add(new NameValue(self.getName(), self.getLevel(), rank));
			}

			{
				int i = 0;
				int rank = -1;
				Iterator<OpenRankBean> itr = stars.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					_stars.add(new NameValue(orb.getName(), orb.getStar()));
					if (orb == self) {
						rank = i;
					}
					++i;
				}

				_stars.add(new NameValue(self.getName(), self.getStar(), rank));
			}
		}

		{
			{
				int i = 0;
				int rank = -1;
				Iterator<OpenRankBean> itr = powers.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					_powers.add(new NameValue(orb.getName(), orb.getPower()));
					if (orb == self) {
						rank = i;
					}
					++i;
				}

				_powers.add(new NameValue(self.getName(), self.getPower(), rank));
			}

			{
				int i = 0;
				int rank = -1;
				Iterator<OpenRankBean> itr = scenes.keySet().iterator();
				while (itr.hasNext() && i < 20)  {
					OpenRankBean orb = itr.next();
					_scenes.add(new NameValue(orb.getName(), orb.getScene()));
					if (orb == self) {
						rank = i;
					}
					++i;
				}

				_scenes.add(new NameValue(self.getName(), self.getScene(), rank));
			}
		}

		return true;
	}

	@PreDestroy
	void dispose() {
		log.warn("--- 系统退出时保存综合榜单..");
		save();
	}
}
