package com.tumei.groovy.commands

import com.tumei.ArenaData
import com.tumei.common.Readonly
import com.tumei.common.service.ServiceRouter
import com.tumei.dto.battle.FightResult
import com.tumei.common.utils.RandomUtil
import com.tumei.configs.RemoteService
import com.tumei.dto.arena.*
import com.tumei.dto.battle.HerosStruct
import com.tumei.groovy.contract.IArenaSystem
import com.tumei.groovy.contract.IBattle
import com.tumei.model.ArenaRoleBean
import com.tumei.model.ArenaSlotBean
import com.tumei.model.LadderGroup
import com.tumei.modelconf.SarenarewardConf
import com.tumei.modelconf.TopRankConf
import com.tumei.modelconf.TrmonsterConf
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Scheduled

/**
 * Created by Administrator on 2017/4/11 0011.
 * <p>
 * 跨服竞技场排名
 *
 */
class ArenaService implements IArenaSystem {
    private static final Log log = LogFactory.getLog(ArenaService.class)

    @Autowired
    private Readonly readonly

    @Autowired
    private ServiceRouter sr

    @Autowired
    private RemoteService remoteService

    @Autowired
    private ArenaData arenaData

    @Autowired
    private ApplicationContext ctx

    IBattle getBattle() {
        return ctx.getBean(IBattle.class)
    }

    // 每晚21点发放竞技场奖励
    @Override
    @Scheduled(cron = "0 0 21 * * ?")
    void arenaSchedule() {
        Map<Integer, List<Long>[]> rk = new HashMap<>()
        synchronized (this) {
            int key = 1
            SarenarewardConf trc = readonly.findArenalistConf(key)
            for (ArenaRoleBean arb : arenaData.ranks) {
                if (trc == null) {
                    break
                }

                // 先计算玩家所属的分区
                int zone = 1
                if (arb.id > 10000) {
                    zone = sr.chooseZone(arb.id)
                }

                List<Long>[] ls = rk[zone]
                if (ls == null) {
                    ls = new List<Long>[10]
                    rk.put(zone, ls)
                }

                // 每个游戏区，内找到当前奖励的类型分组，1-10
                List<Long> lss = ls[key - 1]
                if (lss == null) {
                    lss = new ArrayList<>()
                    ls[key - 1] = lss
                }

                // 每个分组内，加入玩家的id
                if (arb.id > 10000) {
                    lss.add(arb.id)
                }

                if ((arb.rank + 1) == trc.rank) {
                    ++key
                    trc = readonly.findArenalistConf(key)
                }
            }
        }

        int[][] awds = new int[10][]
        // 十种奖励直接搞列出来
        for (int i = 0; i < 10; ++i) {
            awds[i] = readonly.findArenalistConf(i+1).rankreward
        }

        // 以上已经近最快速度将所有玩家的奖励信息记录到了数据结构中，此时我们可以慢慢发放奖励了
        rk.forEach({zone, ls ->
            // 1. 根据游戏区
            // 2. 根据奖励类型
            for (int i = 0; i < ls.length; ++i) {
                // 注意
                if (ls[i] != null && ls[i].size() > 0) {
                    remoteService.sendAwards(zone, awds[i], ls[i])
                }
            }
        })
    }

    // 1000毫秒刷新一次, 正式环境 30分钟一次
    @Scheduled(fixedRate = 1800_000L)
    void update() {
        synchronized (this) {
            // save all changed ranks
            try {
                arenaData.saveChanges();
            } catch (Exception ex) {
                log.error("跨服竞技场服务器，保存失败.", ex);
            }
        }
    }

