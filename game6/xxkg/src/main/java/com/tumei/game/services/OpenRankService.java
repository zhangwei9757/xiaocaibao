package com.tumei.game.services;

import com.tumei.common.LocalService;
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

/**
 * Created by Leon on 2017/12/13.
 * <p>
 * 开服竞赛的排行, 有的排行也会用在将来
 */
@Service
public class OpenRankService {
	final static Log log = LogFactory.getLog(OpenRankService.class);

	private static OpenRankService _instance;

	public static OpenRankService getInstance() {
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

	/**
	 * 所有玩家的信息
	 */
	private HashMap<Long, OpenRankBean> users = new HashMap<>();

	// 累计一定的次数进行数据库写入
	private int cum;

	private int sortInterval;

	// 发生改变的内容
	private HashSet<Long> changes = new HashSet<>();

	/**
	 * 消费排行 3
	 */
	private List<OpenRankBean> spends = new ArrayList<>();
	/**
	 * 充值排行 3
	 */
	private List<OpenRankBean> charges = new ArrayList<>();
	/**
	 * 远征星级 5
	 */
	private List<OpenRankBean> stars = new ArrayList<>();
	/**
	 * 领主等级 5
	 */
	private List<OpenRankBean> levels = new ArrayList<>();
	/**
	 * 战斗力 7
	 */
	private List<OpenRankBean> powers = new ArrayList<>();
	/**
	 * 副本关卡 7
	 */
	private List<OpenRankBean> scenes = new ArrayList<>();

	// 初始化完成标识
	private boolean inited = false;

	// 比较器
	private Comparator<OpenRankBean> cPower;
	private Comparator<OpenRankBean> cScene;
	private Comparator<OpenRankBean> cLevel;
	private Comparator<OpenRankBean> cStar;
	private Comparator<OpenRankBean> cCharge;
	private Comparator<OpenRankBean> cSpend;

	@PostConstruct
	void init() {
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

		cCharge = (OpenRankBean a1, OpenRankBean a2) -> {
			if (a1.getCharge() > a2.getCharge()) {
				return -1;
			}
			else if (a1.getCharge() < a2.getCharge()) {
				return 1;
			}
			return 0;
		};

		cSpend = (OpenRankBean a1, OpenRankBean a2) -> {
			if (a1.getSpend() > a2.getSpend()) {
				return -1;
			}
			else if (a1.getSpend() < a2.getSpend()) {
				return 1;
			}
			return 0;
		};

		cLevel = (OpenRankBean a1, OpenRankBean a2) -> {
			if (a1.getLevel() > a2.getLevel()) {
				return -1;
			}
			else if (a1.getLevel() < a2.getLevel()) {
				return 1;
			}
			return 0;
		};

		cStar = (OpenRankBean a1, OpenRankBean a2) -> {
			if (a1.getStar() > a2.getStar()) {
				return -1;
			}
			else if (a1.getStar() < a2.getStar()) {
				return 1;
			}
			return 0;
		};

		cScene = (OpenRankBean a1, OpenRankBean a2) -> {
			if (a1.getScene() > a2.getScene()) {
				return -1;
			}
			else if (a1.getScene() < a2.getScene()) {
				return 1;
			}
			return 0;
		};

		cPower = (OpenRankBean a1, OpenRankBean a2) -> {
			if (a1.getPower() > a2.getPower()) {
				return -1;
			}
			else if (a1.getPower() < a2.getPower()) {
				return 1;
			}
			return 0;
		};

		long now = System.currentTimeMillis();

		List<OpenRankBean> orbs = openRankBeanRepository.findAll();
		orbs.forEach((orb) -> users.put(orb.getId(), orb));

//		if (now < 3) {
		// 充值
		orbs.sort(cCharge);
		charges.addAll(orbs);

		// 消费
		orbs.sort(cSpend);
		spends.addAll(orbs);
//		}

//		if (now < 5) {
		// 领主等级
		orbs.sort(cLevel);
		levels.addAll(orbs);

		// 远征
		orbs.sort(cStar);
		stars.addAll(orbs);
//		}

		// 副本
		orbs.sort(cScene);
		scenes.addAll(orbs);

		// 战斗力
		orbs.sort(cPower);
		powers.addAll(orbs);
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
			charges.sort(cCharge);
			spends.sort(cSpend);

			// 充值
			int max = Math.min(limit, charges.size());
			for (int i = 0; i < max; ++i) {
				ids.add(charges.get(i).getId());
			}
			// 消费
			max = Math.min(limit, spends.size());
			for (int i = 0; i < max; ++i) {
				ids2.add(spends.get(i).getId());
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
			levels.sort(cLevel);
			stars.sort(cStar);

			// 等级
			int max = Math.min(limit, levels.size());
			for (int i = 0; i < max; ++i) {
				ids.add(levels.get(i).getId());
			}
			// 远征
			max = Math.min(limit, stars.size());
			for (int i = 0; i < max; ++i) {
				ids2.add(stars.get(i).getId());
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
			powers.sort(cPower);
			scenes.sort(cScene);
			
			// 战力
			int max = Math.min(limit, powers.size());
			for (int i = 0; i < max; ++i) {
				ids.add(powers.get(i).getId());
			}
			// 副本
			max = Math.min(limit, scenes.size());
			for (int i = 0; i < max; ++i) {
				ids2.add(scenes.get(i).getId());
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
	 * 每分钟进行检测活动是否结束, 所以发送奖励有一分钟的误差
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

		++sortInterval;

		switch (sortInterval) {
			case 5:
				charges.sort(cCharge);
				spends.sort(cSpend);
				break;
			case 10:
				levels.sort(cLevel);
				stars.sort(cStar);
				break;
			case 15:
				scenes.sort(cScene);
				powers.sort(cPower);
				break;
		}

		if (sortInterval > 15) {
			sortInterval = 0;
		}
	}

	synchronized void save() {
		changes.forEach((uid) -> {
			OpenRankBean orb = users.getOrDefault(uid, null);
			if (orb != null) {
				openRankBeanRepository.save(orb);
			}
		});
		changes.clear();
	}

	public synchronized void flushInfo(long uid, String name) {
//		if (System.currentTimeMillis() >= day7) {
//			return;
//		}

		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			users.put(uid, orb);
			charges.add(orb);
			spends.add(orb);
			powers.add(orb);
			scenes.add(orb);
			stars.add(orb);
			levels.add(orb);
		}

		String old = orb.getName();
		if (old == null || !old.equals(name)) {
			orb.setName(name);
			changes.add(orb.getId());
		}
	}

	public synchronized void putPowers(long uid, long power) {
//		if (System.currentTimeMillis() >= day7) {
//			return;
//		}
		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			users.put(uid, orb);
			powers.add(orb);
		}
		if (orb.getPower() != power) {
			orb.setPower(power);
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
			users.put(uid, orb);
			scenes.add(orb);
		}

		if (orb.getScene() != scene) {
			orb.setScene(scene);
			changes.add(uid);
		}
	}

	public synchronized void putLevel(long uid, int level) {
//		if (System.currentTimeMillis() >= day5) {
//			return;
//		}
		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			users.put(uid, orb);
			levels.add(orb);
		}

		if (orb.getLevel() != level) {
			orb.setLevel(level);
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
			users.put(uid, orb);
			stars.add(orb);
		}
		if (orb.getStar() != star) {
			orb.setStar(star);
			changes.add(uid);
		}
	}

	public synchronized void putCharge(long uid, int rmb) {
//		if (System.currentTimeMillis() >= day3) {
//			return;
//		}
		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			users.put(uid, orb);
			charges.add(orb);
		}
		orb.setCharge(orb.getCharge() + rmb);
		changes.add(uid);
	}

