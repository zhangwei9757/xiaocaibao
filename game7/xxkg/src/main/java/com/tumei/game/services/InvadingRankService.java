package com.tumei.game.services;

import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.common.algo.zset.SkipList;
import com.tumei.common.utils.TimeUtil;
import com.tumei.dto.db2proto.NameValue;
import com.tumei.model.limit.*;
import com.tumei.modelconf.limit.InvadingConf;
import com.tumei.modelconf.limit.InvrankConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zw on 2018/09/28
 * <p>
 * 限时排行服务
 * <p>
 * 在指定的时间段，进行排行统计排行
 */
@Service
public class InvadingRankService {
    private final static Log log = LogFactory.getLog(InvadingRankService.class);

    private static InvadingRankService _instance;

    public static InvadingRankService getInstance() {
        return _instance;
    }

    @Autowired
    private Readonly readonly;

    @Autowired
    private LocalService localService;

    @Autowired
    private InvadingRankBeanRepository invadingRankBeanRepository;

    // 时间段排行统计开始
    public long begin;
    // 时间段排行统计结束
    public long end;

    /**
     * 所有玩家的信息， 上榜的玩家
     */
    private HashMap<Long, InvadingRankBean> users = new HashMap<>();

    // 累计一定的次数进行数据库写入
    private int cum;

    // 发生改变的内容
    private HashSet<Long> changes = new HashSet<>();
    /**
     * 排行信息, key是id, value是得分
     */
    private SkipList<Long, Long> skipList = new SkipList<>(0L);

    // 初始化完成标识
    private boolean inited = false;

    // 活动标识序号，对应节日列表中的flag
    public int key = -1;

    /**
     * 初始化
     */
    @PostConstruct
    synchronized void init() {
        _instance = this;

        List<InvadingRankBean> lrbs = invadingRankBeanRepository.findAll();
        // 先根据排名进行一次排序，然后再插入，否则相同分数的先后顺序可能会被毁掉
        lrbs.sort((a, b) -> {
            if (a.getTs() < b.getTs()) {
                return -1;
            }
            return 1;
        });

        lrbs.forEach((orb) ->
        {
            users.put(orb.getId(), orb);
            addScore(orb.getId(), orb.getCount());
        });
        /**
         * 必须先排序且初始化users，再进行刷新活动识别，否则users集合无数据，
         * 无法对【当前无活动】且【有过期活动奖励未发送】，进行发送奖励
         */
        flushLimitTask(false);

        inited = true;
    }

    /**
     * 对跳表的操作，增加uid与分数的对应记录
     *
     * @param uid
     * @param score
     */
    private void addScore(long uid, long score) {
        InvadingRankBean lrb = users.getOrDefault(uid, null);
        if (lrb != null) {
            long old = lrb.getCount();
            // 初始化情况下分数未有变化，要直接插入
            if (old == score && inited) {
                return;
            }

            skipList.delete(old, uid);
            lrb.setCount(score);
        }

        skipList.insert(score, uid);
    }

    /**
     * 对于指定的玩家id，查找他对应的排名
     *
     * @param uid
     * @return -1 标识玩家不存在
     * 0 标识跳表中没有这个玩家
     * <p>
     * 1-无穷大 标识真实的排名
     */
    private int rank(long uid) {
        InvadingRankBean lrb = users.getOrDefault(uid, null);
        if (lrb == null) {
            return -1;
        }
        long score = lrb.getCount();
        return skipList.getRank(score, uid);
    }

    /**
     * 返回指定排名区间中的所有玩家id
     *
     * @param r1 最低排名，包括
     * @param r2 最高排名，包括
     * @return
     */
    private List<Long> range(int r1, int r2) {
        r1 = Math.max(r1, 1);
        r2 = Math.max(r2, 1);
        return skipList.getRankRange(r1, r2);
    }

