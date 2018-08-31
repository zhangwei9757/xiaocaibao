package com.tumei.common;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.tumei.centermodel.*;
import com.tumei.common.service.CacheIt;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by Administrator on 2016/12/23 0023.
 */
@Component
public class DaoUtils {
	final static Log log = LogFactory.getLog(DaoUtils.class);

	@Autowired
	private CacheIt cacheIt;

	/**
	 * 账户数据库的Template操作句柄
	 */
	@Autowired
	private MongoTemplate mongoTemplate;

	/***
	 * 账户数据库
	 */
	@Autowired
	private AccountBeanRepository accountBeanRepository;

	@Autowired
	private UserBeanRepository userBeanRepository;

	@Autowired
	private DailyStaBeanRepository dailyStaBeanRepository;

	/**
	 * 路由数据库
	 */
	@Autowired
	private RouterBeanRepository routerBeanRepository;

	/********** 各种缓存接口 **********/
	private LoadingCache<Long, UserBean> userBeanLoadingCache;
	private LoadingCache<Integer, DailyStaBean> dailyStaBeanLoadingCache;

	/********** END 各种缓存接口 **********/

	/***
	 * 根据玩家id创建一个属于玩家的随机串
	 * @param id
	 * @return
	 */
	private String createRandomChars(Long id) {
		StringBuilder sb = new StringBuilder();
		String letters = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < 2; ++i) {
			char c = letters.charAt(RandomUtil.getRandom() % letters.length());
			sb.append(c);
		}
		sb.append((id));
		return sb.toString();
	}

	/**
	 * 注册各种缓存与数据库的对应
	 */
	@PostConstruct
	public void initialize() {
		log.debug(this.getClass().getName() + " init.");

		userBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, UserBean>() {
			@Override
			public UserBean load(Long s) {
				// 从指定数据库中读取数据
				UserBean ub;
				try {
					ub = userBeanRepository.findById(s);
					if (ub != null) {
						return ub;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				// 直接创建一个SocietyBean
				ub = new UserBean();
				ub.setId(s);
				return ub;
			}
		}, (RemovalNotification<Long, UserBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				UserBean sb = removalNotification.getValue();
				userBeanRepository.save(sb);
			}
		}, 30, 30);


		dailyStaBeanLoadingCache = cacheIt.cached(new CacheLoader<Integer, DailyStaBean>() {
			@Override
			public DailyStaBean load(Integer s) {
				// 从指定数据库中读取数据
				DailyStaBean ub;
				try {
					ub = dailyStaBeanRepository.findByDay(s);
					if (ub != null) {
						return ub;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				// 直接创建一个SocietyBean
				ub = new DailyStaBean();
				ub.setDay(s);
				return ub;
			}
		}, (RemovalNotification<Integer, DailyStaBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				DailyStaBean sb = removalNotification.getValue();
				dailyStaBeanRepository.save(sb);
			}
		}, 1000, 1000);

	}

	/**
	 * 系统退出或者关闭的时候需要调用，确保更新到数据库
	 */
	@PreDestroy
	public void dispose() {
		log.debug("--- dispose: " + this.getClass().getName());
	}


	/**
	 * 根据名字获取对应的下一个数值,用于获取新用户可用的ID
	 *
	 * @return
	 */
	public Long nextVal() {
		Query query = new Query(where("name").is("userid"));
		Update update = new Update().inc("nextval", 1);
		IDBean idBean = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), IDBean.class);
		return idBean.nextval;
	}

	/**
	 * 查询帐号
	 *
	 * @return
	 */
	public AccountBean findAccount(long _id) {
		return accountBeanRepository.findById(_id);
	}

	/**
	 * 新增帐号
	 * 数据库设定会导出插入错误；
	 *
	 * @param _bean
	 * @throws Exception
	 */
	public boolean addAccount(AccountBean _bean) {
		accountBeanRepository.insert(_bean);
		return true;
	}

	@Autowired
	private ReceiptBeanRepository receiptBeanRepository;

	/***
	 * 保存充值订单，充值订单，需要订单唯一
	 * @param receiptBean
	 */
	public int saveReceipt(ReceiptBean receiptBean) {
		try {
			receiptBeanRepository.save(receiptBean);
		} catch (DuplicateKeyException dup) {
			log.error("重复订单数据:" + receiptBean.toString());
			return -1;
		} catch (Exception ex) {
			log.error("保存订单错误:" + ex.getMessage() + " class:" + ex.getClass().getName());
			return 1;
		}
		return 0;
	}

	/**
	 * 查找帐号相关的数据
	 *
	 * @param id
	 * @return
	 */
	public UserBean findUser(long id) {
		try {
			return userBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("查找UserBean：" + id + " 失败.");
		}
		return null;
	}

	public DailyStaBean findDs(int today) {
		if (today <= 0) {
			today = TimeUtil.getToday();
		}

		try {
			return dailyStaBeanLoadingCache.get(today);
		} catch (ExecutionException e) {
			log.error("查找DaiyStaBean：" + today + " 失败.");
		}
		return null;
	}

	public synchronized void addCharge(int rmb) {
		DailyStaBean dsb = findDs(0);
		if (dsb != null) {
			dsb.addCharge(rmb);
		}
	}

	public synchronized void addUser(long uid) {
		DailyStaBean dsb = findDs(0);
		if (dsb != null) {
			dsb.addUser(uid);
		}
	}

}
