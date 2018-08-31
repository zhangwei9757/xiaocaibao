package com.tumei.controller;

import com.tumei.common.DaoUtils;
import com.tumei.common.RemoteService;
import com.tumei.common.group.GroupSimpleStruct;
import com.tumei.common.webio.RankStruct;
import com.tumei.controller.struct.GroupRankStruct;
import com.tumei.model.GroupBean;
import com.tumei.model.GroupBeanRepository;
import com.tumei.model.GroupInfoBean;
import com.tumei.model.GroupInfoBeanRepository;
import com.tumei.modelconf.GroupConf;
import com.tumei.modelconf.Readonly;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Created by Leon on 2017/5/10 0010.
 * <p>
 * 群组服务
 */
@Service
public class GroupService {
	private Log log = LogFactory.getLog(GroupService.class);

	private static GroupService _instance;

	public static GroupService getInstance() {
		return _instance;
	}

	@Autowired
	private GroupInfoBeanRepository groupInfoBeanRepository;

	@Autowired
	private DaoUtils dao;

	@Autowired
	private RemoteService remoteService;

	@Autowired
	private GroupBeanRepository groupBeanRepository;

	public HashMap<Long, GroupBean> groups = new HashMap<>();

	/**
	 * 服务器与公会的对应
	 */
	public HashMap<Integer, List<GroupBean>> zoneGroups = new HashMap<>();
	/**
	 * 公会名字集合，避免重复
	 */
	public HashMap<String, GroupBean> groupNames = new HashMap<>();

	/**
	 * 玩家对应的公会，用于明确一个玩家仅能最多处于1个公会，不用service的锁，自身单独进行线程竞争的控制
	 */
	public ConcurrentHashMap<Long, Long> users = new ConcurrentHashMap<>();

	// 公会等级排行榜
	public List<GroupRankStruct> levelRanks = new ArrayList<>();
	// 公会副本排行榜
	public List<GroupRankStruct> sceneRanks = new ArrayList<>();

	private GroupInfoBean gib;

	@PostConstruct
	void init() {
		_instance = this;
		synchronized (this) {
			log.info("*** 公会数据初始化....");
			List<GroupBean> gbs = groupBeanRepository.findAll();
			for (GroupBean gb : gbs) {
				// 成员至少有一个的公会才有价值
				if (gb.getRoles().size() > 0) {
					gb.getRoles().keySet().forEach(rid -> users.put(rid, gb.getId()));
					addGroup(gb);
				}
			}

			this.gib = groupInfoBeanRepository.findById(1L);
			if (this.gib == null) {
				this.gib = new GroupInfoBean();
			}
			this.sceneRanks = this.gib.getSceneRanks();
			this.levelRanks = this.gib.getLevelRanks();

			levelRanks.sort((o1, o2) -> {
				if (o1.value < o2.value) {
					return 1;
				} else if (o1.value > o2.value) {
					return -1;
				}

				return 0;
			});

			sceneRanks.sort((o1, o2) -> {
				if (o1.value < o2.value) {
					return 1;
				} else if (o1.value > o2.value) {
					return -1;
				}

				return 0;
			});

			log.info("*** 公会数据初始化结束");
		}
	}

	/**
	 * @param mode 0-等级排行 1-副本排行
	 * @return
	 */
	public synchronized List<RankStruct> getRanks(int mode) {
		List<RankStruct> rtn = new ArrayList<>();
		if (mode == 0) {
			for (GroupRankStruct grs : levelRanks) {
				rtn.add(new RankStruct(grs.id, grs.value));
			}
		}
		else {
			for (GroupRankStruct grs : sceneRanks) {
				rtn.add(new RankStruct(grs.id, grs.value));
			}
		}

		return rtn;
	}

