package com.tumei.game.services;

import com.tumei.common.DaoService;
import com.tumei.game.protos.structs.MailCacheStruct;
import com.tumei.game.protos.structs.TreasureRankStruct;
import com.tumei.model.MailsBean;
import com.tumei.model.TreasureRankBean;
import com.tumei.model.TreasureRankBeanRepository;
import com.tumei.common.Readonly;
import com.tumei.modelconf.DtrankConf;
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
 * 神秘宝藏排名
 *
 */
@Service
public class TreasureRankService {
	private final static Log log = LogFactory.getLog(TreasureRankService.class);

	private static TreasureRankService _instance;

	public static TreasureRankService getInstance() {
		return _instance;
	}

	@Autowired
	private DaoService dao;

	@Autowired
	private Readonly readonly;

	@Autowired
	private TreasureRankBeanRepository treasureRankBeanRepository;

	/**
	 * 排名
	 */
	private LinkedList<TreasureRankBean> ranks = new LinkedList<>();

	/**
	 * 玩家列表
	 */
	private Map<Long, TreasureRankBean> users = new HashMap<>();

	@PostConstruct
	void init() {
		_instance = this;
		log.warn("读取 神秘宝藏排名数据，启动的时候从数据库中读取，每隔5分钟进行一次保存");
		List<TreasureRankBean> rk = treasureRankBeanRepository.findAll(new Sort("rank"));

		synchronized (this) {
			ranks.addAll(rk);
			for (TreasureRankBean rb : rk) {
				users.put(rb.getId(), rb);
			}
		}
	}

	/***
	 * 发放奖励，终结服务的时间点
	 */
	@Scheduled(cron = "0 0 0 ? * MON")
	void keyframe() {
		try {
			List<MailCacheStruct> mcss = new ArrayList<>();
			List<DtrankConf> dcs = Readonly.getInstance().getDtrankConfs();
			synchronized (this)
			{
				int max = ranks.size();
				int begin = 1;

				for (DtrankConf dc : dcs) {
					// dc.key是当前阶段奖励的结束排名
					for (int i = begin; i <= dc.key && i <= max; ++i) {
						TreasureRankBean trb = ranks.get(i - 1);

						MailCacheStruct mcs = new MailCacheStruct(trb.getId(), "神秘宝藏奖励",
						"由于上周您在神秘宝藏积分排名中位于[" + trb.getRank() + "]名，特发送以下奖励!");

						// 1. 发送普通奖励
						mcs.addAward(dc.reward1);

						// 2. 满足条件发送特殊奖励，不小于3000积分
						if (dc.limit != 0 && trb.getScore() >= dc.limit) {
							mcs.addAward(dc.reward2);
						}

						mcss.add(mcs);
					}
					begin = dc.key + 1;
				}

				ranks.clear();
				users.clear();
				// 数据同步删除，清空记录,新的一周重新开始.
				treasureRankBeanRepository.deleteAll();
			}

			for (MailCacheStruct mcs : mcss) {
				MailsBean msb = dao.findMails(mcs.id);
				msb.addAwardMail(mcs.title, mcs.content, mcs.awards);
			}
		} catch (Exception ex) {
			log.error("周一神秘宝藏清理,服务出错.");
		}
	}

	@Scheduled(fixedRate = 1000000)
	void update() {
		try {
			save();
		} catch (Exception e) {
			log.error("神秘宝藏排名服务保存失败.");
		}
	}

	@PreDestroy
	void dispose() {
		log.warn("----- 服务退出的时候保存当前神秘宝藏排名.");
		try {
			save();
		} catch (Exception e) {
			log.error("神秘宝藏排名服务保存失败.");
		}
	}

	/**
	 * 根据变动列表，将发生变化的数据保存
	 */
	void save() {
		synchronized (this) {
			for (TreasureRankBean rrb : ranks) {
				treasureRankBeanRepository.save(rrb);
			}
		}
	}

	/***
	 * 获取自己和前n名的数据
	 *
	 * @param id
	 * @param name
	 * @return
	 */
	public List<TreasureRankStruct> getRanks(long id, String name) {
		List<TreasureRankStruct> rbs = new ArrayList<>();
		synchronized (this) {
			// 读取前n名
			int max = 10;
			if (max > ranks.size()) {
				max = ranks.size();
			}
			for (int i = 0; i < max; ++i) {
				TreasureRankStruct rrs = new TreasureRankStruct(ranks.get(i));
				rbs.add(rrs);
			}

			TreasureRankBean rb = users.getOrDefault(id, null);
			if (rb != null) {
				TreasureRankStruct rrs = new TreasureRankStruct(rb);
				rbs.add(rrs);
			} else {
				TreasureRankStruct rrs = new TreasureRankStruct(id, 99999, name);
				rbs.add(rrs);
			}
		}

		return rbs;
	}

	public synchronized int getRank(long id) {
		TreasureRankBean trb = users.getOrDefault(id, null);
		if (trb == null) {
			return -1;
		}
		else {
			return trb.getRank();
		}
	}

	/**
	 *
	 * @param id
	 * @param score
	 */
	public synchronized void fixRank(long id, String name, int score) {
		TreasureRankBean rb = users.getOrDefault(id, null);
		if (rb == null) { // 玩家不存在则直接增加,排名为当前总长度
			rb = new TreasureRankBean(id, ranks.size(), score);
			ranks.add(rb);
			users.put(id, rb);
		}

		// 此时根据当前的星星数查找对应的位置进行挪动
		int find = rb.getRank();

		int begin = -1;
		for (int i = 0; i < find; ++i) {
			TreasureRankBean rrb = ranks.get(i);
			if (rrb.getScore() >= score) {
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

		// 更新积分
		rb.setName(name);
		rb.setScore(score);

		// 将当前位置进行变化
		if (begin != -1) {
			rb.setRank(begin);
			ranks.remove(find);
			ranks.add(begin, rb);
		}

	}
}
