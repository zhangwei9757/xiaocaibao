package com.tumei.common;

import com.tumei.GameConfig;
import com.tumei.centermodel.ServerBean;
import com.tumei.centermodel.ServerBeanRepository;
import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameServer;
import com.tumei.model.ServerInfoBean;
import com.tumei.model.ServerInfoBeanRepository;
import com.tumei.modelconf.CumrechargeConf;
import com.tumei.modelconf.ExchangeConf;
import com.tumei.modelconf.SaleConf;
import com.tumei.modelconf.SinglerechargeConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/12/23 0023.
 * <p>
 * 本地服务器数据库工具
 */
@Component
public class LocalService {
	private static LocalService _instance = null;

	public static LocalService getInstance() {
		return _instance;
	}

	private static final Log log = LogFactory.getLog(LocalService.class);

	@Autowired
	private Readonly readonly;

	@Autowired
	private GameConfig gameConfig;

	/**
	 * 充值活动和一些与服务器开服时间同步的活动需要全局配置
	 **/
	@Autowired
	private ServerInfoBeanRepository serverInfoBeanRepository;
	private ServerInfoBean serverInfoBean;

	@Autowired
	private ServerBeanRepository serverBeanRepository;


	/**
	 * 注册各种缓存与数据库的对应
	 */
	@PostConstruct
	public void initialize() {
		log.warn(this.getClass().getName() + " 初始化中...");
		_instance = this;

		ServerInfoBean sb = serverInfoBeanRepository.findByKey(1);
		if (sb == null) {
			// 找到当前服务器在中心服务器上注册的结构
			ServerBean s = serverBeanRepository.findById(GameServer.getInstance().getZone());
			sb = new ServerInfoBean();
			sb.key = 1;
			if (s == null) {
				sb.open = new Date();
			} else {
				sb.open = s.start;
			}
			serverInfoBeanRepository.save(sb);
		}
		this.serverInfoBean = sb;
		scheduleThings();
	}

	/**
	 * 系统退出或者关闭的时候需要调用，确保更新到数据库
	 */
	@PreDestroy
	public void dispose() {
		log.warn("--- dispose: " + this.getClass().getName());
	}

	@Scheduled(cron = "1 0 0 * * *")
	void scheduleThings() {
		Date now = new Date();
		if (now.before(this.serverInfoBean.open)) {
			// 当前时间还没有到开服时间，不要进行定时运算
			return;
		}

		updateSingle();
		updateCum();
		updateDc();
	}

	/**
	 * 12点服务器进程刷新当天的单充活动 区间
	 */
	public synchronized void updateSingle() {
		log.info("----------- 更新单冲活动() -------");
		int today = TimeUtil.getToday();
		ServerInfoBean sb = this.serverInfoBean;

		/** 插入夺宝的日期更新 **/
		Date open = LocalService.getInstance().getOpenDate();
		int days = TimeUtil.pastDays(open);
		if ((days - sb.dbRound) >= gameConfig.getDbPeriod()) {
			sb.dbRound = days;
			this.serverInfoBeanRepository.save(sb);
		}
		/** End 插入夺宝的日期更新 **/

		if (today == sb.singleChargeUpdateDay) {
			return;
		}
		try {
			sb.singleChargeUpdateDay = today;

			if (sb.singleEnd != -1 && (++sb.singleCur % gameConfig.getSinglePeriod()) != 0) {
				return;
			}

			List<SinglerechargeConf> scs = readonly.getSingleConfs();

			sb.singleBegin = sb.singleEnd + 1;

			// 最后一个就循环
			if (sb.singleBegin >= scs.size()) {
				sb.singleBegin = 0;
			}

			// 找到结束索引
			int i;
			int type = -1;
			for (i = sb.singleBegin; i < scs.size(); ++i) {
				SinglerechargeConf sc = scs.get(i);
				if (type == -1) {
					type = sc.type;
				}
				else if (type != sc.type) {
					break;
				}
			}
			sb.singleEnd = i - 1;
		} finally {
			this.serverInfoBeanRepository.save(sb);
		}
	}

	public synchronized void updateCum() {
		log.info("----------- 更新累冲活动 -------");
		int today = TimeUtil.getToday();
		ServerInfoBean sb = this.serverInfoBean;
		if (today == sb.cumChargeUpdateDay) {
			return;
		}
		try {
			sb.cumChargeUpdateDay = today;
			if (sb.cumEnd != -1 && ((++sb.cumCur % gameConfig.getCumPeriod()) != 0)) {
				return;
			}

			sb.cumBegin = sb.cumEnd + 1;

			List<CumrechargeConf> scs = readonly.getCumConfs();
			// 最后一个就循环
			if (sb.cumBegin >= scs.size()) {
				sb.cumBegin = 0;
			}

			// 找到结束索引
			int i;
			int type = -1;
			for (i = sb.cumBegin; i < scs.size(); ++i) {
				CumrechargeConf sc = scs.get(i);
				if (type == -1) {
					type = sc.type;
				}
				else if (type != sc.type) {
					break;
				}
			}
			sb.cumEnd = i - 1;
		} finally {
			this.serverInfoBeanRepository.save(sb);
		}
	}