	// 提交公会等级与经验变化，查看是否影响前50名
	// @param exp gid对应的当前总经验
	public void submitExpRankObsolete(long gid, int exp) {
		synchronized (levelRanks) {
			int find = -1;
			int count = levelRanks.size();
			if (count > 50) {
				count = 50;
			}

			int equal = -1;

			for (int i = 0; i < count; ++i) {
				GroupRankStruct grs = levelRanks.get(i);
				if (find == -1) {
					if (exp > grs.value) { // 大于则插入当前位置
						find = i;
					}
				}
				if (gid == grs.id) { // 找到相同的公会id，证明是前50的公会更进一步
					grs.value = exp;
					equal = i;
				}
			}

			if (find != -1) { // 首先看是否本次提交导致了50名发生变化
				GroupRankStruct grs = null;
				if (equal == -1) { // 50名内未找到本次提交的公会, 则以最后一名替代
					if (count < 50) { // 没满 新增一个
						grs = new GroupRankStruct(gid, exp);
					}
					else {
						grs = levelRanks.remove(49);
					}
				}
				else {
					grs = levelRanks.remove(equal);
				}

				grs.id = gid;
				grs.value = exp;
				levelRanks.add(find, grs);
			}
			else {
				if (equal == -1 && count < 50) {
					// 提交到最后
					levelRanks.add(new GroupRankStruct(gid, exp));
				}
			}
		}
	}

	public void submitExpRank(long gid, int exp) {
		synchronized (levelRanks) {
			boolean find = false;
			for (GroupRankStruct grs : levelRanks) {
				if (grs.id == gid) {
					grs.value = exp;
					find = true;
				}
			}
			if (!find) {
				levelRanks.add(new GroupRankStruct(gid, exp));
			}

			levelRanks.sort((o1, o2) -> {
				if (o1.value < o2.value) {
					return 1;
				} else if (o1.value > o2.value) {
					return -1;
				}

				return 0;
			});

			if (levelRanks.size() > 50) {
				levelRanks = levelRanks.subList(0, 49);
			}
		}
	}

	public void submitSceneRank(long gid, int scene) {
		synchronized (sceneRanks) {
			boolean find = false;
			for (GroupRankStruct grs : sceneRanks) {
				if (grs.id == gid) {
					grs.value = scene;
					find = true;
				}
			}

			if (!find) {
				sceneRanks.add(new GroupRankStruct(gid, scene));
			}

			sceneRanks.sort((o1, o2) -> {
				if (o1.value < o2.value) {
					return 1;
				} else if (o1.value > o2.value) {
					return -1;
				}

				return 0;
			});

			if (sceneRanks.size() > 50) {
				sceneRanks = sceneRanks.subList(0, 49);
			}
		}
	}

	@PreDestroy
	void dispose() {
		save();
	}

	/**
	 * 延迟一定的时间进行保存
	 */
	@Scheduled(fixedDelay = 1000)
	synchronized void update() {
		save();

		groupNames.forEach((k, v) -> {
			v.update();
		});
	}

	public <K, V> V callRemote(BiFunction<RemoteService, K, V> func, K arg) {
		return func.apply(remoteService, arg);
	}

	/**
	 * 新增公会加入到缓存队列中 3个队列
	 *
	 * @param gb
	 */
	public synchronized void addGroup(GroupBean gb) {
		groups.put(gb.getId(), gb);
		groupNames.put(gb.getName(), gb);
		int zone = (int) (gb.getId() % 1000);
		List<GroupBean> gs = zoneGroups.getOrDefault(zone, null);
		if (gs == null) {
			gs = new ArrayList<>();
			zoneGroups.put(zone, gs);
		}
		gs.add(gb);
	}

	/**
	 * 删除公会缓存, save调用，不用加锁
	 *
	 * @param gb
	 */
	private void removeGroup(GroupBean gb) {
		groups.remove(gb.getId());
		groupNames.remove(gb.getName());
		List<GroupBean> gbs = zoneGroups.get(gb.getZone());
		if (gbs != null) {
			gbs.remove(gb);
		}
	}

