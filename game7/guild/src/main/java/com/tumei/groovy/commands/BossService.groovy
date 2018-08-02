package com.tumei.groovy.commands

import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.common.service.ServiceRouter
import com.tumei.dto.battle.DirectHeroStruct
import com.tumei.dto.battle.FightResult
import com.tumei.common.utils.Defs
import com.tumei.common.utils.TimeUtil
import com.tumei.common.webio.BattleResultStruct
import com.tumei.controller.GroupService
import com.tumei.dto.battle.HerosStruct
import com.tumei.dto.boss.BossDto
import com.tumei.dto.boss.BossGuildDto
import com.tumei.dto.boss.BossRoleDto
import com.tumei.groovy.contract.IBattle
import com.tumei.groovy.contract.IBossSystem
import com.tumei.model.*
import com.tumei.modelconf.BossConf
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled

import javax.annotation.PostConstruct
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Created by Leon on 2018/2/5.
 *
 * Boss战的逻辑
 *
 * boss战斗
 * 个人排序
 * 公会排序
 * 发送奖励
 * 重置boss
 *
 */
class BossService implements IBossSystem {
    static final Log log = LogFactory.getLog(BossService.class)

    // 个人最大排名
    static final int maxrank = 99

    // 公会最大排名
    static final int maxgrank = 49

    @Autowired
    private ServiceRouter sr

    @Autowired
    private ApplicationContext ctx

    /**
     * 怪物
     */
    @Autowired
    private BossBeanRepository bossBeanRepository

    @Autowired
    private Readonly readonly;

    @Autowired
    private RemoteService remoteService

    @Autowired
    private GroupService groupService;

    /**
     * 今日的boss信息
     */
    private BossBean bossBean;

    /**
     * 有序的boss战斗的角色信息
     * 前五十名伤害的角色
     */
    private List<BossRoleBean> ranks = new ArrayList<>();

    private HashMap<Long, BossRoleBean> users = new HashMap<>();

    /**
     * 有序的boss战斗的公会信息
     *
     * 前五十名公会
     */
    private List<BossGuildBean> gRanks = new ArrayList<>();

    private HashMap<Long, BossGuildBean> gUsers = new HashMap<>();


    /**
     * 今天boss战的开启时间
     */
    private long startTime;

    /**
     * 首领战是否在运行中
     */
    private volatile boolean running;

    /**
     * 是否已经准备好数据
     */
    private volatile boolean preRunning;

    @PostConstruct
    void init() {
        log.info("+++ 初始化bossService")
        checkNextStartTime()
        checkBossBean(false)
    }

    void enable(boolean flag) {
        running = flag
    }

    IBattle getBattle() {
        return ctx.getBean(IBattle.class)
    }

    /**
     * 获取下一个开始boss战的时间
     */
    void checkNextStartTime() {
        def today = LocalDate.now()
        def that = today.atStartOfDay().plusHours(19).plusMinutes(30 );
        def time = that.toEpochSecond(ZoneOffset.ofHours(8))
        if (time != startTime) {
            log.info("+++++ 今日首领战启动时间为:" + that.toString());
            preRunning = false
            startTime = time
        }
    }


    /**
     * 新的一天需要创建新的boss,老的boss不要删除,用于查看历史记录
     */
    void checkBossBean(boolean force) {
        int today = TimeUtil.getToday();
        bossBean = bossBeanRepository.findByDay(today);

        int level = 1
        List<BossBean> bbs = bossBeanRepository.findAll(new Sort("day"));
        // 找到最近的一次的BossBean,判断下一个boss生成的等级
        if (bbs.size() > 0) {
            BossBean latest = bbs.get(bbs.size() - 1);
            if (latest.isDead()) {
                int killTime = latest.getKillTime();
                if (killTime <= 120) {
                    level = latest.getLevel() + 4;
                } else if (killTime <= 240) {
                    level = latest.getLevel() + 3;
                } else if (killTime <= 360) {
                    level = latest.getLevel() + 2;
                } else {
                    level = latest.getLevel() + 1;
                }
            } else {
                level = latest.getLevel();
            }
        }

        if (bossBean == null) {
            bossBean = new BossBean()
        } else if (!force) { // 不强制刷新直接读取数据库返回
            return
        }


        bossBean.setDay(today)
        bossBean.setKillTime(0)
        bossBean.sendAwards = 0
        bossBean.level = level
        bossBean.killer = ""

        // 根据当前等级和配置设置血量
        BossConf bc = readonly.findBossConf(level)
        while (bc == null) {
            --level;
            bc = readonly.findBossConf(level)
        }

        /**
         * 创建boss相关信息
         */
        bossBean.createPeers(bc)
        bossBeanRepository.save(bossBean)
    }

    /**
     * 一天检查一次今日的boss战开启时间, 重启也会调用,只是在不重启的时候自动更新时间和boss
     */
    @Scheduled(cron = "0 0/1 0 * * ?")
    void schedule() {
        synchronized (this) {
            // 检查新的boss开始时间
            checkNextStartTime()
        }
    }

