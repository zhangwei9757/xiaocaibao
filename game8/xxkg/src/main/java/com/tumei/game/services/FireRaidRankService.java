package com.tumei.game.services;

import com.tumei.game.protos.structs.RaidRankStruct;
import com.tumei.model.RaidRankBean;
import com.tumei.model.RaidRankBeanRepository;
import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
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
 *
 * 远征星数排名
 *
 */
@Service
public class FireRaidRankService {
	static final Log log = LogFactory.getLog(FireRaidRankService.class);

	@Autowired
	private DaoGame dao;

	@Autowired
	private Readonly readonly;

	@Autowired
	private RaidRankBeanRepository raidRankBeanRepository;

	/**
	 * 排名
	 */
	private LinkedList<RaidRankBean> ranks = new LinkedList<>();

	/**
	 * 玩家列表
	 */
	private Map<Long, RaidRankBean> users = new HashMap<>();

	@PostConstruct
	void init() {
		log.warn("读取 远征历史星数排名数据，启动的时候从数据库中读取，每隔5分钟进行一次保存");
		List<RaidRankBean> rk = raidRankBeanRepository.findAll(new Sort("rank"));

		synchronized (this) {
			ranks.addAll(rk);
			for (RaidRankBean rb : rk) {
				users.put(rb.getId(), rb);
			}
		}
	}

	@Scheduled(fixedRate = 1000000)
	void update() {
		try {
			save();
		} catch (Exception ex) {
			log.error("燃烧远征副本排名服务器，保存失败.");
		}
	}

	@PreDestroy
	void dispose() {
		log.warn("服务退出的时候保存当前远征排名数据");
		try {
			save();
		} catch (Exception ex) {
			log.error("燃烧远征副本排名服务器，保存失败.");
		}
	}


	/**
	 * 根据变动列表，将发生变化的数据保存
	 */
	void save() {
		synchronized (this) {
			for (RaidRankBean rrb : ranks) {
				raidRankBeanRepository.save(rrb);
			}
		}
	}

	/**
	 * 根据玩家id，获取当前排名
	 * @param id
	 * @return
	 */
	public List<RaidRankStruct> getRanks(long id, String name) {
		List<RaidRankStruct> rbs = new ArrayList<>();
		synchronized (this) {
			// 读取前n名
			int max = 10;
			if (max > ranks.size()) {
				max = ranks.size();
			}
			for (int i = 0; i < max; ++i) {
				RaidRankStruct rrs = new RaidRankStruct(ranks.get(i));
				rbs.add(rrs);
			}

			RaidRankBean rb = users.getOrDefault(id, null);
			if (rb != null) {
				RaidRankStruct rrs = new RaidRankStruct(rb);
				rbs.add(rrs);
			} else {
				RaidRankStruct rrs = new RaidRankStruct(id, 99999, name);
				rbs.add(rrs);
			}
		}

		return rbs;
	}

	/**
	 * 更新玩家对应的总星星数
	 * @param id
	 * @param star
	 */
	public synchronized void fixRank(long id, String name, int star) {
		RaidRankBean rb = users.getOrDefault(id, null);
		if (rb == null) { // 玩家不存在则直接增加,排名为当前总长度
			rb = new RaidRankBean(id, ranks.size(), star);
			ranks.add(rb);
			users.put(id, rb);
		}

		// 此时根据当前的星星数查找对应的位置进行挪动
		int find = rb.getRank();

		int begin = -1;
		for (int i = 0; i < find; ++i) {
			RaidRankBean rrb = ranks.get(i);
			if (rrb.getStar() >= star) {
				continue;
			}

			if (rrb.getId() != id) {
				// 从第一个小于star的排名开始，后续排名全部+1, 一直到玩家当前排名
				if (begin == -1) {
					begin = i;
				}
				rrb.fixRank(1);
			}
		}

		rb.setName(name);
		rb.setStar(star);

		if (begin != -1) {
			rb.setRank(begin);
			if (find >= 0 && find < ranks.size()) {
				ranks.remove(find);
			}
			ranks.add(begin, rb);
		}
	}
}