	public synchronized void putSpend(long uid, int spend) {
//		if (System.currentTimeMillis() >= day3) {
//			return;
//		}
		OpenRankBean orb = users.getOrDefault(uid, null);
		if (orb == null) {
			orb = new OpenRankBean(uid);
			users.put(uid, orb);
			spends.add(orb);
		}
		orb.setSpend(orb.getSpend() + spend);
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
				Iterator<OpenRankBean> itr = charges.iterator();
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
				Iterator<OpenRankBean> itr = spends.iterator();
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
				Iterator<OpenRankBean> itr = levels.iterator();
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
				Iterator<OpenRankBean> itr = stars.iterator();
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
				Iterator<OpenRankBean> itr = scenes.iterator();
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
				Iterator<OpenRankBean> itr = powers.iterator();
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

	public synchronized boolean copyInfos(long uid, List<NameValue> _powers, List<NameValue> _scenes, List<NameValue> _levels, List<NameValue> _stars, List<NameValue> _charges, List<NameValue> _spends) {
		long now = System.currentTimeMillis();

		OpenRankBean self = users.getOrDefault(uid, null);
		if (self == null) {
			return false;
		}

		if (now < day3) {
			int max = Math.min(charges.size(), 20);
			for (int i = 0; i < max; ++i) {
				OpenRankBean orb = charges.get(i);
				_charges.add(new NameValue(orb.getName(), orb.getCharge()/100));
			}
			int rank = charges.indexOf(self);
			_charges.add(new NameValue(self.getName(), self.getCharge()/100, rank));

			max = Math.min(spends.size(), 20);
			for (int i = 0; i < max; ++i) {
				OpenRankBean orb = spends.get(i);
				_spends.add(new NameValue(orb.getName(), orb.getSpend()));
			}
			rank = spends.indexOf(self);
			_spends.add(new NameValue(self.getName(), self.getSpend(), rank));
		}

		{
			int max = Math.min(levels.size(), 20);
			for (int i = 0; i < max; ++i) {
				OpenRankBean orb = levels.get(i);
				_levels.add(new NameValue(orb.getName(), orb.getLevel()));
			}
			int rank = levels.indexOf(self);
			_levels.add(new NameValue(self.getName(), self.getLevel(), rank));

			max = Math.min(stars.size(), 20);
			for (int i = 0; i < max; ++i) {
				OpenRankBean orb = stars.get(i);
				_stars.add(new NameValue(orb.getName(), orb.getStar()));
			}
			rank = stars.indexOf(self);
			_stars.add(new NameValue(self.getName(), self.getStar(), rank));
		}

		{
			int max = Math.min(powers.size(), 20);
			for (int i = 0; i < max; ++i) {
				OpenRankBean orb = powers.get(i);
				_powers.add(new NameValue(orb.getName(), orb.getPower()));
			}
			int rank = powers.indexOf(self);
			_powers.add(new NameValue(self.getName(), self.getPower(), rank));

			max = Math.min(scenes.size(), 20);
			for (int i = 0; i < max; ++i) {
				OpenRankBean orb = scenes.get(i);
				_scenes.add(new NameValue(orb.getName(), orb.getScene()));
			}
			rank = scenes.indexOf(self);
			_scenes.add(new NameValue(self.getName(), self.getScene(), rank));
		}

		return true;
	}

	@PreDestroy
	void dispose() {
		log.warn("--- 系统退出时保存综合榜单..");
		save();
	}
}
