package com.tumei.common;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.tumei.GameConfig;
import com.tumei.centermodel.ProductBean;
import com.tumei.centermodel.ProductBeanRepository;
import com.tumei.centermodel.ReceiptBean;
import com.tumei.centermodel.ReceiptBeanRepository;
import com.tumei.common.service.BaseDaoService;
import com.tumei.game.GameServer;
import com.tumei.model.*;
import com.tumei.model.festival.FestivalBean;
import com.tumei.model.festival.FestivalBeanRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Administrator on 2016/12/23 0023.
 * <p>
 * 数据库工具
 */
@Component
public class DaoService extends BaseDaoService {
	private static DaoService _instance = null;

	public static DaoService getInstance() {
		return _instance;
	}

	private final static Log log = LogFactory.getLog(DaoService.class);

	/**
	 * 本服务器名字-》id
	 */
	private HashMap<String, Long> names = new HashMap<>();

	/**
	 * 本服务器id -> 名字
	 */
	private HashMap<Long, String> ids = new HashMap<>();

	@Autowired
	private GameConfig gameConfig;
	@Autowired
	private NameBeanRepository nameBeanRepository;
	@Autowired
	private ReceiptBeanRepository receiptBeanRepository;
	@Autowired
	private ProductBeanRepository productBeanRepository;
	@Autowired
	private PackBeanRepository packBeanRepository;
	@Autowired
	private RoleBeanRepository roleBeanRepository;
	@Autowired
	private HerosBeanRespository herosBeanRespository;
	@Autowired
	private SceneBeanRepository sceneBeanRepository;
	@Autowired
	private MailsBeanRepository mailsBeanRepository;
	@Autowired
	private ChargeBeanRepository chargeBeanRepository;
	@Autowired
	private SummonBeanRepository summonBeanRepository;
	@Autowired
	private StarBeanRepository starBeanRepository;
	@Autowired
	private DailyTaskBeanRepository dailyTaskBeanRepository;
	@Autowired
	private StoreBeanRepository storeBeanRepository;
	@Autowired
	private FireRaidBeanRepository fireRaidBeanRepository;
	@Autowired
	private DailySceneBeanRepository dailySceneBeanRepository;
	@Autowired
	private GroupBeanRespository groupBeanRespository;
	@Autowired
	private RuneBeanRepository runeBeanRepository;
	@Autowired
	private ActivityBeanRepository activityBeanRepository;
	@Autowired
	private StaBeanRepository staBeanRepository;
	@Autowired
	private FriendsBeanRepository friendsBeanRepository;
	@Autowired
	private TreasureBeanRepository treasureBeanRepository;
	@Autowired
	private RoleMineBeanRepository roleMineBeanRepository;
	@Autowired
	protected FatalBeanRepository fatalBeanRepository;
	@Autowired
	private DataStaBeanRepository dataStaBeanRepository;
	@Autowired
	private FestivalBeanRepository festivalBeanRepository;
	@Autowired
	private BossBeanRepository bossBeanRepository;

	/********** 各种缓存接口 **********/
	/**
	 * 角色基本信息缓存
	 */
	private LoadingCache<Long, RoleBean> roleLoadingCache;
	/**
	 * 背包信息
	 */
	private LoadingCache<Long, PackBean> packLoadingCache;
	/**
	 * 英雄信息
	 */
	private LoadingCache<Long, HerosBean> herosBeanLoadingCache;
	/**
	 * 副本信息
	 */
	private LoadingCache<Long, SceneBean> sceneBeanLoadingCache;
	/**
	 * 邮件信息
	 */
	private LoadingCache<Long, MailsBean> mailsBeanLoadingCache;
	/**
	 * 充值信息
	 */
	private LoadingCache<Long, ChargeBean> chargeBeanLoadingCache;
	/**
	 * 英雄召唤信息
	 */
	private LoadingCache<Long, SummonBean> summonBeanLoadingCache;
	/**
	 * 占星台信息
	 */
	private LoadingCache<Long, StarBean> starBeanLoadingCache;