    /**
     * 刷新，查看是否进入了一个新的限时任务区间,
     * <p>
     * 如果之前的限时任务标记没有清理，现在进入的新任务，或者进入无任务时间，需要将奖励发放到位。
     */
    public synchronized void flushLimitTask(boolean force) {
        int today = TimeUtil.getToday();
        InvadingConf thisFc = null;

        List<InvadingConf> vcs = readonly.getInvadingConfs();
        int lastKey = localService.getInvadingday();

        for (InvadingConf fest : vcs) {
            if (today >= fest.start && today <= fest.end) {
                thisFc = fest;
                key = fest.key;
                break;
            }
        }

        // 有任务，要检查任务是否和以前的任务标识相同
        if (thisFc != null) {
            this.begin = TimeUtil.fromDay(thisFc.start).atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8));
            this.end = TimeUtil.fromDay(thisFc.end + 1).atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8));
            // 是一个新的任务，需要结算之前任务的排行奖励
            if (lastKey != thisFc.key || force) {
                if (lastKey != 0 || force) {
                    // 发送奖励
                    sendTaskAwards(lastKey);
                }

                // 将所有排行数据都重置
                users.clear();
                skipList = new SkipList<>(0L);
                localService.setInvadingday(thisFc.key);
                this.invadingRankBeanRepository.deleteAll();
                // 如果不清掉，动态修改配置时，这个值判断的是错误的上次区间
                begin = 0;
                end = 0;
            }
        } else {
            if (lastKey != 0 || force) {
                // 发送奖励
                sendTaskAwards(lastKey);
                // 将所有排行数据都重置
                users.clear();
                skipList = new SkipList<>(0L);
                localService.setInvadingday(0);
                this.invadingRankBeanRepository.deleteAll();
                begin = 0;
                end = 0;
            }
        }
    }

    /**
     * 发送任务奖励
     *
     * @param lastKey 对应的任务配置表中的key
     */
    public synchronized void sendTaskAwards(int lastKey) {
        // 发送玩家累计充值，未手动领取的奖励
        DaoGame instance = DaoGame.getInstance();
        for (long uid : users.keySet()) {
            InvadingBean ib = instance.findInvading(uid);
            ib.sendMailAward();
        }
        // 发送排行榜奖励
        List<NameValue> ranks = getRanks(-1);
        for (NameValue rank : ranks) {
            if (rank.getUid() > 0) {
                instance.findInvading(rank.getUid()).sendMailAwardRank(rank.getRank());
            }
        }
        // 清掉活动标识key,防止使用下标0找到错误信息，还原成 -1
        key = -1;
    }

    /**
     * 1. 每秒钟检查 三日，五日，七日奖励是否发送，如果没有，是否已经到时，并进行发送任务
     * <p>
     * 2. 每隔一定的时间讲变动的数据进行保存
     */
    @Scheduled(fixedDelay = 1000)
    void schedule() {
        if (!inited) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now >= end) {
            flushLimitTask(false);
        }

        // 20多分钟进行一次保存 1000
        if (++cum >= 10) {
            save();
            cum = 0;
        }
    }

    /**
     * 保存: 将变动的数据刷新到数据库中
     */
    synchronized void save() {
        changes.forEach((uid) -> {
            InvadingRankBean orb = users.getOrDefault(uid, null);
            if (orb != null) {
                invadingRankBeanRepository.save(orb);
            }
        });
        changes.clear();
    }


    /**
     * 活动中对应的任务完成后，增加分数
     *
     *
     * @param uid
     * @param name
     * @param val
     */
    public synchronized void put(long uid, String name, long val, int activity) {
        long now = System.currentTimeMillis() / 1000;
        if (now < begin || now > end) {
            return;
        }
        // 活动标识与activity一致，参加对应活动
        if (key != activity) {
            return;
        }
        InvadingRankBean orb = users.getOrDefault(uid, null);
        if (orb == null) {
            orb = new InvadingRankBean(uid);
            orb.setName(name);
            users.put(uid, orb);
        } else {
            if (!orb.getName().equals(name)) {
                orb.setName(name);
            }
        }

        addScore(uid, orb.getCount() + val);
        // 为了确保排位唯一性，使用时间戳
        orb.setTs(System.currentTimeMillis());
        changes.add(uid);
    }

    /**
     * 获取排行榜前十个
     *
     * @return 返回一个列表即为排行榜
     */
    public synchronized List<NameValue> getRanks(long self) {
        List<NameValue> rtn = new ArrayList<>();
        // 键：排名----值：详细信息
        HashMap<Integer, NameValue> temp = new HashMap<>();

        List<InvrankConf> invrankConfs = readonly.getInvrankConfs();

        List<Long> uids = range(1, 10);
        int i = 1;
        for (long uid : uids) {
            InvadingRankBean orb = users.getOrDefault(uid, null);
            if (orb != null) {
                for (InvrankConf ic : invrankConfs) {
                    // 此处判断，排行榜对应位置排名是否符合，配置表对应位置最低要求
                    if (orb.getCount() >= ic.limit) {
                        // 如果自己可以得此当前排名但是，已有人占了，只能往下排名，一直排到第十名还有人，自己没机会上榜了
                        int realRank = -1;
                        for (int j = 1; j <= 10; ++j) {
                            NameValue nv = temp.getOrDefault(j, null);
                            // 自己上榜要求符合且对应排名位置无人，否则往下排
                            if (nv == null && j >= ic.key) {
                                realRank = j;
                                break;
                            }
                        }
                        // 筛选后还可以上榜
                        if (realRank >= 1) {
                            NameValue nv = new NameValue(orb.getId(), orb.getName(), orb.getCount(), realRank);
                            temp.put(realRank, nv);
                        }

                        break;
                    }
                }
            }
            ++i;
        }
        // 有资格入榜的所有排名重新排序
        for (int j = 1; j <= 10; ++j) {
            // 自己的排行rank位置，与当当前对应下标j位置一样,就排序在此下标
            if (temp.getOrDefault(j, null) != null && temp.get(j).getRank() == j) {
                rtn.add(temp.get(j));
            } else {
                InvrankConf ic = readonly.findInvrankConf(j);
                rtn.add(new NameValue(-1,"", ic.limit, j));
            }
        }

        return rtn;
    }

    @PreDestroy
    void dispose() {
        log.warn("--- 系统退出时保存综合榜单..");
        save();
    }

    /**
     * 是否有有限时活动正在开启中
     *
     * @return
     */
    public boolean isActive() {
        long now = System.currentTimeMillis() / 1000;
        if (now < begin || now > end) {
            return false;
        }
        return true;
    }

}
