package com.tumei.game.services;

import com.tumei.common.DaoGame;
import com.tumei.common.RemoteService;
import com.tumei.model.*;
import com.tumei.common.Readonly;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/4/11 0011.
 * <p>
 * 抢夺宝物碎片的服务
 *
 * 1. 记录是按照玩家进入系统后提交的当前碎片为准则的（如果抢夺的时候，玩家已经使用完了这个碎片，则不扣除，但是抢夺的玩家只按照机率获得）
 * 2. 记录的是每个玩家的信息，包括玩家上次抢夺的时间
 * 3.
 *
 */
@Service
public class RobService {
	private static RobService sInstance;

	public static RobService getInstance() {
		return sInstance;
	}

	private Log log = LogFactory.getLog(RobService.class);

	@Autowired
	private DaoGame dao;

	@Autowired
	private Readonly readonly;

	@Autowired
	private RemoteService remoteService;

	@Autowired
	private RobBeanRepository robBeanRepository;

	/**
	 * 碎片，反向对应的玩家
	 */
	private HashMap<Integer, Set<Long>> itemMaps = new HashMap<>();

	/**
	 * 保存的所有玩家抢夺信息
	 */
	private HashMap<Long, RobBean> robs = new HashMap<>();

	/**
	 * 变更列表
	 */
	private HashSet<Long> changes = new HashSet<>();

	@PostConstruct
	void init() {
		sInstance = this;
		log.warn("开始初始化竞技场数据，启动的时候从数据库中读取，每隔5分钟进行一次保存");
		List<RobBean> rk = robBeanRepository.findAll();
		synchronized (this) {
			for (RobBean rb : rk) {
				robs.put(rb.getId(), rb);
				// 将碎片反向映射
				mapFrags(rb);
			}
		}
	}

	/**
	 * 初始化时调用，根据玩家碎片，建立反向映射 {碎片id -> {玩家id1, 玩家id2}}
	 * @param rb
	 */
	void mapFrags(RobBean rb) {
		long id = rb.getId();
		HashMap<Integer, Integer> frags = rb.getFrags();
		for (Map.Entry<Integer, Integer> entry : frags.entrySet()) {
			int key = entry.getKey();
			Set<Long> users = itemMaps.getOrDefault(key, null);
			if (users == null) {
				users = new HashSet<>();
				itemMaps.put(key, users);
			}
			users.add(id);
		}
	}

	/**
	 * 玩家提交自己的背包中物品清单，此处更新玩家的数据
	 * @param id
	 */
	public synchronized RobBean updateFrags(long id)  {
		PackBean pb = DaoGame.getInstance().findPack(id);
		HashMap<Integer, Integer> items = pb.copyRobItems();

		RobBean rb = robs.getOrDefault(id, null);

		if (rb == null) {
			rb = new RobBean(id);
			robs.put(id, rb);
		}

		HashMap<Integer, Integer> frags = rb.getFrags();

		items.forEach((key, c) -> {
			if (c > 0) {
//				int count = frags.getOrDefault(key, 0);
//				if (count <= 0) { // 如果原来碎片没有，这次就增加映射
					Set<Long> users = itemMaps.getOrDefault(key, null);
					if (users == null) {
						users = new HashSet<>();
						itemMaps.put(key, users);
					}
					users.add(id);
//				}

				frags.put(key, c);
			}
		});

		// 还有一个情况是原来有的，现在没有了
		// 删除反向列表
		List<Integer> removed = new ArrayList<>();
		Iterator<Integer> itr = frags.keySet().iterator();
		while (itr.hasNext()) {
			int key = itr.next();
			int cc = items.getOrDefault(key, 0);
			if (cc <= 0) {
				removed.add(key);
				// 删除反向列表
				Set<Long> users = itemMaps.getOrDefault(key, null);
				if (users != null) {
					users.remove(id);
				}
			}
		}
		for (int key : removed) {
			frags.remove(key);
		}

		dirty(id);
		return rb;
	}

