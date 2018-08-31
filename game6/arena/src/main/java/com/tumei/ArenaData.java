package com.tumei;

import com.tumei.common.fight.HeroStruct;
import com.tumei.common.fight.PowerStruct;
import com.tumei.configs.MongoTemplateConfig;
import com.tumei.configs.RemoteService;
import com.tumei.model.*;
import com.tumei.modelconf.Readonly;
import com.tumei.modelconf.RobotConf;
import com.tumei.modelconf.RobotConfRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.text.html.Option;
import java.util.*;

/**
 * Created by Leon on 2017/11/24 0024.
 *
 * 数据统一保存在java文件中，热更新不能导致数据不一致
 *
 * 这个bean是保存数据用的，他不带任何业务相关的锁, 在开始的时候从数据库拉取数据，在结束的时候保存一次数据
 *
 */
@Component
public class ArenaData {
	static final Log log = LogFactory.getLog(ArenaData.class);

	@Value("${runbean.zone}")
	private int zone;

	/**
	 * 排名
	 */
	private List<ArenaRoleBean> ranks = new LinkedList<>();

	/**
	 * 玩家列表
	 */
	private Map<Long, ArenaRoleBean> users = new HashMap<>();

	/**
	 * 发生变动的排名
	 */
	private Set<Long> changes = new HashSet<>();

	/**
	 * 所有分组
	 */
	private List<ArenaSlotBean> slots = new ArrayList<>();

	/**
	 * slot的改变记录
	 */
	private Set<Integer> slotChanges = new HashSet<>();

	@Autowired
	private ArenaRoleBeanRepository arenaRoleBeanRepository;

	@Autowired
	private ArenaSlotBeanRepository arenaSlotBeanRepository;

	@Autowired
	private RobotConfRepository robotConfRepository;

	@Autowired
	private MongoTemplateConfig mongoTemplateConfig;

	@Autowired
	private Readonly readonly;

