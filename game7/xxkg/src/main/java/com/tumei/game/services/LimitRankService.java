package com.tumei.game.services;

import com.tumei.common.Readonly;
import com.tumei.common.algo.zset.SkipList;
import com.tumei.common.utils.TimeUtil;
import com.tumei.dto.db2proto.NameValue;
import com.tumei.game.GameServer;
import com.tumei.model.limit.LimitRankBean;
import com.tumei.model.limit.LimitRankBeanRepository;
import com.tumei.modelconf.SummonrankConf;
import com.tumei.modelconf.VigorrankConf;
import com.tumei.modelconf.festival.CrazylistConf;
import com.tumei.modelconf.happy.SoulrankConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Leon on 2017/12/13.
 * <p>
 * 限时排行服务
 * <p>
 * 在指定的时间段，进行排行统计排行
 */
@Service
public class LimitRankService {
    private final static Log log = LogFactory.getLog(LimitRankService.class);

    private static LimitRankService _instance;

    public static LimitRankService getInstance() {
        return _instance;
    }

    @Autowired
    private Readonly readonly;

    @Autowired
    private LocalService localService;

    @Autowired
    private LimitRankBeanRepository limitRankBeanRepository;


    // 时间段排行统计开始
    public long begin;
    // 时间段排行统计结束
    public long end;
    // 在以上活动时间段中，每个人完成次数，对应可以领取的奖励
    public int[] rewards;

    /**
     * 所有玩家的信息
     */
    private HashMap<Long, LimitRankBean> users = new HashMap<>();

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
    public int flag = -1;

    /**
     * 初始化
     */
    @PostConstruct
    synchronized void init() {
        _instance = this;

        List<LimitRankBean> lrbs = limitRankBeanRepository.findAll();
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
        flushLimitTask();

        inited = true;
    }