    /**
     * 每晚19点30 开始广播boss战的信息:
     *
     */
    @Scheduled(cron = "0/1 * * * * ?")
    void update() {
        def now = System.currentTimeMillis() / 1000;

        def diff = Math.round(startTime - now);
        if (diff <= 1800 && diff >= -2000) {
            log.info("距离开始时间:" + diff)
            // 开启战斗之前，如果没有准备就进行数据清理和装备工作
            if (diff > 0 && !running && !preRunning) { // 开始前
                preRunning = true
                ranks.clear()
                gRanks.clear()
                users.clear()
                gUsers.clear()
                checkBossBean(true)
                log.info("------- 刷新排行榜和首领 ------------")
            } else if (diff <= 0 && diff > -10) {
                synchronized (this) {
                    if (!running) {
                        if (bossBean.killTime == 0) {
                            running = true
                        }
                    }
                }
            } else if (diff >= -1800) { // 战斗时间段
            } else if (running) { // boss没死，到了时间就关闭首领战
                conclusion()
            }
        }
    }

    /**
     * 结算当前boss战斗, 最后在结束后1到2分钟在进行,服务器之间时间不一定相同
     */
    void conclusion() {
        Map<Integer, Map<Long, Integer>> rk = new HashMap<>()
        Map<Integer, Map<Long, Integer>> grk = new HashMap<>()

        synchronized (this) {
            if (bossBean.sendAwards != 0) {
                return
            }

            running = false
            // 1. 再来填充bossBean的状态

            // 2. 个人排名同步到每个服务器,然后自己发送奖励
            int rank = 1;
            for (BossRoleBean brb : ranks) {
                // 先计算玩家所属的分区
                int zone = 1
                if (brb.id > 10000) {
                    zone = sr.chooseZone(brb.id)
                }

                Map<Long, Integer> ls = rk[zone]
                if (ls == null) {
                    ls = new HashMap<>()
                    rk.put(zone, ls)
                }

                ls.put(brb.id, rank)

                ++rank
            }

            // 2. 根据伤害,发送公会奖励
            rank = 1;
            for (BossGuildBean brb : gRanks) {
                // 根据brb的id也就是公会id，找到公会所有玩家id
                GroupBean gb = groupService.find(brb.id)
                for (long uid :gb.roles.keySet()) {
                    int zone = 1
                    if (uid > 10000) {
                        zone = sr.chooseZone(uid)
                    }

                    Map<Long, Integer> ls = grk[zone]
                    if (ls == null) {
                        ls = new HashMap<>()
                        grk.put(zone, ls)
                    }
                    ls.put(uid, rank)
                }

                ++rank
            }
        }

        rk.forEach({zone, ls ->
            // 1. 根据游戏区
            // 2. 根据奖励类型
            if (ls.size() > 0) {
                remoteService.sendBossAwards(zone, ls)
            }
        })

        grk.forEach({zone, ls ->
            // 1. 根据游戏区
            // 2. 根据奖励类型
            if (ls.size() > 0) {
                remoteService.sendBossGroupAwards(zone, ls)
            }
        })

        bossBean.sendAwards = 1
        bossBeanRepository.save(bossBean)
        log.info("***** 首领战发送奖励完毕 *****")
    }

    /**
     * 当前boss是否死亡, 在战斗中进行查看
     * @return
     */
    synchronized boolean isDead() {
        return bossBean.isDead()
    }

    /**
     * 获取当前boss的血量
     * @return
     */
    synchronized long bossLife() {
        return bossBean.life()
    }

    /**
     * 获取个人伤害排名
     * @return
     */
    List<BossRoleDto> getRanks() {
        List<BossRoleDto> rtn = new ArrayList<>();
        synchronized (this) {
            int max = Math.min(ranks.size(), 20)
            for (int i = 0; i < max; ++i) {
                rtn.add(ranks.get(i).createDto())
            }
        }
        return rtn
    }

    /**
     * 获取公会伤害排名
     * @return
     */
    List<BossGuildDto> getGuildRanks() {
        List<BossGuildDto> rtn = new ArrayList<>();
        synchronized (this) {
            int max = Math.min(gRanks.size(), 10)
            for (int i = 0; i < max; ++i) {
                rtn.add(gRanks.get(i).createDto())
            }
        }
        return rtn
    }

    /**
     * boss首页需要的内容
     * @return
     */
    BossDto getInfo(long uid, String name) {
        BossDto dto = new BossDto()

        String guild = ""
        long gid = 0
        GroupBean gb = groupService.findByUid(uid)
        if (gb != null) {
            guild = gb.name
            gid = gb.id
        }

        synchronized (this) {
            BossRoleBean brb = users.getOrDefault(uid, null)
            if (brb == null) {
                brb = new BossRoleBean(uid, name, gid, guild)
                users.put(uid, brb)
                updateRank(brb)
            } else {
                brb.name = name
                brb.gid = gid
                brb.guild = guild
            }

            dto.level = bossBean.level
            dto.life = bossBean.life()
            dto.killer = bossBean.killer
            dto.rank = brb.rank
            dto.harm = brb.harm
            dto.topharm = brb.topharm

            if (gid > 0) {
                BossGuildBean bgb = gUsers.getOrDefault(gid, null)
                if (bgb == null) {
                    bgb = new BossGuildBean(gid, guild)
                    gUsers.put(gid, bgb)
                    updateGuildRank(bgb)
                }
                dto.grank = bgb.rank
            }
        }
        return dto
    }