	/**
	 * 日常任务信息
	 */
	private LoadingCache<Long, DailyTaskBean> dailyTaskBeanLoadingCache;
	/**
	 * 商店
	 */
	private LoadingCache<Long, StoreBean> storeBeanLoadingCache;
	/**
	 * 远征
	 */
	private LoadingCache<Long, FireRaidBean> fireRaidBeanLoadingCache;
	/**
	 * 日常副本
	 */
	private LoadingCache<Long, DailySceneBean> dailySceneBeanLoadingCache;
	/**
	 * 公会
	 */
	private LoadingCache<Long, GroupBean> groupBeanLoadingCache;
	/**
	 * 符文副本
	 */
	private LoadingCache<Long, RuneBean> runeBeanLoadingCache;
	/**
	 * 活动
	 */
	private LoadingCache<Long, ActivityBean> activityBeanLoadingCache;
	/**
	 * 统计
	 */
	private LoadingCache<Long, StaBean> staBeanLoadingCache;
	/**
	 * 好友
	 */
	private LoadingCache<Long, FriendsBean> friendsBeanLoadingCache;
	/**
	 * 神秘宝藏
	 */
	private LoadingCache<Long, TreasureBean> treasureBeanLoadingCache;
	/**
	 * 矿区信息
	 */
	private LoadingCache<Long, RoleMineBean> roleMapBeanLoadingCache;

	/**
	 * 全局统计信息，分日期
	 */
	private LoadingCache<Integer, DataStaBean> dataStaBeanLoadingCache;

	/**
	 * 节日活动信息
	 */
	private LoadingCache<Long, FestivalBean> festivalBeanLoadingCache;

	/**
	 * boss战信息
	 */
	private LoadingCache<Long, BossBean> bossBeanLoadingCache;
	/********** END 各种缓存接口 **********/

