package com.tumei.game.services;

import com.tumei.GameConfig;
import com.tumei.centermodel.ServerBean;
import com.tumei.centermodel.ServerBeanRepository;
import com.tumei.common.Readonly;
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
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/12/23 0023.
 * <p>
 * 本地服务器数据库工具
 */
@Service
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

	@Autowired
	private ServerBeanRepository serverBeanRepository;


	private ServerInfoBean serverBean;

	/**
	 * 注册各种缓存与数据库的对应
	 */
	@PostConstruct
	public void initialize() {
		log.warn(this.getClass().getName() + " 初始化中...");
		_instance = this;

		ServerBean server = serverBeanRepository.findById(GameServer.getInstance().getZone());

		ServerInfoBean sb = serverInfoBeanRepository.findByKey(1);
		if (sb == null) {
			sb = new ServerInfoBean();
			sb.key = 1;
			if (server != null) {
				sb.open = server.start;
			} else {
				sb.open = new Date();
			}
			serverInfoBeanRepository.save(sb);
		}
		this.serverBean = sb;
		scheduleThings();

		updateSingle();
		updateCum();
		updateDc();
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
		if (now.before(this.serverBean.open)) {
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
		ServerInfoBean sb = this.serverBean;

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
			// 记录结束的日期
//			sb.singleCur = 0;
		} finally {
			this.serverInfoBeanRepository.save(sb);
		}
	}

	public synchronized void updateCum() {
		log.info("----------- 更新累冲活动 -------");
		int today = TimeUtil.getToday();
		ServerInfoBean sb = this.serverBean;
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
		ServerInfoBean sb = this.serverBean;
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
		return this.serverBean.singleCur;
	}

	public synchronized int getSingleBeginIdx() {
		return this.serverBean.singleBegin;
	}

	public int getSingleEndIdx() {
		return this.serverBean.singleEnd;
	}

	public synchronized int getCumCur() {
		return this.serverBean.cumCur;
	}

	public synchronized int getCumBeginIdx() {
		return this.serverBean.cumBegin;
	}

	public synchronized int getCumEndIdx() {
		return this.serverBean.cumEnd;
	}

	public synchronized int getDcCur() {
		return this.serverBean.dcCur;
	}

	public synchronized int getDcBeginIdx() {
		return this.serverBean.dcBegin;
	}

	public synchronized int getDcEndIdx() {
		return this.serverBean.dcEnd;
	}

	public synchronized int getEcBeginIdx() {
		return this.serverBean.ecBegin;
	}

	public synchronized int getEcEndIdx() {
		return this.serverBean.ecEnd;
	}

	/**
	 * 保存服务器信息
	 */
	public synchronized void incFundCount() {
		++this.serverBean.fund;
		serverInfoBeanRepository.save(this.serverBean);
	}

	public synchronized int getFundCount() {
		return this.serverBean.fund;
	}

	public synchronized Date getOpenDate() {
		return this.serverBean.open;
	}

	public synchronized boolean getDay3() {
		return this.serverBean.day3;
	}

	public synchronized boolean getDay5() {
		return this.serverBean.day5;
	}

	public synchronized boolean getDay7() {
		return this.serverBean.day7;
	}

	public synchronized int getLimitday() {
		return this.serverBean.limitday;
	}

	public synchronized void setDay3() {
		this.serverBean.day3 = true;
		serverInfoBeanRepository.save(this.serverBean);
	}

	public synchronized void setDay5() {
		this.serverBean.day5 = true;
		serverInfoBeanRepository.save(this.serverBean);
	}

	public synchronized void setDay7() {
		this.serverBean.day7 = true;
		serverInfoBeanRepository.save(this.serverBean);
	}

	public synchronized void setLimitday(int taskid) {
		this.serverBean.limitday = taskid;
		serverInfoBeanRepository.save(this.serverBean);
	}
}