    /**
     * 获取boss当前生命值
     * @return
     */
    long getLife() {
        synchronized (this) {
            return bossBean.life()
        }
    }

    /**
     * 玩家的伤害与ranks进行对比,更新ranks
     * @param uid
     * @param harm
     */
    void updateRank(BossRoleBean role) {
        int last = role.getRank()
        if (last == 0) { // 没有排名,就和排名最后一个人进行比较
            int dest = ranks.size() - 1

            if (dest < maxrank) { // 当前ranks没有满,则直接填入
                role.rank = ranks.size() + 1
                ranks.add(role)
                last = role.rank
            } else { // 满了
                def prev = ranks.get(dest)
                if (prev.harm < role.harm) { // 交换
                    ranks.set(dest, role)
                    role.rank = prev.rank
                    prev.rank = 0
                    last = role.rank
                } else { // 空位也没有了,直接放弃
                    return
                }
            }
        }

        // 上述和最后一个人是否比较之后,再和当前更前的人进行比较一直到第一名

        for (int i = last - 2; i >= 0; --i) {
            def prev = ranks.get(i)
            if (prev.harm < role.harm) { // 交换
                ranks.set(i, role)
                ranks.set(i + 1, prev)
                role.rank = prev.rank
                ++prev.rank
            } else {
                break
            }
        }
    }

    /**
     * 玩家的伤害与ranks进行对比,更新ranks
     * @param uid
     * @param harm
     */
    void updateGuildRank(BossGuildBean role) {
        int last = role.getRank()
        if (last == 0) { // 没有排名,就和排名最后一个人进行比较
            int dest = gRanks.size() - 1
            if (dest < maxgrank) { // 当前ranks没有满,则直接填入
                role.rank = gRanks.size() + 1
                gRanks.add(role)
                last = role.rank
            } else { // 满了
                def prev = gRanks.get(dest)
                if (prev.harm < role.harm) { // 交换
                    gRanks.set(dest, role)
                    role.rank = prev.rank
                    prev.rank = 0
                    last = role.rank
                } else { // 空位也没有了,直接放弃
                    return
                }
            }
        }

        // 上述和最后一个人是否比较之后,再和当前更前的人进行比较一直到第一名

        for (int i = last - 2; i >= 0; --i) {
            def prev = gRanks.get(i)
            if (prev.harm < role.harm) { // 交换
                gRanks.set(i, role)
                gRanks.set(i + 1, prev)
                role.rank = prev.rank
                ++prev.rank
            } else {
                break
            }
        }
    }

    synchronized BattleResultStruct callFight(HerosStruct bs) {
        BattleResultStruct rl = new BattleResultStruct();
        if (!running) {
            rl.result = "首领战休战中"
            return rl
        }

        BossRoleBean role  = users.getOrDefault(bs.uid, null)
        if (role == null) {
            rl.result = "请重新进入首领战界面，提交今日信息参战."
            return rl
        }

        if (bossBean.isDead()) {
            rl.result = "首领已被击杀";
        } else {
            try {
                FightResult r = getBattle().doSceneBattle(bs, bossBean.peers.toList(), 0, true, 0, 0, 0, 0)

                if (r == null) {
                    rl.result = "战斗服务器维护中";
                } else {
                    if (r.win < 1) {
                        rl.result = "战斗出错";
                    } else {    // 胜利，就是击杀

                        log.info("结果:" + r.win)
                        if (r.win == 1) {
                            bossBean.setKillTime((long)(System.currentTimeMillis() / 1000) - startTime)
                            rl.kill = 1;
                        }

                        long harm = 0;
                        for (int j = 0; j < bossBean.peers.size(); ++j) {
                            DirectHeroStruct shs = bossBean.peers[j];
                            if (shs != null) {
                                long l = r.lifes.get(j);
//								log.info("英雄(" + shs.hero + ") life:" + shs.life + "  现在:" + l);
                                harm += (shs.life - l);
                                shs.life = l;
                            }
                        }

                        // 调整最大伤害
                        if (role.topharm < harm) {
                            role.topharm = harm;
                        }
                        // 增加累计伤害
                        role.harm += harm
                        rl.harm = harm

                        updateRank(role)

                        BossGuildBean guild = gUsers.getOrDefault(role.gid, null)
                        if (guild != null) {
                            guild.harm += harm
                            updateGuildRank(guild)
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("错误原因:" + ex.getMessage());
                ex.printStackTrace();
                rl.result = "战斗出错";
            }
        }

        if (rl.kill == 1) {
            bossBean.killer = role.name
            remoteService.broadcast("恭喜玩家" + Defs.getColorString(5, role.name) + "对首领完成最后一击，获得丰厚奖励!")
//            conclusion()
        }

        log.info("boss击杀:" + rl.kill)
        return rl
    }
}