    // 每周一0点进行更新
    @Override
    @Scheduled(cron = "1 0 0 ? * MON")
    void schedule() {
        try {
            synchronized (this) {
                int count = readonly.findTopRankConf(1).newslot[arenaData.getZone() - 1]
                for (int i = 0; i < count; ++i) {
                    arenaData.createSlot(i)
                }

                for (int i = 0; i < arenaData.slotsSize(); ++i) {
                    arenaData.dirtySlot(i)
                }

                arenaData.users.forEach({k, arb ->
                    if (arb.slot != -1) {
                        arb.setSlot(-1)
                        arb.setGroup(6)
                        arb.setGindex(0)
                        arb.setRewardTime(0)
                        arb.setGroupTime(0)
                        arb.videos.clear()
                        arenaData.dirty(arb.id)
                    }
                })

                arenaData.saveChanges()
            }
        } catch (Exception e) {
            log.error("周一清理 天体赛 错误:" + e.message)
        }
    }

    /**
     * 根据玩家id，获取当前排名
     *
     * @param id
     * @return
     */
    ArenaInfo getInfo(long id) {
        ArenaInfo ai = new ArenaInfo()
        synchronized (this) {
            ArenaRoleBean rb = arenaData.findUser(id)
            if (rb == null) {
                return null
            }
            ai.rank = rb.rank

            int X = Readonly.getInstance().getArenaInterval(rb.rank)

            for (int i = 0; i < 20; ++i) {
                ai.peers.add(arenaData.ranks.get(i).createDto())
            }

            for (int i = 9; i >= 1; --i) {
                int tmp = rb.rank - X * i
                if (tmp >= 20 && tmp < arenaData.ranks.size()) {
                    ai.peers.add(arenaData.ranks.get(tmp).createDto())
                }
            }

            // 如果自己在20名之内，就不用再将自己打包了
            if (rb.rank >= 20) {
                ai.peers.add(rb.createDto())
            }

            int tmp = rb.rank
            for (int i = 1; i < 3; ++i) {
                tmp = tmp + X
                if (tmp >= 20 && tmp < arenaData.ranks.size()) {
                    ai.peers.add(arenaData.ranks.get(tmp).createDto())
                }
            }
        }
        return ai
    }

    /**
     * 获取指定玩家的最高历史排行
     *
     * @param uid
     * @return
     */
    synchronized int getPeekRank(long uid) {
        ArenaRoleBean rb = arenaData.findUser(uid)
        if (rb != null) {
            return rb.getPeek()
        }
        return 99998
    }

    /**
     * 进行交换
     *
     * @param uid
     * @param id
     * @param rank 别人的名次
     * @return -1: 排名不符
     * >=0: 排名变化导致钻石的提升
     */
    int exchange(long uid, long id, int rank) {
        int diff = 0
        int selfRank = -1
        String pname = null
        int selfGrade = 0

        synchronized (this) {
            ArenaRoleBean other = arenaData.findUser(id)
            if (other.getRank() != rank) {
                return -1
            }

            ArenaRoleBean self = arenaData.findUser(uid)
            selfRank = self.getRank()

            if (rank < selfRank) {
                other.setRank(selfRank)
                self.setRank(rank)

                pname = self.name
                selfGrade = self.grade

                // 列表也对应的进行交换
                arenaData.exchangeRank(self, selfRank, other, rank)

                diff = rank - self.getPeek()
                if (diff < 0) {
                    self.setPeek(rank)
                }
            }
        }

        if (id > 10000 && pname != null) {
            remoteService.sendFailInfo(id, pname, selfGrade, selfRank)
        }

        if (diff < -1) {
            remoteService.sendPeekAward(uid, -diff, rank)
        }

        if (diff < 0) {
            diff = -diff
        }

        return diff
    }


    /**
     * 提交信息
     * @param ard
     */
    @Override
    void submitInfo(ArenaRoleDto ard) {
        ArenaRoleBean rb
        synchronized (this) {
            rb = arenaData.findUser(ard.uid)
            if (rb == null) {
                rb = new ArenaRoleBean(ard.uid, arenaData.rankSize())
                arenaData.addRank(rb)
                arenaData.addUser(rb)
            }
            rb.update(ard)

            arenaData.dirty(rb.getId())
        }
    }