	/**
	 * 保存
	 */
	private synchronized void save() {
		if (this.gib == null) {
			this.gib = new GroupInfoBean();
		}
		// 保存排行
		this.gib.setLevelRanks(this.levelRanks);
		this.gib.setSceneRanks(this.sceneRanks);
		groupInfoBeanRepository.save(this.gib);

//		log.info("公会信息开始保存");
		List<GroupBean> dels = new ArrayList<>();
		groups.forEach((gid, gb) -> {
			if (gb != null) {
				gb.save((g) -> {
					// 成员数为0的公会需要去掉
					if (gb.getRoles().size() == 0) {
						dels.add(gb);
					}
					else {
//						log.info("保存公会:" + gb.getId());
						groupBeanRepository.save(gb);
					}
				});
			}
		});

		for (GroupBean gb : dels) {
			removeGroup(gb);
			groupBeanRepository.delete(gb); // 数据库同时删除
		}
	}

	/**
	 * 创建公会
	 *
	 * @param gid
	 * @param name
	 * @return
	 */
	public synchronized GroupBean create(long gid, String name) {
		if (groupNames.containsKey(name)) {
			return null;
		}

		GroupBean gb = GroupBean.create(gid);
		gb.setName(name);

		addGroup(gb);
		return gb;
	}

	public synchronized GroupBean find(long gid) {
		return groups.getOrDefault(gid, null);
	}

	/**
	 * 通过uid找到公会, 如果没有加入公会则返回null
	 * @param uid
	 * @return
	 */
	public synchronized GroupBean findByUid(long uid) {
		long gid = users.getOrDefault(uid, 0L);
		if (gid != 0) {
			return groups.getOrDefault(gid, null);
		}
		return null;
	}

	/**
	 * 客户端查看公会排行的时候，对每个公会的详细信息进行一次填写
	 * @param gid
	 * @param rs
	 */
	public synchronized void fillRankStruct(long gid, RankStruct rs) {
		GroupBean gb = groups.get(gid);
		if (gb != null) {
			rs.icon = gb.getIcon();
			rs.name = gb.getName();
			rs.count = gb.getRoles().size();

			rs.leader = gb.getLeaderName();
			rs.level = gb.getLevel();

			GroupConf gc = Readonly.getInstance().findGroup(gb.getLevel());
			if (gc != null) {
				rs.limit = gc.num;
			}
		}
	}


	public synchronized GroupBean find(String gid) {
		return groupNames.getOrDefault(gid, null);
	}

	/**
	 * 推荐公会
	 *
	 * @param zone
	 * @return
	 */
	public synchronized List<GroupSimpleStruct> findRecommands(int zone) {
		List<GroupSimpleStruct> gss = new ArrayList<>();

		HashSet<Long> already = new HashSet<>();
		int n = 8;
		// 1. 如果需要锁定本服的公会，则先随机一个出来
		List<GroupBean> local = zoneGroups.getOrDefault(zone, null);
		if (local != null) {
			Collections.shuffle(local);
			if (zone > 0) { // 全部取这个区的数据
				if (n > local.size()) {
					n = local.size();
				}
				for (int i = 0; i < n; ++i) {
					GroupBean gb = local.get(i);
					gss.add(gb.createSimpleBody());
					already.add(gb.getId());
				}
//				return gss;
			}
			else {
				GroupBean gb = local.get(0);
				already.add(gb.getId());
				gss.add(gb.createSimpleBody());
			}
		}


		n = 8 - gss.size();
		// 将所有的分组形成列表，然后随机打散
		List<GroupBean> tmp = new ArrayList<>(groups.values());
		Collections.shuffle(tmp);
		for (int i = 0; i < tmp.size(); ++i) {
			GroupBean gb = tmp.get(i);
			if (already.contains(gb.getId())) {
				continue;
			}
			gss.add(gb.createSimpleBody());
			if (gss.size() >= n) {
				break;
			}
		}
		return gss;
	}

	/**
	 * 尝试 将玩家指定的一个公会，如果有另外一个公会也批准了他，则返回false
	 *
	 * @param role
	 * @param gid
	 * @return
	 */
	public long tryGroup(long role, long gid) {
		Long old = users.putIfAbsent(role, gid);
		if (old == null) {
			return 0;
		}
		return old;
	}

	public void leaveGroup(long role, long gid) {
		users.remove(role, gid);
	}

	public boolean isInGroup(long role, long gid) {
		return (users.getOrDefault(role, 0L) == gid);
	}
}