	/**
	 * 根据碎片搜寻玩家 (保护的玩家不被搜寻到)
	 *
	 * @param item
	 * @return
	 */
	public synchronized Long findByItem(long self, int item) {
		Set<Long> users = itemMaps.getOrDefault(item, null);
		if (users == null || users.size() <= 0) {
			return 0L;
		}

		List<Long> us = users.stream().filter((id) -> (id != self) && !robs.get(id).isProtect()).collect(Collectors.toList());
		if (us.size() <= 0) {
			return 0L;
		}
		Collections.shuffle(us);
		return us.get(0);
	}

	/**
	 * 根据碎片id,获取所有可能的玩家
	 * @param item
	 * @return
	 */
	public synchronized List<Long> findAllByItem(int item) {
		List<Long> list = new ArrayList<>();

		Set<Long> users = itemMaps.getOrDefault(item, null);
		if (users == null || users.size() <= 0) {
			return list;
		}
		users.forEach( u -> {
			list.add(u);
		});

		return list;
	}

	/**
	 * 抢劫玩家碎片，提交碎片消失申请
	 * @param id
	 */
	public synchronized boolean commitFrags(long id, int item, int val) {
		if (val == 0) {
			return false;
		}

		RobBean rb = robs.getOrDefault(id, null);
		if (rb == null) {
			return false;
		}

		HashMap<Integer, Integer> frags = rb.getFrags();

		int count = frags.getOrDefault(item, 0);
		count += val;

		if (count > 0) {
			frags.put(item, count);
		}

		if (count <= 0) { // 碎片不大于0，减少反向索引
			Set<Long> users = itemMaps.getOrDefault(item, null);
			if (users != null) {
				users.remove(id);
			}
		} else { // 如果碎片大于0，增加反向索引
			Set<Long> users = itemMaps.getOrDefault(item, null);
			if (users == null) {
				users = new HashSet<>();
				users.add(id);
				itemMaps.put(item, users);
			}
		}

		dirty(id);
		return true;
	}

	@Scheduled(fixedRate = 1000000)
	void update() {
		// save all changed ranks
		try {
			saveChanges();
		} catch (Exception ex) {
			log.error("宝物碎片抢夺服务，保存失败.");
		}
	}

	@PreDestroy
	void dispose() {
		log.warn("服务退出的时候保存当前宝物碎片抢夺服务.");
		try {
			saveChanges();
		} catch (Exception ex) {
			log.error("宝物碎片抢夺服务，保存失败.");
		}
	}


	/**
	 * 根据变动列表，将发生变化的数据保存
	 */
	void saveChanges() {
		synchronized (this) {
			for (Long index : changes) {
				RobBean rb = robs.get(index);
				robBeanRepository.save(rb);
			}
			changes.clear();
		}
	}

	public synchronized void dirty(long uid) {
		changes.add(uid);
	}

	/**
	 * 获取抢劫信息
	 * @param uid
	 * @return
	 */
	public synchronized RobBean findRob(long uid) {
		RobBean rb = robs.getOrDefault(uid, null);
		if (rb == null) {
			robs.put(uid, new RobBean(uid));
		}

		return rb;
	}

	@Override
	public synchronized String toString() {
		List<String> vl = new ArrayList<>();

		robs.forEach((k, v) -> {
			String msg = "玩家(" + k + ") 索引信息:" + v.toString();
			vl.add(msg);
		});

		List<String> list = new ArrayList<>();

//		private HashMap<Integer, Set<Long>> itemMaps = new HashMap<>();
		itemMaps.forEach((k, v) -> {
			list.add("碎片(" + k + ") 对应玩家:");
			v.forEach(u -> {
				list.add("------ [" + u + "] ");
			});
		});

		return String.join("\n", vl) + "\n\n" + String.join("\n", list);
	}
}