    /**
     * 玩家挑战 某个排名的人
     * @param uid
     * @param peerRank
     * @return
     */
    @Override
    ArenaFightResult fight(long uid, int peerRank) {
        ArenaFightResult result = new ArenaFightResult()

        long pid = -1
        HerosStruct hss = null;
        HerosStruct oss = null;

        synchronized (this) {
            ArenaRoleBean self = arenaData.findUser(uid)
            if (self == null) {
                result.reason = "参数错误"
                return result
            }

            if (peerRank < 20 && self.rank >= 40) {
                result.reason = "进入前四十名才能挑战前二十名的玩家"
                return result
            }

            // 1. 填充左边
            hss = self.info.clone()

            if (peerRank < 0 || peerRank >= arenaData.rankSize()) {
                result.reason = "挑战的名次参数错误"
                return result
            }

            ArenaRoleBean peer = arenaData.findRank(peerRank)
            if (peer == null) {
                result.reason = "参数错误"
                return result
            }
            pid = peer.id
            oss = peer.info.clone()
        }

        FightResult r = getBattle().doBattle(hss, oss)
        result.data = r.data

        if (r.win == 1) { // 胜利
            result.win = true

            // 1. 交换2个人的排名
            int rtn = exchange(uid, pid, peerRank)
            if (rtn < 0) {
                log.warn("排名发生变化")
                result.reason = "对方排名发生变化"
            } else if (rtn > 0) {
                result.rank = peerRank
                result.up = rtn
            }
        } else { // 失败
//            log.warn("挑战失败")
        }

        return result
    }

    /** 天体赛相关的接口 **/

    /***
     * 进入天梯界面
     *
     * @param uid
     * @return
     */
    synchronized LadderInfoDto enterLadder(long uid) {
        ArenaRoleBean arb = arenaData.findUser(uid)
        if (arb == null) {
            return null
        }

        LadderInfoDto dto = new LadderInfoDto()
        dto.rank = arb.getRank()
        dto.slot = arb.getSlot()
        dto.maxSlot = arenaData.slotsSize()
        // 没有选择slot没有继续计算的必要
        if (dto.slot < 0 || dto.slot >= dto.maxSlot) {
            if (dto.slot > 0) {
                arb.setSlot(-1)
                arb.setGroup(6)
                arb.setGindex(0)
                arb.setRewardTime(0)
                arb.setGroupTime(0)
                arb.videos.clear()
                arenaData.dirty(arb.id)
            }
            return dto
        }

        dto.honor = arb.flushHonor(true)
        if (dto.honor > 0) {
            arenaData.dirty(uid)
        }
        dto.next = arb.getRewardTime()

        // 创建分组
        dto.group = arb.getGroup()
        dto.gindex = arb.getGindex()

        ArenaSlotBean asb = arenaData.getSlot(dto.slot)
        for (LadderGroup lg : asb.groups) {
            lg.roles.stream().forEach({rid ->
                if (rid < 10000) { // 机器人填充
                    TrmonsterConf tc = readonly.findMonsterConf(rid as int)
                    dto.roles.add(tc.createSimpleDto())
                } else { // 玩家查询填充
                    ArenaRoleBean sarb = arenaData.findUser(rid)
                    dto.roles.add(sarb.createSimpleDto())
                }
            })
        }

        // 返回所有组的信息
        return dto
    }

    /**
     * 选择分组
     *
     * @param uid
     * @param slot
     * @return
     * < 0 表示失败
     * slot = 10 表示随机, 一个天梯分组不会到10个
     */
    synchronized int chooseSlot(long uid, int slot) {
        ArenaRoleBean arb = arenaData.findUser(uid)
        if (arb == null || arb.slot != -1) {
            return -1
        }

        boolean rnd = false
        // 随机分组
        if (slot == 10) {
            slot = RandomUtil.getRandom() % arenaData.slotsSize()
            rnd = true
        }

        // 参数错误
        if (slot < 0 || slot > 6) {
            return -2
        }

        arb.setSlot(slot)
        arb.setRandSlot(rnd)
        // 下次奖励时间
        arb.setRewardTime((System.currentTimeMillis() / 1000 + Readonly.reward_interval) as long)

        arb.setGroup(6)
        // 0表示不衰减
        arb.setGroupTime(0)

        arenaData.dirty(uid)

        return slot
    }