	/**
	 * 注册各种缓存与数据库的对应
	 */
	@PostConstruct
	public void initialize() {
		log.warn(this.getClass().getName() + " 初始化中...");

		int accDelay = gameConfig.getAccess_delay();
		int writeDelay = gameConfig.getWrite_delay();

		nameBeanRepository.findAll().stream().forEach((nb) -> {
			names.put(nb.name, nb.id);
			ids.put(nb.id, nb.name);
		});

		// 用于第一个
		roleLoadingCache = cacheIt.cached(new CacheLoader<Long, RoleBean>() {
			@Override
			public RoleBean load(Long id) {
				// 从指定数据库中读取数据
				RoleBean role;
				try {
					role = roleBeanRepository.findById(id);
					if (role != null) {
						return role;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
					return null;
				}

				role = RoleBean.createNewRole(id);
				return role;
			}
		}, (RemovalNotification<Long, RoleBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
//				log.warn("role:" + removalNotification.getKey() + " evited.");
				roleBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);


		roleMapBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, RoleMineBean>() {
			@Override
			public RoleMineBean load(Long s) {
				// 从指定数据库中读取数据
				RoleMineBean herosBean;
				try {
					herosBean = roleMineBeanRepository.findById(s);
					if (herosBean != null) {
						return herosBean;
					}
				} catch (Exception e) {
					log.fatal("查找RoleBean(" + s + ") 错误:", e);
				}

				herosBean = new RoleMineBean(s);
				return herosBean;
			}
		}, (RemovalNotification<Long, RoleMineBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				try {
					roleMineBeanRepository.save(removalNotification.getValue());
				} catch (Exception ee) {
					log.error("RoleMine Save Error:" + ee.getMessage());
				}
			}
		}, accDelay, writeDelay);

		herosBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, HerosBean>() {
			@Override
			public HerosBean load(Long s) {
				// 从指定数据库中读取数据
				HerosBean herosBean;
				try {
					herosBean = herosBeanRespository.findById(s);
					if (herosBean != null) {
						return herosBean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				herosBean = HerosBean.createNewHeros(s);
				return herosBean;
			}
		}, (RemovalNotification<Long, HerosBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				try {
					herosBeanRespository.save(removalNotification.getValue());
				} catch (Exception ex) {
				}
			}
		}, accDelay, writeDelay);

		packLoadingCache = cacheIt.cached(new CacheLoader<Long, PackBean>() {
			@Override
			public PackBean load(Long s) {
				// 从指定数据库中读取数据
				PackBean packBean;
				try {
					packBean = packBeanRepository.findById(s);
					if (packBean != null) {
						return packBean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				packBean = PackBean.createNewPack(s);
				return packBean;
			}
		}, (RemovalNotification<Long, PackBean> removalNotification) -> {
//			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				try {
					packBeanRepository.save(removalNotification.getValue());
				} catch (Exception ee) {
					log.error("PackBean Save Error:" + ee.getMessage());
				}
//			}
		}, accDelay, writeDelay);


		sceneBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, SceneBean>() {
			@Override
			public SceneBean load(Long id) {
				// 从指定数据库中读取数据
				SceneBean role;
				try {
					role = sceneBeanRepository.findById(id);
					if (role != null) {
						return role;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
					return null;
				}

				role = SceneBean.createNewScene(id);
				return role;
			}
		}, (RemovalNotification<Long, SceneBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				sceneBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		mailsBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, MailsBean>() {
			@Override
			public MailsBean load(Long id) {
				// 从指定数据库中读取数据
				MailsBean role;
				try {
					role = mailsBeanRepository.findById(id);
					if (role != null) {
						return role;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
					return null;
				}

				role = MailsBean.createNewMailsBean(id);
				return role;
			}
		}, (RemovalNotification<Long, MailsBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				mailsBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		chargeBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, ChargeBean>() {
			@Override
			public ChargeBean load(Long id) {
				// 从指定数据库中读取数据
				ChargeBean role;
				try {
					role = chargeBeanRepository.findById(id);
					if (role != null) {
						return role;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
					return null;
				}

				role = new ChargeBean();
				role.setId(id);
				return role;
			}
		}, (RemovalNotification<Long, ChargeBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				chargeBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		summonBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, SummonBean>() {
			@Override
			public SummonBean load(Long id) {
				// 从指定数据库中读取数据
				SummonBean role;
				try {
					role = summonBeanRepository.findById(id);
					if (role != null) {
						return role;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
					return null;
				}

				role = new SummonBean(id);
				return role;
			}
		}, (RemovalNotification<Long, SummonBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					summonBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);


		starBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, StarBean>() {
			@Override
			public StarBean load(Long id) {
				// 从指定数据库中读取数据
				StarBean role;
				try {
					role = starBeanRepository.findById(id);
					if (role != null) {
						return role;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
					return null;
				}

				role = new StarBean(id);
				return role;
			}
		}, (RemovalNotification<Long, StarBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					starBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		dailyTaskBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, DailyTaskBean>() {
			@Override
			public DailyTaskBean load(Long id) {
				// 从指定数据库中读取数据
				DailyTaskBean role;
				try {
					role = dailyTaskBeanRepository.findById(id);
					if (role != null) {
						return role;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
					return null;
				}

				role = new DailyTaskBean(id);
				return role;
			}
		}, (RemovalNotification<Long, DailyTaskBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					dailyTaskBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);


		storeBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, StoreBean>() {
			@Override
			public StoreBean load(Long id) {
				// 从指定数据库中读取数据
				StoreBean role;
				try {
					role = storeBeanRepository.findById(id);
					if (role != null) {
						return role;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
					return null;
				}

				role = new StoreBean(id);
				return role;
			}
		}, (RemovalNotification<Long, StoreBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					storeBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 燃烧远征
		fireRaidBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, FireRaidBean>() {
			@Override
			public FireRaidBean load(Long s) {
				// 从指定数据库中读取数据
				FireRaidBean bean;
				try {
					bean = fireRaidBeanRepository.findById(s);
					if (bean != null) {
						return bean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				bean = new FireRaidBean(s);
				return bean;
			}
		}, (RemovalNotification<Long, FireRaidBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					fireRaidBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 日常副本
		dailySceneBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, DailySceneBean>() {
			@Override
			public DailySceneBean load(Long s) {
				// 从指定数据库中读取数据
				DailySceneBean herosBean;
				try {
					herosBean = dailySceneBeanRepository.findById(s);
					if (herosBean != null) {
						return herosBean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				herosBean = new DailySceneBean(s);
				return herosBean;
			}
		}, (RemovalNotification<Long, DailySceneBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					dailySceneBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 公会
		groupBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, GroupBean>() {
			@Override
			public GroupBean load(Long s) {
				// 从指定数据库中读取数据
				GroupBean herosBean;
				try {
					herosBean = groupBeanRespository.findById(s);
					if (herosBean != null) {
						return herosBean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				herosBean = new GroupBean(s);
				return herosBean;
			}
		}, (RemovalNotification<Long, GroupBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					groupBeanRespository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 符文副本
		runeBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, RuneBean>() {
			@Override
			public RuneBean load(Long s) {
				// 从指定数据库中读取数据
				RuneBean herosBean;
				try {
					herosBean = runeBeanRepository.findById(s);
					if (herosBean != null) {
						return herosBean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				herosBean = new RuneBean(s);
				return herosBean;
			}
		}, (RemovalNotification<Long, RuneBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					runeBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 活动，各种活动
		activityBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, ActivityBean>() {
			@Override
			public ActivityBean load(Long s) {
				// 从指定数据库中读取数据
				ActivityBean herosBean;
				try {
					herosBean = activityBeanRepository.findById(s);
					if (herosBean != null) {
						return herosBean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				herosBean = new ActivityBean(s);
				return herosBean;
			}
		}, (RemovalNotification<Long, ActivityBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					activityBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 个人统计
		staBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, StaBean>() {
			@Override
			public StaBean load(Long s) {
				// 从指定数据库中读取数据
				StaBean herosBean;
				try {
					herosBean = staBeanRepository.findById(s);
					if (herosBean != null) {
						return herosBean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				herosBean = new StaBean(s);
				return herosBean;
			}
		}, (RemovalNotification<Long, StaBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					staBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 好友
		friendsBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, FriendsBean>() {
			@Override
			public FriendsBean load(Long s) {
				// 从指定数据库中读取数据
				FriendsBean herosBean;
				try {
					herosBean = friendsBeanRepository.findById(s);
					if (herosBean != null) {
						return herosBean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				herosBean = new FriendsBean(s);
				return herosBean;
			}
		}, (RemovalNotification<Long, FriendsBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					friendsBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 神秘宝藏
		treasureBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, TreasureBean>() {
			@Override
			public TreasureBean load(Long s) {
				// 从指定数据库中读取数据
				TreasureBean herosBean;
				try {
					herosBean = treasureBeanRepository.findById(s);
					if (herosBean != null) {
						return herosBean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				herosBean = new TreasureBean(s);
				return herosBean;
			}
		}, (RemovalNotification<Long, TreasureBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					treasureBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 全局统计
		dataStaBeanLoadingCache = cacheIt.cached(new CacheLoader<Integer, DataStaBean>() {
			@Override
			public DataStaBean load(Integer s) {
				// 从指定数据库中读取数据
				DataStaBean bean;
				try {
					bean = dataStaBeanRepository.findByDate(s);
					if (bean != null) {
						return bean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				bean = new DataStaBean(s);
				return bean;
			}
		}, (RemovalNotification<Integer, DataStaBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
					dataStaBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// 节日活动
		festivalBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, FestivalBean>() {
			@Override
			public FestivalBean load(Long s) {
				// 从指定数据库中读取数据
				FestivalBean bean;
				try {
					bean = festivalBeanRepository.findById(s);
					if (bean != null) {
						return bean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				bean = new FestivalBean();
				bean.setId(s);
				return bean;
			}
		}, (RemovalNotification<Long, FestivalBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				festivalBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);

		// boss战斗
		bossBeanLoadingCache = cacheIt.cached(new CacheLoader<Long, BossBean>() {
			@Override
			public BossBean load(Long s) {
				// 从指定数据库中读取数据
				BossBean bean;
				try {
					bean = bossBeanRepository.findById(s);
					if (bean != null) {
						return bean;
					}
				} catch (Exception e) {
					log.error("cache find:", e);
				}

				bean = new BossBean();
				bean.setId(s);
				return bean;
			}
		}, (RemovalNotification<Long, BossBean> removalNotification) -> {
			if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
				bossBeanRepository.save(removalNotification.getValue());
			}
		}, accDelay, writeDelay);


		_instance = this;
	}

	/**
	 * 系统退出或者关闭的时候需要调用，确保更新到数据库
	 */
	@PreDestroy
	public void dispose() {
		log.warn("--- dispose: " + this.getClass().getName());
	}


	/**
	 * 记录本服务器的一些异常信息，需要人工干预
	 * @param _uid
	 * @param _info
	 */
	public void fatal(long _uid, String _info) {
		FatalBean fb = new FatalBean(_uid, _info);
		fatalBeanRepository.save(fb);
	}

	/**
	 * 根据商品ID,返回商品对应的价格mode
	 *
	 * @param product
	 * @return
	 */
	public int findModeByProduct(String product) {
		ProductBean pb = productBeanRepository.findByProduct(product);
		if (pb == null) {
			return 0;
		}

		return pb.rmb;
	}

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
	 * 查找角色信息，如果没有就创建一个
	 *
	 * @param id
	 * @return
	 */
	public RoleBean findRole(Long id) {
		try {
			return roleLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("查询角色[" + id + "] 失败: " + e.getMessage());
		}
		return null;
	}

	/**
	 * 获取角色的背包
	 *
	 * @param id
	 * @return
	 */
	public PackBean findPack(long id) {
		try {
			return packLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色背包失败:", e);
		}
		return null;
	}

	public HerosBean findHeros(long id) {
		try {
			return herosBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色英雄信息失败:", e);
		}
		return null;
	}

	public SceneBean findScene(long id) {
		try {
			return sceneBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色副本闯关信息失败:", e);
		}
		return null;
	}

	public MailsBean findMails(long id) {
		try {
			return mailsBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色邮件信息失败:", e);
		}
		return null;
	}

	public ChargeBean findCharge(long id) {
		try {
			return chargeBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色充值信息失败:", e);
		}
		return null;
	}

	/**
	 * 英雄召唤相关数据结构缓存
	 *
	 * @param id
	 * @return
	 */
	public SummonBean findSummon(long id) {
		try {
			return summonBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色英雄召唤信息失败:", e);
		}
		return null;
	}

	/**
	 * 角色占星台数据
	 *
	 * @param id
	 * @return
	 */
	public StarBean findStar(long id) {
		try {
			return starBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色占星台信息失败:", e);
		}
		return null;
	}

	/**
	 * 获取角色日常任务
	 *
	 * @param id
	 * @return
	 */
	public DailyTaskBean findDailyTask(long id) {
		try {
			return dailyTaskBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色日常任务信息失败:", e);
		}
		return null;
	}


	public StoreBean findStore(long id) {
		try {
			return storeBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色商店信息失败:", e);
		}
		return null;
	}

	public FireRaidBean findFireRaid(long id) {
		try {
			return fireRaidBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色远征信息失败:", e);
		}
		return null;
	}

	public DailySceneBean findDailyScene(long id) {
		try {
			return dailySceneBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色日常副本信息失败:", e);
		}
		return null;
	}

	public GroupBean findGroup(long id) {
		try {
			return groupBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色公会信息失败:", e);
		}
		return null;
	}

	public RuneBean findRune(long id) {
		try {
			return runeBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色符文信息失败:", e);
		}
		return null;
	}

	public ActivityBean findActivity(long id) {
		try {
			return activityBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色活动信息失败:", e);
		}
		return null;
	}

	public StaBean findSta(long id) {
		try {
			return staBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色统计信息失败:", e);
		}
		return null;
	}

	public void pushSta(long id, String action) {
		GameServer.getInstance().info(id + "|" + action);
	}

	/**
	 * 根据昵称返回对应的玩家id;
	 * @param name
	 * @return
	 */
	public synchronized long findByName(String name) {
		return names.getOrDefault(name, 0L);
	}

	/**
	 * id -> name 搜索
	 * @param id
	 * @return
	 */
	public synchronized String findById(long id) {
		return ids.getOrDefault(id, null);
	}

	/**
	 * 获取服务器所有玩家的id信息
	 * @return
	 */
	public synchronized List<Long> getAllIds() {
		List<Long> rtn = new ArrayList<>();
		ids.forEach((k, v) -> {
			rtn.add(k);
		});
		return rtn;
	}

	/**
	 * 更换名字
	 * @param id
	 * @param name
	 * @param old
	 * @return
	 */
	public synchronized boolean changeName(long id, String name, String old) {
		if (names.containsKey(name)) {
			return false;
		}

		names.remove(old);
		names.put(name, id);
		ids.put(id, name);
		return true;
	}

	public FriendsBean findFriends(long id) {
		try {
			return friendsBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色好友信息失败:", e);
		}
		return null;
	}

	public TreasureBean findTreasure(long id) {
		try {
			return treasureBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色神秘宝藏信息失败:", e);
		}
		return null;
	}

	public RoleMineBean findRoleMap(long id) {
		try {
			return roleMapBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取角色矿区地图信息失败:", e);
		}
		return null;
	}

	public DataStaBean findDataSta(int date) {
		try {
			return dataStaBeanLoadingCache.get(date);
		} catch (ExecutionException e) {
			log.error("获取全局统计，日期(" + date + ") 失败:", e);
		}
		return null;
	}

	public FestivalBean findFestival(long id) {
		try {
			return festivalBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取节日信息错误:", e);
		}
		return null;
	}

	public BossBean findBoss(long id) {
		try {
			return bossBeanLoadingCache.get(id);
		} catch (ExecutionException e) {
			log.error("获取Boss信息错误:", e);
		}
		return null;
	}
}