	// 从Local下面的ArenaRoles中拷贝数据过来
	boolean fillFromLocal() {
		try {
			MongoTemplate other = mongoTemplateConfig.otherTemplate("local");
			List<ArenaRoleBean> ars = other.findAll(ArenaRoleBean.class);
			arenaRoleBeanRepository.save(ars);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	void fillRobots() {
		List<ArenaRoleBean> rk = arenaRoleBeanRepository.findAll(new Sort("rank"));
		// Role.Ranks 为空的时候，需要创建1000个机器人
		if (rk.size() == 0) {
			List<RobotConf> rcs = robotConfRepository.findAll(new Sort("key"));
			int start = 0;
			for (RobotConf rc : rcs) {
				for (int i = start; i < rc.rank; ++i) {
					ArenaRoleBean rb = new ArenaRoleBean(i, i);
					rb.setName(readonly.randomName());
					List<Integer> heros = readonly.randHerosByList(6, rc.quality);
					rb.setIcon(heros.get(0));
					rb.setLevel(rc.level);

					List<HeroStruct> hss = rb.getFormation();
					for (int j = 0; j < 6; ++j) {
						HeroStruct hb = new HeroStruct();
						hb.hero = heros.get(j);
						hb.level = rc.level;
						hb.grade = rc.grade;
						hss.add(hb);
					}

					PowerStruct ts = new PowerStruct();
					ts.setHeros(hss);

//					long power = remoteService.callPower(ts);
//					rb.setPower(power);

					rk.add(rb);
					arenaRoleBeanRepository.save(rb);
				}
				start = rc.rank;
			}
		}
	}

	@PostConstruct
	void init() {
		log.warn("开始初始化竞技场数据，启动的时候从数据库中读取，每隔5分钟进行一次保存.");
		List<ArenaRoleBean> rk = arenaRoleBeanRepository.findAll(new Sort("rank"));
		if (rk.size() <= 0) {
			fillFromLocal();
			rk = arenaRoleBeanRepository.findAll(new Sort("rank"));
		}

		ranks.addAll(rk);
		for (ArenaRoleBean rb : rk) {
			users.put(rb.getId(), rb);
		}

		slots = arenaSlotBeanRepository.findAll(new Sort("slot"));

		int count = readonly.findTopRankConf(1).newslot[zone-1];
		if (slots.size() == 0) {
			for (int i = 0; i < count; ++i) {
				createSlot(i);
			}
		}

		// 数据库slots数量大于 配置的slot,删除多余的部分
		if (slots.size() > count) {
			slots.removeIf((asb) -> asb.getSlot() >= count);
		}

		log.info("---- 竞技场数据导入完成: ranks:" + ranks.size());
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

	/**
	 * 可以动态创建slot
	 * @param id
	 */
	public void createSlot(int id) {
		Optional<ArenaSlotBean> opt = slots.stream().filter(asb -> asb.getSlot() == id ).findFirst();
		ArenaSlotBean asb;

		if (opt.isPresent()) {
			asb = opt.get();
			asb.setGroups(new LadderGroup[7]);
		} else {
			asb = new ArenaSlotBean();
			asb.setSlot(id);
			slots.add(asb);
		}

		// 王者
		LadderGroup lg0 = new LadderGroup();
		lg0.setGroup(0);
		for (long k = 1; k < 2; ++k) {
			lg0.getRoles().add(k);
		}
		asb.getGroups()[0] = lg0;


		// 钻石
		LadderGroup lg1 = new LadderGroup();
		lg1.setGroup(1);
		for (long k = 2; k < 6; ++k) {
			lg1.getRoles().add(k);
		}
		asb.getGroups()[1] = lg1;

		// 铂金
		LadderGroup lg2 = new LadderGroup();
		lg2.setGroup(2);
		for (long k = 6; k < 12; ++k) {
			lg2.getRoles().add(k);
		}
		asb.getGroups()[2] = lg2;

		// 黄金
		LadderGroup lg3 = new LadderGroup();
		lg3.setGroup(3);
		for (long k = 12; k < 24; ++k) {
			lg3.getRoles().add(k);
		}
		asb.getGroups()[3] = lg3;

		// 白银
		LadderGroup lg4 = new LadderGroup();
		lg4.setGroup(4);
		for (long k = 24; k < 42; ++k) {
			lg4.getRoles().add(k);
		}
		asb.getGroups()[4] = lg4;

		// 青铜
		LadderGroup lg5 = new LadderGroup();
		lg5.setGroup(5);
		for (long k = 42; k < 66; ++k) {
			lg5.getRoles().add(k);
		}
		asb.getGroups()[5] = lg5;

		// 备战
		LadderGroup lg6 = new LadderGroup();
		lg6.setGroup(6);
		asb.getGroups()[6] = lg6;

		slotChanges.add(id);
	}

	/**
	 * 根据变动列表，将发生变化的数据保存
	 */
	public void saveChanges() {
		for (Long index : changes) {
			ArenaRoleBean rb = users.get(index);
			arenaRoleBeanRepository.save(rb);
		}
		changes.clear();

		for (int index : slotChanges) {
			slots.stream().filter(asb -> asb.getSlot() == index).findFirst().ifPresent(slot -> {
				if (slot != null) {
					arenaSlotBeanRepository.save(slot);
				}
			});
		}

		slotChanges.clear();
	}

	public ArenaRoleBean findUser(long uid) {
		return users.getOrDefault(uid, null);
	}

	/**
	 * 根据排名查找玩家
	 * @param rank [0,...]
	 * @return
	 */
	public ArenaRoleBean findRank(int rank) {
		if (rank < 0 || rank >= ranks.size()) {
			return null;
		}

		return ranks.get(rank);
	}

	/**
	 * 交换2个角色的排名
	 *
	 * @param self
	 * @param selfRank
	 * @param other
	 * @param rank
	 */
	public void exchangeRank(ArenaRoleBean self, int selfRank, ArenaRoleBean other, int rank) {
		ranks.set(selfRank, other);
		ranks.set(rank, self);

		changes.add(self.getId());
		changes.add(other.getId());
	}

	public void dirty(long uid) {
		changes.add(uid);
	}

	public void dirtySlot(int slot) {
		slotChanges.add(slot);
	}

	public ArenaSlotBean getSlot(int index) {
		return slots.get(index);
	}

	public long getSlotGroupRole(int slot, int group, int index) {
		return slots.get(slot).getGroups()[group].getRoles().get(index);
	}

	public void setSlotGroupRole(int slot, int group, int index, long uid) {
		slots.get(slot).getGroups()[group].getRoles().set(index, uid);
	}

	/**
	 * 一共多少个slot
	 * @return
	 */
	public int slotsSize() {
		return slots.size();
	}

	public int rankSize() {
		return ranks.size();
	}

	public void addRank(ArenaRoleBean role) {
		ranks.add(role);
	}

	public void addUser(ArenaRoleBean role) {
		users.put(role.getId(), role);
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}
}
