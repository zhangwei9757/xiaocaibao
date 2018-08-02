package com.tumei.common;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.tumei.GameConfig;
import com.tumei.centermodel.*;
import com.tumei.common.service.BaseDaoService;
import com.tumei.model.*;
import com.tumei.model.festival.FestivalBean;
import com.tumei.model.festival.FestivalBeanRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DaoGame extends BaseDaoService {
    final static Log log = LogFactory.getLog(DaoGame.class);
    private static DaoGame _instance = null;

    public static DaoGame getInstance() {
        return _instance;
    }

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
    private AccountBeanRepository accountBeanRepository;
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
    @Autowired
    private WarBeanRepository warBeanRepository;
    @Autowired
    private LimitReceiveRespository limitReceiveRespository;
    @Autowired
    private RdshopRepository rdshopRepository;
    @Autowired
    private GuildbagBeanRespository guildbagBeanRespository;

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


    private LoadingCache<Long, WarBean> warBeanLoadingCache;

    private LoadingCache<Long, LimitReceiveBean> limitReceiveBeanLoadingCache;

    private LoadingCache<Long, RdshopBean> rdshopBeanLoadingCache;

    private LoadingCache<Long, GuildbagBean> guildbagBeanLoadingCache;

    /********** END 各种缓存接口 **********/

    /**
     * 注册各种缓存与数据库的对应
     */
    @PostConstruct
    public void initialize() {
        log.warn(this.getClass().getName() + " 初始化中...");

        nameBeanRepository.findAll().stream().forEach((nb) -> {
            names.put(nb.name, nb.id);
            ids.put(nb.id, nb.name);
            log.info("id:" + nb.id + " name:" + nb.name);
        });

        roleLoadingCache = gameCache.cached(new CacheLoader<Long, RoleBean>() {
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
        });

        roleMapBeanLoadingCache = gameCache.cached(new CacheLoader<Long, RoleMineBean>() {
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
        });

        herosBeanLoadingCache = gameCache.cached(new CacheLoader<Long, HerosBean>() {
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
        });

        packLoadingCache = gameCache.cached(new CacheLoader<Long, PackBean>() {
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
        });


        sceneBeanLoadingCache = gameCache.cached(new CacheLoader<Long, SceneBean>() {
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
        });

        mailsBeanLoadingCache = gameCache.cached(new CacheLoader<Long, MailsBean>() {
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
        });

        chargeBeanLoadingCache = gameCache.cached(new CacheLoader<Long, ChargeBean>() {
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
        });

        summonBeanLoadingCache = gameCache.cached(new CacheLoader<Long, SummonBean>() {
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
        });


        starBeanLoadingCache = gameCache.cached(new CacheLoader<Long, StarBean>() {
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
        });

        dailyTaskBeanLoadingCache = gameCache.cached(new CacheLoader<Long, DailyTaskBean>() {
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
        });

        storeBeanLoadingCache = gameCache.cached(new CacheLoader<Long, StoreBean>() {
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
        });

        // 燃烧远征
        fireRaidBeanLoadingCache = gameCache.cached(new CacheLoader<Long, FireRaidBean>() {
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
        });

        // 日常副本
        dailySceneBeanLoadingCache = gameCache.cached(new CacheLoader<Long, DailySceneBean>() {
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
        });

        // 公会
        groupBeanLoadingCache = gameCache.cached(new CacheLoader<Long, GroupBean>() {
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
        });

        // 符文副本
        runeBeanLoadingCache = gameCache.cached(new CacheLoader<Long, RuneBean>() {
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
        });

        // 活动，各种活动
        activityBeanLoadingCache = gameCache.cached(new CacheLoader<Long, ActivityBean>() {
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
        });

        // 个人统计
        staBeanLoadingCache = gameCache.cached(new CacheLoader<Long, StaBean>() {
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
        });

        // 好友
        friendsBeanLoadingCache = gameCache.cached(new CacheLoader<Long, FriendsBean>() {
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
        });

        // 神秘宝藏
        treasureBeanLoadingCache = gameCache.cached(new CacheLoader<Long, TreasureBean>() {
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
        });

        // 全局统计
        dataStaBeanLoadingCache = gameCache.cached(new CacheLoader<Integer, DataStaBean>() {
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
        });

        // 节日活动
        festivalBeanLoadingCache = gameCache.cached(new CacheLoader<Long, FestivalBean>() {
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
        });

        // boss战斗
        bossBeanLoadingCache = gameCache.cached(new CacheLoader<Long, BossBean>() {
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
        });

        warBeanLoadingCache = gameCache.cached(new CacheLoader<Long, WarBean>() {
            @Override
            public WarBean load(Long id) {
                // 从指定数据库中读取数据
                WarBean role;
                try {
                    role = warBeanRepository.findById(id);
                    if (role != null) {
                        return role;
                    }
                } catch (Exception e) {
                    log.error("cache find:", e);
                    return null;
                }

                role = new WarBean(id);
                return role;
            }
        }, (RemovalNotification<Long, WarBean> removalNotification) -> {
            if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
                warBeanRepository.save(removalNotification.getValue());
            }
        });

        limitReceiveBeanLoadingCache = gameCache.cached(new CacheLoader<Long, LimitReceiveBean>() {
            @Override
            public LimitReceiveBean load(Long id) {
                // 从指定数据库中读取数据
                LimitReceiveBean role;
                try {
                    role = limitReceiveRespository.findById(id);
                    if (role != null) {
                        return role;
                    }
                } catch (Exception e) {
                    log.error("cache find:", e);
                    return null;
                }

                role = new LimitReceiveBean(id);
                return role;
            }
        }, (RemovalNotification<Long, LimitReceiveBean> removalNotification) -> {
            if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
                limitReceiveRespository.save(removalNotification.getValue());
            }
        });

        rdshopBeanLoadingCache = gameCache.cached(new CacheLoader<Long, RdshopBean>() {
            @Override
            public RdshopBean load(Long id) {
                // 从指定数据库中读取数据
                RdshopBean role;
                try {
                    role = rdshopRepository.findById(id);
                    if (role != null) {
                        return role;
                    }
                } catch (Exception e) {
                    log.error("cache find:", e);
                    return null;
                }

                role = new RdshopBean(id);
                return role;
            }
        }, (RemovalNotification<Long, RdshopBean> removalNotification) -> {
            if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
                rdshopRepository.save(removalNotification.getValue());
            }
        });

        guildbagBeanLoadingCache = gameCache.cached(new CacheLoader<Long, GuildbagBean>() {
            @Override
            public GuildbagBean load(Long id) {
                // 从指定数据库中读取数据
                GuildbagBean role;
                try {
                    role = guildbagBeanRespository.findById(id);
                    if (role != null) {
                        return role;
                    }
                } catch (Exception e) {
                    log.error("cache find:", e);
                    return null;
                }

                role = new GuildbagBean(id);
                return role;
            }
        }, (RemovalNotification<Long, GuildbagBean> removalNotification) -> {
            if (removalNotification.getCause() == RemovalCause.EXPIRED || removalNotification.getCause() == RemovalCause.EXPLICIT) {
                guildbagBeanRespository.save(removalNotification.getValue());
            }
        });

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
     *
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


    /**
     * 根据昵称返回对应的玩家id;
     *
     * @param name
     * @return
     */
    public synchronized long findByName(String name) {
        return names.getOrDefault(name, 0L);
    }

    /**
     * id -> name 搜索
     *
     * @param id
     * @return
     */
    public synchronized String findById(long id) {
        return ids.getOrDefault(id, null);
    }

    /**
     * 获取服务器所有玩家的id信息
     *
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
     *
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

    public WarBean findWar(long id) {
        try {
            return warBeanLoadingCache.get(id);
        } catch (ExecutionException e) {
            log.error("获取战争学院信息错误:", e);
        }
        return null;
    }


    public LimitReceiveBean findLimitReceive(long id) {
        try {
            return limitReceiveBeanLoadingCache.get(id);
        } catch (ExecutionException e) {
            log.error("获取每日限时领取奖励信息错误:", e);
        }
        return null;
    }

    public RdshopBean findRdshopBean(long id) {
        try {
            return rdshopBeanLoadingCache.get(id);
        } catch (ExecutionException e) {
            log.error("获取神秘商店信息错误:", e);
        }
        return null;
    }

    public GuildbagBean findGuildbagBean(long id) {
        try {
            return guildbagBeanLoadingCache.get(id);
        } catch (ExecutionException e) {
            log.error("获取公会红包信息错误:", e);
        }
        return null;
    }


    /**
     * 对于自动注册的账号，绑定新的账号名和密码。
     *
     * @param uid
     * @param account
     * @param password
     * @return
     */
    public String bindAccount(long uid, String account, String password) {
        try {
            AccountBean accountBean = accountBeanRepository.findById(uid / 1000);
            if (accountBean != null) {
                if (!accountBean.getAccount().startsWith("__xcb_")) {
                    return "非自动生成的帐号不能再次绑定";
                }

                if (account.length() < 4) {
                    return "账号名必须不少于四个字符";
                }
                if (password.length() < 6) {
                    return "密码必须不少于六个字符";
                }
                if (account.startsWith("__")) {
                    return "账号名不能使用非法字符";
                }

                accountBean.setAccount(account);
                accountBean.setPasswd(password);
                accountBeanRepository.save(accountBean);

                return null;
            }
        } catch (Exception e) {
            return "重复的账号";
        }

        return "错误的绑定id";
    }

    /**
     * 检测开服之前，测试的时候充值的人民币数量，返回应该奖励的钻石，该函数在免费改名的时候调用
     *
     * @param uid
     * @return
     */
    public int checkOpenRmb(long uid) {
        AccountBean accountBean = accountBeanRepository.findById(1053L);
        if (accountBean != null) {
            int rmb = accountBean.getOpenrmb();

            if (rmb > 0) {
                accountBean.setOpenrmb(0);
                accountBeanRepository.save(accountBean);
            }

            return rmb;
        }

        return 0;
    }
}