	public synchronized void updateDc() {
		log.info("----------- 更新折扣活动 -------");
		int today = TimeUtil.getToday();
		ServerInfoBean sb = this.serverInfoBean;
		if (today == sb.dcUpdateDay) {
			return;
		}
		try {
			sb.dcUpdateDay = today;
			if (sb.dcEnd != -1 && ((++sb.dcCur % gameConfig.getSalePeriod()) != 0)) {
				return;
			}

			sb.dcBegin = sb.dcEnd + 1;
			sb.ecBegin = sb.ecEnd + 1;

			List<SaleConf> scs = readonly.getSaleConfs();
			List<ExchangeConf> ecs = readonly.getExchangeConfs();

			// 最后一个就循环
			if (sb.dcBegin >= scs.size()) {
				sb.dcBegin = 0;
			}
			if (sb.ecBegin >= ecs.size()) {
				sb.ecBegin = 0;
			}

			// 找到结束索引
			int i;
			int type = -1;
			for (i = sb.dcBegin; i < scs.size(); ++i) {
				SaleConf sc = scs.get(i);
				if (type == -1) {
					type = sc.type;
				}
				else if (type != sc.type) {
					break;
				}
			}
			sb.dcEnd = i - 1;

			type = -1;
			for (i = sb.ecBegin; i < ecs.size(); ++i) {
				ExchangeConf ec = ecs.get(i);
				if (type == -1) {
					type = ec.type;
				}
				else if (type != ec.type) {
					break;
				}
			}
			sb.ecEnd = i - 1;
		} finally {
			this.serverInfoBeanRepository.save(sb);
		}
	}

	public synchronized int getSingleCur() {
		return this.serverInfoBean.singleCur;
	}

	public synchronized int getSingleBeginIdx() {
		return this.serverInfoBean.singleBegin;
	}

	public int getSingleEndIdx() {
		return this.serverInfoBean.singleEnd;
	}

	public synchronized int getCumCur() {
		return this.serverInfoBean.cumCur;
	}

	public synchronized int getCumBeginIdx() {
		return this.serverInfoBean.cumBegin;
	}

	public synchronized int getCumEndIdx() {
		return this.serverInfoBean.cumEnd;
	}

	public synchronized int getDcCur() {
		return this.serverInfoBean.dcCur;
	}

	public synchronized int getDcBeginIdx() {
		return this.serverInfoBean.dcBegin;
	}

	public synchronized int getDcEndIdx() {
		return this.serverInfoBean.dcEnd;
	}

	public synchronized int getEcBeginIdx() {
		return this.serverInfoBean.ecBegin;
	}

	public synchronized int getEcEndIdx() {
		return this.serverInfoBean.ecEnd;
	}

	/**
	 * 保存服务器信息
	 */
	public synchronized void incFundCount() {
		++this.serverInfoBean.fund;
		serverInfoBeanRepository.save(this.serverInfoBean);
	}

	public synchronized int getFundCount() {
		return this.serverInfoBean.fund;
	}

	public synchronized Date getOpenDate() {
		return this.serverInfoBean.open;
	}

	public synchronized int getDbRound() {
		return this.serverInfoBean.dbRound;
	}

	public synchronized boolean getDay3() {
		return this.serverInfoBean.day3;
	}
	public synchronized boolean getDay5() {
		return this.serverInfoBean.day5;
	}
	public synchronized boolean getDay7() {
		return this.serverInfoBean.day7;
	}

	public synchronized void setDay3() {
		this.serverInfoBean.day3 = true;
		serverInfoBeanRepository.save(this.serverInfoBean);
	}
	public synchronized void setDay5() {
		this.serverInfoBean.day5 = true;
		serverInfoBeanRepository.save(this.serverInfoBean);
	}
	public synchronized void setDay7() {
		this.serverInfoBean.day7 = true;
		serverInfoBeanRepository.save(this.serverInfoBean);
	}

	public synchronized int getLimitday() {
		return this.serverInfoBean.limitday;
	}

	public synchronized void setLimitday(int taskid) {
		this.serverInfoBean.limitday = taskid;
		serverInfoBeanRepository.save(this.serverInfoBean);
	}
}
