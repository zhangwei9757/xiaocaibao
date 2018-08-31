package com.tumei;

import com.tumei.centermodel.MobBean;
import com.tumei.centermodel.MobBeanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Leon on 2018/3/27.
 *
 * 每个小时一个数组内记录,超过24个小时,就将之前的一个数组清空, 在24个数组内进行归因
 *
 */
@Service
public class MobService {
	/**
	 * 起始编号
	 */
	private int gno;

	// 点击次数
	private int clicks;
	// 命中
	private int gotcha;

	/**
	 * 分组清空
	 */
	private List<List<MobBean>> mobs = new ArrayList<>();

	private List<MobBean> current = null;

	@Autowired
	private MobBeanRepository mobBeanRepository;

	@PostConstruct
	void init() {
		gno = (int)mobBeanRepository.count();
		current = new ArrayList<>();
		mobs.add(current);
	}

	@PreDestroy
	void dispose() {
		synchronized (this) {
			for (List<MobBean> layer : mobs) {
				for (MobBean mb : layer) {
					mobBeanRepository.save(mb);
				}
			}
		}
	}

	/**
	 * 每个小时清空一个mobs
	 */
	@Scheduled(fixedDelay = 3600_000)
	void update() {
		synchronized (this) {
			if (mobs.size() >= 24) {
				List<MobBean> first = mobs.remove(0);
				// 将第一个保存到数据库
				for (MobBean mb : first) {
					mobBeanRepository.save(mb);
				}
			}

			if (mobs.size() < 24) {
				current = new ArrayList<>();
				mobs.add(current);
			}
		}
	}

	/**
	 * 增加一个点击
	 */
	public synchronized void addClick(String clickid, String s1, String idfa, String ipos, String callback) {
		++gno;
		MobBean mobBean = new MobBean();
		mobBean.setNo(gno);
		mobBean.setClickid(clickid);
		mobBean.setCallback(callback);
		mobBean.setIp(ipos);
		mobBean.setTs(new Date());
		mobBean.setIdfa(idfa);
		mobBean.setS1(s1);
//		mobBean.setOs(os);
		mobBean.setHashCode(ipos.hashCode());
		current.add(mobBean);
		++clicks;
	}

	/**
	 * 尝试匹配一个ip
	 * @param ipos
	 * @param account
	 * @return
	 */
	public synchronized MobBean conclude(String ipos, long account) {
		int hash = ipos.hashCode();
		for (int i = mobs.size() - 1; i >= 0; --i) {
			List<MobBean> layer = mobs.get(i);
			for (int j = layer.size() - 1; j >= 0; --j) {
				MobBean mb = layer.get(j);
				if (mb.getHashCode() == hash) {
					if (mb.getAccount() == 0) {
						// 命中
						mb.setAccount(account);
						++gotcha;
						return mb;
					}
					return null;
				}
			}
		}
		return null;
	}

	public synchronized String analyse() {
		return String.format("点击总次数:%d, 命中次数:%d, 转化率:%f", clicks, gotcha,  ((float)gotcha)/clicks);
	}

	public synchronized String view() {
		if (current.size() > 0) {
			return current.get(current.size() - 1).toString();
		}

		for (int i = mobs.size() - 1; i >= 0; --i) {
			List<MobBean> layer = mobs.get(i);
			for (int j = layer.size() - 1; j >= 0; --j) {
				MobBean mb = layer.get(j);
				return mb.toString();
			}
		}
		return "暂无数据";
	}
}