    synchronized LadderHonorDto getHonor(long uid) {
        ArenaRoleBean arb = arenaData.findUser(uid)
        if (arb == null) {
            return null
        }
        LadderHonorDto dto = new LadderHonorDto()
        dto.honor = arb.flushHonor(true)
        if (dto.honor > 0) {
            arenaData.dirty(uid)
        }
        dto.next = arb.getRewardTime()
        return dto
    }

    @Override
    List<LadderVideoDto> getVideos(long uid) {
        List<LadderVideoDto> dtos = new ArrayList<>();
        synchronized (this) {
            ArenaRoleBean arb = arenaData.findUser(uid)
            if (arb != null) {
                arb.getVideos().forEach({ lv -> dtos.add(lv.createDto())})
            }
        }
        return dtos
    }
/**
     * 玩家挑战 某个排名的人
     * @param uid
     * @param group [0, 5]
     * @return
     *
     * 先根据 uid 和 peer 进行战斗，战斗结束后，看看对应的group,index是不是这个人，如果是就切换到这个位置
     * 否则，告诉客户端，挑战的对手发生了变化，重新刷新后再次挑战
     *
     */
    @Override
    LadderFightResult fightLadder(long uid, long pid, int group, int index) {
        LadderFightResult result = new LadderFightResult()

        TopRankConf trc = readonly.findTopRankConf(group + 1)

        if (pid > 10000) { // 玩家
            String selfName = ""
            int selfGrade = 0
            int selfGroup = 6
            long groupTime = 0

            HerosStruct hss = null;
            HerosStruct oss = null;

            synchronized (this) {
                ArenaRoleBean self = arenaData.findUser(uid)
                if (self == null) {
                    result.reason = "参数错误"
                    return result
                }
                selfGroup = self.group

                if (trc.limit <= self.rank) {
                    result.reason = "跨服竞技场排名不满足条件，无法挑战"
                    return result
                }
                selfName = self.name
                selfGrade = self.grade

                ArenaRoleBean peer = arenaData.findUser(pid)
                if (peer == null) {
                    result.reason = "挑战的对手id不存在"
                    return result
                }

                if (peer.group != group || peer.gindex != index) {
                    result.reason = "对手排名发生变化，请重新发起挑战"
                    return result
                }

                hss = self.info.clone()
                oss = self.info.clone()
                groupTime = peer.groupTime
            }

            // 根据所在的天梯分组，增加个人的buff
            // 1. 自己的
            TopRankConf strc = Readonly.getInstance().findTopRankConf(selfGroup + 1)
            if (strc != null) {
                for (int i = 0; i < strc.attadd.length; i += 2) {
                    hss.buffs.merge(strc.attadd[i], strc.attadd[i+1], {a, b -> a + b})
                }
            }
            // 2. 对方的
            if (trc != null) {
                // 3. 对方的衰弱
                if (groupTime != 0) {
                    long now = System.currentTimeMillis() / 1000
                    // 间隔多少秒
                    int diff = (now - groupTime - trc.time[0]) / trc.time[1] as int
                    if (diff > 0) {
                        oss.weak = diff * 2
                        if (oss.weak > 20) {
                            oss.weak = 20
                        }
                    }
                }
            }

            FightResult r = getBattle().doBattle(hss, oss)
            result.data = r.data
            if (r.win == 1) { // 胜利
                result.win = true
            } else {
                result.win = false
            }

            // 挑战成功后需要给被挑战者增加失败视频记录
            synchronized (this) {
                ArenaRoleBean peer = arenaData.findUser(pid)
                if (peer != null) {
                    int z = sr.chooseZone(uid)
                    peer.addViedo(z, selfName, selfGrade, r.data, r.win)
                    arenaData.dirty(pid)
                }
            }

        } else {
            int selfGroup = 6;
            HerosStruct hss = null;
            synchronized (this) {
                ArenaRoleBean self = arenaData.findUser(uid)
                if (self == null) {
                    result.reason = "参数错误"
                    return result
                }
                selfGroup = self.group

                if (trc.limit <= self.rank) {
                    result.reason = "跨服竞技场排名不满足条件，无法挑战"
                    return result
                }

                long target = arenaData.getSlotGroupRole(self.slot, group, index)
                if (target != pid) {
                    result.reason = "对手排名发生变化，请重新发起挑战"
                    return result
                } else {
                    hss = self.info.clone()
                }
            }
            // 1. 自己的
            TopRankConf strc = Readonly.getInstance().findTopRankConf(selfGroup + 1)
            if (strc != null) {
                for (int i = 0; i < strc.attadd.length; i += 2) {
                    hss.buffs.merge(strc.attadd[i], strc.attadd[i+1], {a, b -> a + b})
                }
            }

            TrmonsterConf tc = readonly.findMonsterConf(pid as int)

            FightResult r = getBattle().doSceneBattle(hss, tc.create(), 0, false, 0, 0, 0, 0)
            result.data = r.data
            if (r.win == 1) { // 胜利
                result.win = true
            } else {
                result.win = false
            }
        }

        if (result.win) { // 胜利
            synchronized (this) {
                ArenaRoleBean self = arenaData.findUser(uid)

                if (pid > 10000) {
                    ArenaRoleBean peer = arenaData.findUser(pid)
                    // 此刻还得查看对方的排名没有发生过变化
                    if (self != null && peer != null && peer.group == group && peer.gindex == index) {
                        if (group < self.group) {
                            // 交换排名之前先刷荣誉
                            self.flushHonor(false)
                            peer.flushHonor(false)

                            // 交换排名
                            changeLadderPosition(self, peer)
                        }
                    } else {
                        result.reason = "对方所属段位发生变化"
                    }
                } else { // 对方npc
                    long target = arenaData.getSlotGroupRole(self.slot, group, index)
                    if (target != pid) {
                        result.reason = "对手排名发生变化，请重新发起挑战"
                    } else if (self.group > group) {
                        self.flushHonor(false)

                        int oldGroup = self.group
                        int oldGindx = self.gindex

                        // 将自己的排名提升到这个位置
                        arenaData.setSlotGroupRole(self.slot, group, index, self.id)
                        self.group = group
                        self.gindex = index
                        self.groupTime = System.currentTimeMillis() / 1000
                        arenaData.dirty(self.id)

                        if (oldGroup < 6) { // 只有不是青铜守卫的情况才需要交换怪物的位置
                            arenaData.setSlotGroupRole(self.slot, oldGroup, oldGindx, target)
                        }

                        arenaData.dirtySlot(self.slot)
                    }
                }
            }
        }

        return result
    }

    /**
     * 交换2个玩家的天梯排行
     * @param self
     * @param peer
     */
    private void changeLadderPosition(ArenaRoleBean self, ArenaRoleBean peer) {
        // 交换两个人的位置
        int a = self.group
        int ai = self.gindex
        int b = peer.group
        int bi = peer.gindex

        ArenaSlotBean asb = arenaData.getSlot(self.slot)
        if (asb != null) {
            // 将对方放到自己的位置
            if (a < 6) {
                asb.groups[a].roles.set(self.gindex, peer.id)
            }

            // 将自己放到对方的位置
            if (b < 6) {
                asb.groups[b].roles.set(peer.gindex, self.id)
            }
        }

        self.group = b
        self.gindex = bi
        peer.group = a
        peer.gindex = ai

        // 分组变化后需要重新计算衰弱时间
        long now = System.currentTimeMillis() / 1000
        self.groupTime = now
        peer.groupTime = now

        arenaData.dirtySlot(self.slot)

        arenaData.dirty(self.id)
        arenaData.dirty(peer.id)
    }


}