    /**
     * 对跳表的操作，增加uid与分数的对应记录
     *
     * @param uid
     * @param score
     */
    private void addScore(long uid, long score) {
        LimitRankBean lrb = users.getOrDefault(uid, null);
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
        LimitRankBean lrb = users.getOrDefault(uid, null);
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
    private synchronized void flushLimitTask() {
        int today = TimeUtil.getToday();
        CrazylistConf thisFc = null;
        List<CrazylistConf> vcs = readonly.getCrazylistConfs();
        int lastKey = localService.getLimitday();
        for (CrazylistConf fest : vcs) {
            if (today >= fest.start && today <= fest.last) {
                // 开启时间超过10天可进行对应活动
                long tmp = localService.getOpenDate().getTime();
                tmp = LocalDateTime.ofEpochSecond(tmp / 1000, 0, ZoneOffset.ofHours(8)).toEpochSecond(ZoneOffset.ofHours(8));
                long least = (tmp + 10L * 24 * 3600) * 1000;
                long predict = System.currentTimeMillis();
                if (predict >= least) {
                    thisFc = fest;
                    // 如果存在活动就记录类型
                    flag = fest.flag;
                    break;
                }
            }
        }

        if (thisFc != null) { // 有任务，要检查任务是否和以前的任务标识相同
            this.begin = TimeUtil.fromDay(thisFc.start).atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8));
            this.end = TimeUtil.fromDay(thisFc.last + 1).atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8));
            // 是一个新的任务，需要结算之前任务的排行奖励
            if (lastKey != thisFc.key) {
                if (lastKey != 0) {
                    // 发送奖励
                    sendTaskAwards(lastKey);
                }

                // 将所有排行数据都重置
                users.clear();
                skipList = new SkipList<>(0L);
                localService.setLimitday(thisFc.key);
                this.limitRankBeanRepository.deleteAll();
            }
        } else {
//			log.error("----- 没有找到合适的限时排行活动 -----");
            if (lastKey != 0) {
                // 发送奖励
                sendTaskAwards(lastKey);
                // 将所有排行数据都重置
                users.clear();
                skipList = new SkipList<>(0L);
                localService.setLimitday(0);
                this.limitRankBeanRepository.deleteAll();
            }
        }
    }

    /**
     * 发送任务奖励
     *
     * @param lastKey 对应的任务配置表中的key
     */
    public synchronized void sendTaskAwards(int lastKey) {
        int key = -1;
        CrazylistConf cc = readonly.findCrazylistConf(lastKey);
        if (cc != null) {
            key = cc.flag;
        }
        String title = "注灵狂欢";
        List<Long> uids = range(1, 10);
        if (key == 2) {
            List<SoulrankConf> srs = readonly.getSoulrankConfs();
            int i = 1;
            for (long uid : uids) {
                LimitRankBean orb = users.getOrDefault(uid, null);
                if (orb != null) {
                    SoulrankConf sr = srs.get(i - 1);

                    StringBuilder sb = new StringBuilder();
                    sb.append(sr.reward1[0]);
                    sb.append(",");
                    sb.append(sr.reward1[1]);
                    // 排名达标，且次数达标，确实有奖励可拿，才可领取额外奖励
                    if (sr.limit > 0 && orb.getCount() >= sr.limit) {
                        sb.append(",");
                        sb.append(sr.reward2[0]);
                        sb.append(",");
                        sb.append(sr.reward2[1]);
                    }
                    GameServer.getInstance().sendAwardMail(orb.getId(), title, String.format("恭喜玩家在获得中获得第<color=red>" + i + "</color>名."), sb.toString());
                }
                ++i;
            }
        } else if (key == 3) {
            title = "活力狂欢";

            List<VigorrankConf> vcs = readonly.getVigorranks();
            int i = 1;
            for (long uid : uids) {
                LimitRankBean vlrb = users.getOrDefault(uid, null);
                if (vlrb != null) {
                    VigorrankConf vc = vcs.get(i - 1);

                    StringBuilder sb = new StringBuilder();
                    sb.append(vc.reward1[0]);
                    sb.append(",");
                    sb.append(vc.reward1[1]);

                    if (vc.limit > 0 && vlrb.getCount() >= vc.limit) {
                        sb.append(",");
                        sb.append(vc.reward2[0]);
                        sb.append(",");
                        sb.append(vc.reward2[1]);
                    }
                    GameServer.getInstance().sendAwardMail(vlrb.getId(), title, String.format("恭喜玩家在获得中获得第<color=red>" + i + "</color>名."), sb.toString());
                }
                ++i;
            }
        } else if (key == 4) {
            title = "英雄狂欢";

            List<SummonrankConf> scs = readonly.getSummonranks();
            int i = 1;
            for (long uid : uids) {
                LimitRankBean srb = users.getOrDefault(uid, null);
                if (srb != null) {
                    SummonrankConf sc = scs.get(i - 1);

                    StringBuilder sb = new StringBuilder();
                    sb.append(sc.reward1[0]);
                    sb.append(",");
                    sb.append(sc.reward1[1]);

                    if (sc.limit > 0 && srb.getCount() >= sc.limit) {
                        sb.append(",");
                        sb.append(sc.reward2[0]);
                        sb.append(",");
                        sb.append(sc.reward2[1]);
                    }
                    GameServer.getInstance().sendAwardMail(srb.getId(), title, String.format("恭喜玩家在获得中获得第<color=red>" + i + "</color>名."), sb.toString());
                }
                ++i;
            }
        }

        if (key > 0) {
            // 发送完奖励，活动类型标识还原
            flag = -1;
        }
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
            flushLimitTask();
        }

        // 20多分钟进行一次保存
        if (++cum >= 1000) {
            save();
            cum = 0;
        }
    }

    /**
     * 保存: 将变动的数据刷新到数据库中
     */
    synchronized void save() {
        changes.forEach((uid) -> {
            LimitRankBean orb = users.getOrDefault(uid, null);
            if (orb != null) {
                limitRankBeanRepository.save(orb);
            }
        });
        changes.clear();
    }


    /**
     * 活动中对应的任务完成后，增加分数
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
        if (flag != activity) {
            return;
        }
        LimitRankBean orb = users.getOrDefault(uid, null);
        if (orb == null) {
            orb = new LimitRankBean(uid);
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

        List<Long> uids = range(1, 10);
        int i = 1;
        for (long uid : uids) {
            LimitRankBean orb = users.getOrDefault(uid, null);
            if (orb != null) {
                rtn.add(new NameValue(orb.getName(), orb.getCount(), i));
            } else {
                rtn.add(new NameValue("匿名", 0, i));
            }
            ++i;
        }

        // 加上自己的排名
        LimitRankBean lrb = users.getOrDefault(self, null);
        if (lrb != null) {
            i = rank(self);
            rtn.add(new NameValue(lrb.getName(), lrb.getCount(), i));
        } else { // 无法查询到自己，则不提供，排名
            rtn.add(new NameValue("", 0, -1));
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

    /**
     * 请求领取对应次数的奖励
     *
     * @param uid,index
     * @return
     */
    public synchronized int[] requireAward(long uid, int index) {
        int[] rtn = new int[2];
        LimitRankBean lrb = users.getOrDefault(uid, null);
        if (lrb == null || flag < 0) {
            return rtn;
        }
        // 奖励领取过直接返回
        if (lrb.getAwd()[index] == 1) {
            return new int[]{-1, -1};
        }
        //根据不同节日配置文件，领取对应奖励
        switch (flag) {
            case 2:
                //获取个人奖励的所有条件列表
                // 用for收集
                List<SoulrankConf> scs = readonly.getSoulrankConfs().stream().filter(f -> f.limit3 > 0).collect(Collectors.toList());
                // 获取欲领取的对应下标配置
                SoulrankConf sc = scs.get(index);
                // 对应位置奖励未领取且可以领取
                if (lrb.getCount() >= sc.limit3 && lrb.getAwd()[index] != 1) {
                    rtn = sc.reward3;
                    lrb.updaAwd(index, 1);
                    changes.add(uid);
                }
                break;
            case 3:
                //获取个人奖励的所有条件列表
                List<VigorrankConf> vcs = readonly.getVigorranks().stream().filter(f -> f.limit3 > 0).collect(Collectors.toList());
                // 获取欲领取的对应下标配置
                VigorrankConf vc = vcs.get(index);
                // 对应位置奖励未领取且可以领取
                if (lrb.getCount() >= vc.limit3 && lrb.getAwd()[index] != 1) {
                    rtn = vc.reward3;
                    lrb.updaAwd(index, 1);
                    changes.add(uid);
                }
                break;
            case 4:
                //获取个人奖励的所有条件列表
                List<SummonrankConf> summons = readonly.getSummonranks().stream().filter(f -> f.limit3 > 0).collect(Collectors.toList());
                // 获取欲领取的对应下标配置
                SummonrankConf smc = summons.get(index);
                // 对应位置奖励未领取且可以领取
                if (lrb.getCount() >= smc.limit3 && lrb.getAwd()[index] != 1) {
                    rtn = smc.reward3;
                    lrb.updaAwd(index, 1);
                    changes.add(uid);
                }
                break;
        }
        return rtn;
    }

    /**
     * 获取指定uid的对应奖励结果标记
     *
     * @param uid
     */
    public int[] getRewardFlag(long uid) {
        LimitRankBean orb = users.getOrDefault(uid, null);
        int[] rtn = new int[5];
        if (orb != null) {
            rtn = orb.getAwd();
        }
        return rtn;
    }
}
