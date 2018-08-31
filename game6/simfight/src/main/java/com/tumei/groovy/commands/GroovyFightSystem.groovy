package com.tumei.groovy.commands

import com.tumei.common.fight.*
import com.tumei.common.utils.Defs
import com.tumei.common.utils.JsonUtil
import com.tumei.common.utils.RandomUtil
import com.tumei.groovy.contract.IFightSystem
import com.tumei.modelconf.*
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.util.stream.Collectors

/**
 *  矿区系统(第一个脚本化的系统)
 *
 *
 */
@Component
@Scope(value = "prototype")
class GroovyFightSystem implements IFightSystem {
    private static Log logger = LogFactory.getLog(IFightSystem.class)

    static final int 前列 = 1
    static final int 后列 = 2

    /**
     * 进攻方英雄列表
     */
    private List<FightHero> left = new ArrayList<>()
    /**
     * 防守方英雄列表
     */
    private List<FightHero> right = new ArrayList<>()

    // 左方神器
    private List<int[]> leftArt = new ArrayList<>()

    // 右方神器
    private List<int[]> rightArt = new ArrayList<>()

    /**
     * 返回战斗初始状态和结果
     */
    private FightResultStruct frb = new FightResultStruct()
    /**
     * 回合数
     */
    private int round
    /**
     * 回合中的出手序号
     */
    private int roundindex
    /**
     * 最大回合数
     */
    private int maxRound = 11

    /**
     * 随机生成器
     */
    private Random random

    /**
     * 战斗结果，只有==1是胜利，其他全部失败
     */
    private int winResult = 0

    /**
     * 胜利条件
     */
    private int condition = 0

    /**
     * 攻击顺序，每个回合都会发生影响
     */
    private LinkedList<FightHero> orderList = new LinkedList<>()

    /**
     * 动作序列
     */
    private List<ActionRecord> actions = new ArrayList<>()

    /**
     * 当前的动作
     */
    private ActionRecord currentAction = null

    private int getRandom() {
        return Math.abs(random.nextInt() % 100)
    }

    private int getRandom1000() {
        return Math.abs(random.nextInt() % 1000)
    }

    /**
     * 返回特定队伍的所有成员
     *
     * @param side
     * @return
     */
    List<FightHero> getTeam(int side) {
        if (side == FightHero.左边) {
            return left
        }
        return right
    }

    /**
     * 获取英雄信息
     *
     * @param side
     * @param index
     * @return
     */
    FightHero getHeroInfo(int side, int index) {
        return getTeam(side).get(index)
    }

    GroovyFightSystem() {
        round = 1
        roundindex = 0
        random = new Random(System.currentTimeMillis())
    }

    /**
     * 输出左边人员的战斗属性
     * @return
     */
    String debugLeft() {
        String msg = ""
        for (FightHero fh in left) {
            if (fh != null) {
                msg += fh.toString() + "\n"
            }
        }
        return msg
    }

    /**
     * 单纯计算战斗力
     *
     * @return
     */
    @Override
    long calcPower(PowerStruct ts) {

        List<HeroStruct> _heroStructs = ts.getHeros()
        List<FightHero> his = left

        HashMap<Integer, Float> allattrs = new HashMap<Integer, Float>()
        HashMap<Integer, Float> attrs1 = new HashMap<Integer, Float>()
        HashMap<Integer, Float> attrs2 = new HashMap<Integer, Float>()
        HashMap<Integer, Float> attrs3 = new HashMap<Integer, Float>()
        HashMap<Integer, Float> attrs4 = new HashMap<Integer, Float>()

        if (ts.getBuffs() != null) {
            allattrs.putAll(ts.getBuffs())
        }

        if (ts.lineups == null) {
            ts.lineups = new int[6]
        }

        int i = 0
        for (HeroStruct hb : _heroStructs) {
            if (hb != null && !hb.assist) {
                HashMap<Integer, Float> attrs = null
                HeroConf hbi = Readonly.getInstance().findHero(hb.hero)
                switch (hbi.sect) {
                    case 1:
                        attrs = attrs1
                        break
                    case 2:
                        attrs = attrs2
                        break
                    case 3:
                        attrs = attrs3
                        break
                    case 4:
                        attrs = attrs4
                        break
                }

                FightHero hi = new FightHero(this, FightHero.左边, i, hb, allattrs, attrs, ts.lineups[i])
                his.add(hi)
                orderList.add(hi)
            }
            ++i
            // 超过12个英雄显然不可能
            if (i >= 12) {
                break
            }
        }

        // ----------------- begin 神器 -----------------

        List<ArtifactStruct> ass = ts.getArts()
        if (ass != null) {
            for (ArtifactStruct af : ass) {
                for (ArtcomStruct ac : af.coms) {
                    ArtpartConf conf = Readonly.instance.findArtpart(ac.id)
                    if (conf != null) {
                        int lv = ac.level - 1

                        // 根据升星计算提升的倍率
                        int slv = (ac.star / 10) as int
                        ArtpartstupConf asc = Readonly.instance.findArtpartup(slv)
                        float lvup = 1.0f
                        if (asc != null) {
                            lvup += asc.attupall/100f
                        }

                        // 基础
                        for (int m = 0; m < conf.basicatt.length; ++m) {
                            int[] tmp = conf.basicatt[m]
                            allattrs.merge(tmp[0], tmp[1] * lvup as float, {a, b -> a + b})
                        }
                        // 强化
                        if (lv > 0) {
                            for (int m = 0; m < conf.stratt.length; ++m) {
                                int[] tmp = conf.stratt[m]
                                allattrs.merge(tmp[0], tmp[1] * lv * lvup as float, {a, b -> a + b})
                            }
                        }

                        // 附加英雄，遍历自己的所有英雄，只要满足conf.advtag,则附加
                        his.forEach({h ->
                            // 只要满足，则增加附加属性给该英雄
                            if (Arrays.stream(conf.advtag).anyMatch({a -> a == h.heroStruct.hero})) {
                                for (int m = 0; m < conf.advatt.length; ++m) {
                                    int[] tmp = conf.advatt[m]
                                    if (ac.level >= tmp[0]) {
                                        h.preDealAttrs(tmp[1], tmp[2], null, null)
                                    }
                                }
                            }
                        })
                    }
                }

                // 此时再判断是否完整神器，以及完整神器带的属性加成
                if (af.level > 0) {
                    ArtifactConf conf = Readonly.instance.findArtifact(af.id)
                    if (conf != null) {
                        // 基础
                        for (int m = 0; m < conf.batt.length; m += 2) {
                            allattrs.merge(conf.batt[m], conf.batt[m+1], {a, b -> a + b })
                        }

                        // 强化
                        if (af.level > 1) {
                            for (int m = 0; m < conf.attstr.length; m += 2) {
                                allattrs.merge(conf.attstr[m], conf.attstr[m+1] * (af.level - 1), {a, b -> a + b })
                            }
                        }

                        // 附加
                        for (int m = 0; m < conf.satt.length; ++m) {
                            int[] tmp = conf.satt[m]
                            if (af.level >= tmp[0]) {
                                allattrs.merge(tmp[1], tmp[2], {a, b -> a + b })
                            }
                        }
                    }
                }
            }
        }

        // ----------------- end   神器 -----------------

        for (FightHero hi : his) {
            HashMap<Integer, Float> attrs = null
            switch (hi.sect) {
                case 1:
                    attrs = attrs1
                    break
                case 2:
                    attrs = attrs2
                    break
                case 3:
                    attrs = attrs3
                    break
                case 4:
                    attrs = attrs4
                    break
            }
            hi.handleSkills(_heroStructs, allattrs, attrs)
        }

        long power = 0

        // 将全体与阵营提升合并到英雄的属性提升中再处理
        for (FightHero hi : his) {
            HashMap<Integer, Float> attrs = null
            switch (hi.sect) {
                case 1:
                    attrs = attrs1
                    break
                case 2:
                    attrs = attrs2
                    break
                case 3:
                    attrs = attrs3
                    break
                case 4:
                    attrs = attrs4
                    break
            }

            hi.merge(allattrs, attrs)
            double p = (hi.maxLife * 0.02 + hi.attack * 0.3 + hi.defs * 0.5 + hi.mdef * 0.5) * (1.0 + hi.critical / 1000.0 + hi.aim / 1000.0 + hi.dodge / 1000.0 + hi.antiCrit / 1000.0 + hi.enHarm / 1000.0 - hi.overHarm / 1000.0 + hi.critHarm / 100.0)
            // 单个英雄攻击力大于一定值, 记录该英雄的详细属性
//            if (hi.attack > 60_000_000) {
//                logger.info("玩家(${ts.uid})数据异常:\n" + hi.debug(p))
//                if (ts.lineups != null) {
//                    logger.info("lineups:" + ts.lineups)
//                }
//                if (ts.getBuffs() != null) {
//                    logger.info("buffs:" + ts.getBuffs())
//                }
//            }

            power += (long)p
        }

        return power
    }

    /**
     * 玩家战斗数据构建队伍
     *
     * @param side
     */
    @Override
    void buildTeam(int side, FightStruct fs) {
        if (side == 1) {
            _build(fs.uid, side, fs.left, fs.getBuffs(), fs.getLineups(), 0, fs.getArts1())
        } else {
            _build(fs.uid, side, fs.right, fs.getBuffs2(), fs.getLineups2(), fs.weak, fs.getArts2())
        }
    }

    @Override
    void buildSceneTeam(int side, SceneFightStruct sfs) {
        _build(sfs.uid, side, sfs.left, sfs.getBuffs(), sfs.getLineups(), 0, sfs.getArts())
        if (sfs.condition > 0) {
            this.condition = sfs.condition
        }
    }

    @Override
    void buildGroupTeam(int side, GroupFightStruct gfs) {
        _build(gfs.uid, side, gfs.left, gfs.getBuffs(), null, 0, gfs.getArts())
    }

    void _build(long uid, int side, List<HeroStruct> _heroStructs, Map<Integer, Float> _buffs, int[] lineups, int weak, List<ArtifactStruct> ass) {
        List<FightHero> his = null
        if (side == FightHero.左边) {
            his = left
        } else {
            his = right
        }

        HashMap<Integer, Float> allattrs = new HashMap<>()

        // 四个阵营提升,暂存的效果
        HashMap<Integer, Float> attrs1 = new HashMap<>()
        HashMap<Integer, Float> attrs2 = new HashMap<>()
        HashMap<Integer, Float> attrs3 = new HashMap<>()
        HashMap<Integer, Float> attrs4 = new HashMap<>()

        if (_buffs != null) {
            allattrs.putAll(_buffs)
        }

        if (lineups == null) {
            lineups = new int[6]
        }

        // 遍历英雄列表，创建对应的HeroInfo战斗单元
        int i = 0
        for (HeroStruct hb : _heroStructs) {
            if (hb != null && !hb.assist) {
                HashMap<Integer, Float> attrs = null
                HeroConf hbi = Readonly.getInstance().findHero(hb.hero)
                switch (hbi.sect) {
                    case 1:
                        attrs = attrs1
                        break
                    case 2:
                        attrs = attrs2
                        break
                    case 3:
                        attrs = attrs3
                        break
                    case 4:
                        attrs = attrs4
                        break
                }

                FightHero hi = new FightHero(this, side, i, hb, allattrs, attrs, lineups[i])
                his.add(hi)
                orderList.add(hi)
            }
            ++i
        }

        // ----------------- begin 神器 -----------------
        if (ass != null) {
            for (ArtifactStruct af : ass) {
                for (ArtcomStruct ac : af.coms) {
                    ArtpartConf conf = Readonly.instance.findArtpart(ac.id)
                    if (conf != null) {
                        int lv = ac.level - 1
                        // 根据升星计算提升的倍率
                        int slv = (ac.star / 10) as int
                        ArtpartstupConf asc = Readonly.instance.findArtpartup(slv)
                        float lvup = 1.0f
                        if (asc != null) {
                            lvup += asc.attupall/100f
                        }

                        // 基础
                        for (int m = 0; m < conf.basicatt.length; ++m) {
                            int[] tmp = conf.basicatt[m]
                            allattrs.merge(tmp[0], tmp[1] * lvup as float, {a, b -> a + b})
                        }
                        // 强化
                        if (lv > 0) {
                            for (int m = 0; m < conf.stratt.length; ++m) {
                                int[] tmp = conf.stratt[m]
                                allattrs.merge(tmp[0], tmp[1] * lv * lvup as float, {a, b -> a + b})
                            }
                        }

                        // 附加英雄，遍历自己的所有英雄，只要满足conf.advtag,则附加
                        his.forEach({h ->
                            // 只要满足，则增加附加属性给该英雄
                            if (Arrays.stream(conf.advtag).anyMatch({a -> a == h.heroStruct.hero})) {
                                for (int m = 0; m < conf.advatt.length; ++m) {
                                    int[] tmp = conf.advatt[m]
                                    if (ac.level >= tmp[0]) {
                                        h.preDealAttrs(tmp[1], tmp[2], null, null)
                                    }
                                }
                            }
                        })
                    }
                }

                // 此时再判断是否完整神器，以及完整神器带的属性加成
                if (af.level > 0) {
                    ArtifactConf conf = Readonly.instance.findArtifact(af.id)
                    if (conf != null) {
                        // 基础
                        for (int m = 0; m < conf.batt.length; m += 2) {
                            allattrs.merge(conf.batt[m], conf.batt[m+1], {a, b -> a + b })
                        }

                        // 强化
                        if (af.level > 1) {
                            for (int m = 0; m < conf.attstr.length; m += 2) {
                                allattrs.merge(conf.attstr[m], conf.attstr[m+1] * (af.level - 1), {a, b -> a + b })
                            }
                        }

                        // 附加
                        for (int m = 0; m < conf.satt.length; ++m) {
                            int[] tmp = conf.satt[m]
                            if (af.level >= tmp[0]) {
                                allattrs.merge(tmp[1], tmp[2], {a, b -> a + b })
                            }
                        }
                        if (conf.bateff.length > 0) {
                            if (side == FightHero.左边) {
                                leftArt.add(conf.bateff)
                            } else {
                                rightArt.add(conf.bateff)
                            }
                        }
                    }
                }
            }
        }

        // ----------------- end   神器 -----------------

        // 上述英雄遍历结束后，再次处理技能之间的组合，缘分等信息.
        for (FightHero hi : his) {
            HashMap<Integer, Float> attrs = null
            switch (hi.sect) {
                case 1:
                    attrs = attrs1
                    break
                case 2:
                    attrs = attrs2
                    break
                case 3:
                    attrs = attrs3
                    break
                case 4:
                    attrs = attrs4
                    break
            }
            hi.handleSkills(_heroStructs, allattrs, attrs)
        }

        // 将全体与阵营提升合并到英雄的属性提升中再处理
        for (FightHero hi : his) {
            HashMap<Integer, Float> attrs = new HashMap<>()
            switch (hi.sect) {
                case 1:
                    attrs = attrs1
                    break
                case 2:
                    attrs = attrs2
                    break
                case 3:
                    attrs = attrs3
                    break
                case 4:
                    attrs = attrs4
                    break
            }

            hi.merge(allattrs, attrs)
//            hi.debug()

            if (side == FightHero.左边) {
                frb.left.add(new SimpleHero(hi.heroStruct.hero, hi.maxLife, hi.anger, hi.heroStruct.skin, hi.heroStruct.grade))
//                if (hi.attack > 60_000_000) {
//                    logger.info("玩家(${uid})数据异常:\n" + hi.debug(0))
//                    if (lineups != null) {
//                        logger.info("lineups:${lineups}")
//                    }
//                    if (_buffs != null) {
//                        logger.info("buffs:${_buffs}")
//                    }
//                }
            } else {
                if (weak > 0) {
                    float w = 1.0f - weak / 100f;
                    hi.maxLife = (hi.maxLife * w) as long
                    hi.life = hi.maxLife
                    hi.attack = (hi.attack * w) as int
                    hi.defs = (hi.defs * w) as int
                    hi.mdef = (hi.mdef * w) as int
                }
                frb.right.add(new SimpleHero(hi.heroStruct.hero, hi.maxLife, hi.anger, hi.heroStruct.skin, hi.heroStruct.grade))
            }
        }

        if (side == FightHero.左边) {
            if (left.size() <= 0) {
                println("param left:" + _heroStructs.size())
                for (int ii = 0; ii < _heroStructs.size(); ++ii) {
                    HeroStruct hs = _heroStructs.get(ii)
                    if (hs != null) {
                        println(i + ":" + hs.toString())
                    }
                }
            } else if (left.size() > 6) {
                logger.error("玩家(${uid}模拟的部队数量大于六个限制.")
            }
        }
    }

    /**
     * 副本, 公会副本，战斗初始化, 副本英雄结构不同
     *
     * @param side
     * @param _heroStructs
     */
    @Override
    void buildTeamByStruct(int side, List<DirectHeroStruct> _heroStructs) {
        List<FightHero> his = null
        if (side == FightHero.左边) {
            his = left
        } else {
            his = right
        }

        int i = 0
        for (DirectHeroStruct hb : _heroStructs) {
            if (hb != null) {
                FightHero hi = new FightHero(this, side, i, hb)
                his.add(hi)
                orderList.add(hi)
                ++i
            }
        }

        // 遍历左边阵营，将全体与阵营提升合并进去
        for (FightHero hi : his) {
            hi.life = hi.maxLife
//            hi.debug()
            if (side == FightHero.左边) {
                frb.left.add(new SimpleHero(hi.heroStruct.hero, hi.maxLife, hi.anger, hi.heroStruct.skin, hi.heroStruct.grade))
            } else {
                frb.right.add(new SimpleHero(hi.heroStruct.hero, hi.maxLife, hi.anger, hi.heroStruct.skin, hi.heroStruct.grade))
            }
        }
    }

    /**
     * 战斗
     */
    @Override
    int run() {
        try {
            if (left.size() <= 0) {
                winResult = FightHero.右边
                println("己方没有部队，错误的攻击请求")
            } else if (right.size() <= 0) {
                winResult = FightHero.右边
                println("对方没有部队，错误的攻击请求")
            } else {
//                println("左边:" + left.size() + " 右边:" + right.size())
                HeroInfoComparator hc = new HeroInfoComparator()
                orderList.sort(hc)

                /**
                 * 遍历orderList表
                 */
                int i = 0
                winResult = 0 // 默认没有胜利方

                // 神器释放 左边
                for (int j = 0; j < leftArt.size(); ++j) {
                    int[] artatt = leftArt.get(j);
                    currentAction = new ActionRecord(0, FightHero.左边, -1)
                    currentAction.attackMode = 1
                    currentAction.skillMode = artatt[0]
                    doAction(FightHero.左边, artatt)
                    actions.addAll(currentAction)
                }

                // 神器释放 右边
                for (int j = 0; j < rightArt.size(); ++j) {
                    int[] artatt = rightArt.get(j);
                    currentAction = new ActionRecord(0, FightHero.右边, -1)
                    currentAction.attackMode = 1
                    currentAction.skillMode = artatt[0]
                    doAction(FightHero.右边, artatt)
                    actions.addAll(currentAction)
                }

                orderList.sort(hc)

                int count = orderList.size()
                for (; i < count; ++i) {
                    // 检查攻击方
                    FightHero hi = orderList.get(i)
                    if (!hi.isDead()) {
                        currentAction = new ActionRecord(round, hi.side, hi.index)

                        winResult = doAction(hi)
                        ++roundindex
//					logger.info(currentAction.debug(this, roundindex))
                        actions.add(currentAction)

                        if (winResult != 0) { // 战斗结束或者发生了错误
                            break
                        }
                    }

                    if (i == count - 1) {
                        i = -1
                        roundindex = 0
                        if (++round == maxRound) {
                            break
                        }
                        orderList.sort(hc)
                    }
                }

                if (winResult == 0) { // 表示平局，则防守方胜利
                    winResult = FightHero.右边
                }
            }

            if (winResult == FightHero.左边) {
                // 在进攻方胜利的时候，如果判定胜利条件不为0，则需要重新判定
                switch (condition) {
                    case 2: // 6个回合内战斗胜利
                        if (round >= 7) {
                            winResult = FightHero.右边
                        }
                        break
                    case 3: // 我方总血量高于50%
                        if (getLifeRatioBySide(FightHero.左边) < 0.5f) {
                            winResult = FightHero.右边
                        }
                        break
                    case 4: // 我方死亡人数不超过2人
                        if (getDeadsBySide(FightHero.左边) > 2) {
                            winResult = FightHero.右边
                        }
                        break
                    case 5: // 我方总血量高于70%
                        if (getLifeRatioBySide(FightHero.左边) < 0.7f) {
                            winResult = FightHero.右边
                        }
                        break
                    case 6: // 我方死亡人数不超过1人
                        if (getDeadsBySide(FightHero.左边) > 1) {
                            winResult = FightHero.右边
                        }
                        break
                    case 7: // 5个回合内战斗胜利
                        if (round >= 5) {
                            winResult = FightHero.右边
                        }
                        break
                    case 8: // 我方死亡人数不超过0人
                        if (getDeadsBySide(FightHero.左边) > 0) {
                            winResult = FightHero.右边
                        }
                        break
                    case 9: // 4个回合内战斗胜利
                        if (round >= 5) {
                            winResult = FightHero.右边
                        }
                        break
                    case 10: // 我方总血量高于80%
                        if (getLifeRatioBySide(FightHero.左边) < 0.8f) {
                            winResult = FightHero.右边
                        }
                        break
                }
            } else if (winResult == FightHero.右边) {

            } else {
                logger.error("***** 发生错误,中途停止 *****")
                winResult = -1
            }

            frb.win = winResult
            frb.actions = actions
        } catch (Exception ex) {
            logger.error("Simbattle.Run error:" + ex.getMessage() + " stack:" + ex.getStackTrace().toString())
            throw ex
        }

        return frb.win
    }

    /**
     * 当前生命百分比
     * @param side
     * @return
     */
    private float getLifeRatioBySide(int side) {
        List<FightHero> his = left
        if (side == FightHero.右边) {
            his = right
        }
        long now = 0
        long total = 0
        for (FightHero fh : his) {
            now += fh.life
            total += fh.maxLife
        }
        float r = ((now as float) / total) as float
//        println("now:" + now + " total:" + total + " r:" + r)
        return r
    }

    /**
     * 死亡人数
     * @param side
     * @return
     */
    private int getDeadsBySide(int side) {
        List<FightHero> his = left
        if (side == FightHero.右边) {
            his = right
        }
        long now = 0
        for (FightHero fh : his) {
            if (fh.isDead()) {
                ++now
            }
        }

        return now
    }


    /**
     * 检查当前英雄在本轮是否可以行动
     *
     * @param _hi
     * @return
     */
    private boolean checkRoundCanAct(FightHero _hi) {
        if (_hi.getForbidAction() > 0) {
            return false
        }
        return true
    }

    /**
     * 行动, 记得检查是不是可以行动
     *
     * @param _hi
     */
    private int doAction(FightHero _hi) {
        try {

            _hi.checkBuffsRoundBefore(round)

            if (!checkRoundCanAct(_hi)) {
//                logger.warn("+++ 英雄：" + _hi.name + " 被眩晕了，本回合:" + round + " 无法行动")
                _hi.checkBuffsRoundEnd(round)
                return 0
            }

            int heroId = _hi.heroStruct.hero
            if (Defs.isLordID(heroId) && _hi.heroStruct.skin != 0) {
                heroId = _hi.heroStruct.skin
            }

            HeroSkillConf hsb = Readonly.getInstance().findSkill(heroId)

            // 1. 检查当前怒气值，判断是否应该使用普攻
            if (_hi.anger < 4) {
                // 普通攻击
                _hi.anger += 2
                // 普通攻击多恢复2点怒气
                currentAction.attackMode = 1
                doHarm(_hi, hsb.attack1eff, _hi.fateSkillUp[0])
            } else {
                // 特殊攻击
                _hi.anger -= 4

                // 根据当前英雄可以释放的技能，顺序检测:
                // 超级组合技>组合技>怒气技
                if (_hi.isSuperGroupSkill()) {
                    currentAction.attackMode = 4
                    doHarm(_hi, hsb.attack4eff, _hi.fateSkillUp[3])
                } else if (_hi.isGroupSkill()) {
                    currentAction.attackMode = 3
                    doHarm(_hi, hsb.attack3eff, _hi.fateSkillUp[2])
                } else {
                    currentAction.attackMode = 2
                    doHarm(_hi, hsb.attack2eff, _hi.fateSkillUp[1])
                }
            }
        } catch (GameOverException goe) {
            return goe.getSide()
        } catch (Exception ex) {
            logger.error("英雄行动错误，当前所在回合:" + round + " 英雄:" + _hi.toString() + " 原因:", ex)
            return 3
        }

        _hi.checkBuffsRoundEnd(round)
        return 0
    }

    /**
     * 神器行动
     * @param _hi
     * @return
     */
    private int doAction(int side, int[] arts) {
        try {
            switch (arts[0]) {
                case 601:
                    List<FightHero> peers = findPeer(side, 0, FindPeerEnum.全体敌方)
                    if (peers != null) {
                        for (FightHero fh : peers) {
                            if (!fh.isDead()) {
                                if (RandomUtil.getBetween(1, 100) <= arts[1]) {
                                    fh.add龙(arts[2], arts[3], arts[4])
                                }
                            }
                        }
                    }
                    break
                case 801:
                    List<FightHero> peers = findPeer(side, 0, FindPeerEnum.全体己方)
                    if (peers != null) {
                        for (FightHero fh : peers) {
                            if (!fh.isDead()) {
                                fh.add神器1(arts[1])
                            }
                        }
                    }
                    break
                case 802:
                    List<FightHero> peers = findPeer(side, 0, FindPeerEnum.全体己方)
                    if (peers != null) {
                        for (FightHero fh : peers) {
                            if (!fh.isDead()) {
                                fh.add神器2(arts[1])
                            }
                        }
                    }
                    break
                case 803:
                    List<FightHero> peers = findPeer(side, 0, FindPeerEnum.全体己方)
                    if (peers != null) {
                        for (FightHero fh : peers) {
                            if (!fh.isDead()) {
                                fh.add神器3(arts[1])
                            }
                        }
                    }
                    break
            }

        } catch (GameOverException goe) {
            return goe.getSide()
        } catch (Exception ex) {
            logger.error("神器行动错误, 原因:", ex)
            return 3
        }

        return 0
    }


    /**
     * 检查 side 所在的队伍是否死光
     *
     * @param side
     * @return
     */
    boolean checkOver(int side) {
        if (side == FightHero.右边) {
            for (FightHero hi : right) {
                if (!hi.isDead()) {
                    return false
                }
            }
        } else {
            for (FightHero hi : left) {
                if (!hi.isDead()) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * 攻击效果应用到本次攻击上
     *
     * @param _hi
     * @param effs
     */
    private void doHarm(FightHero _hi, int[][] effs, int skillUp) throws GameOverException {
        List<FightHero> peers = null

        /**
         * 501,502,503 只临时提高本次攻击的暴击和命中，在本次doHarm结束后还原
         */
        int eh_crit = 0
        int eh_aim = 0

//        logger.info("+++回合[" + round + "]索引[" + roundindex + "] 英雄(" + _hi.name + "): 本次暴击提升(" + eh_crit + ") 命中提升(" + eh_aim + "). 基本攻击力:" + _hi.attack)

        currentAction.skillMode = effs[0][0]
        for (int[] eff : effs) {
            switch (eff[0]) {
                case 1:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.前列单体)
                    if (peers == null) {
                        logger.error("side:" + _hi.side + " index:" + _hi.index + " 前列单体没找到")
                        if (_hi.side == 2) {
                            String err = ""
                            if (left.size() == 0) {
                                err = "己方没有部队"
                            } else {
                                for (int ii = 0; ii < left.size(); ++ii) {
                                    FightHero fh = left.get(ii)
                                    if (fh != null) {
                                        err += fh.toString() + "\n"
                                    }
                                }
                            }
                            println(err)
                        }
                    } else {
                        for (FightHero peer : peers) {
                            doHarm(harm, _hi, peer)
                        }
                    }
                    break
                case 2:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体敌方)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 3:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.前列敌方)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 4:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.一行敌方)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 5:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机3个敌方)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 6:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个敌方)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 7:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机1个敌方)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 8:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.相邻敌方)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 9:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.后列敌方)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 10:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.后列单体)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 11:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.生命最少敌方)
                    for (FightHero peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 12:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.生命最少己方)
                    for (FightHero peer : peers) {
                        doCure(harm, _hi, peer, BuffInfo.治疗恢复)
                    }
                    break
                case 13:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方)
                    for (FightHero peer : peers) {
                        doCure(harm, _hi, peer, BuffInfo.群疗恢复)
                    }
                    break
            /* 下面一组的效果，根据上面一组获取的目标和伤害，是否闪避来处理 */

                case 101: // 概率减少目标怒气
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.anger -= eff[2]
                                currentAction.addEffectHarm(peer, BuffInfo.怒气控制, -eff[2] as long, false)
                            }
                        }
                    }
                    break
                case 102: // 概率眩晕
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.add眩晕(eff[2])
                            }
                        }
                    }
                    break
                case 103: // 概率灼烧
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                long harm = peer.tempHarm * eff[2] / 100
                                peer.add灼烧(eff[3], harm)
                            }
                        }
                    }
                    break
                case 104: // 概率中毒
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                long harm = peer.tempHarm * eff[2] / 100
                                peer.add中毒(eff[3], harm)
                            }
                        }
                    }
                    break
                case 105: // 概率降低防御
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.add降防(_hi.heroStruct.hero, eff[3], eff[2])
                            }
                        }
                    }
                    break
                case 106: // 概率降低攻击
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.add降攻(_hi.heroStruct.hero, eff[3], eff[2])
                            }
                        }
                    }
                    break
                case 107: // 概率易伤
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.add易伤(_hi.heroStruct.hero, eff[3], eff[2])
                            }
                        }
                    }
                    break
                case 108: // 本次攻击的目标如果是敌方生命值最低的单位，触发该效果
                    List<FightHero> heros = left
                    if (_hi.side == FightHero.左边) {
                        heros = right
                    }

                    // 1. 找到敌方所有生命值最低的
                    Optional<FightHero> opt = heros.stream().min({ a, b ->
                        try {
                            if (a.life <= 0) {
                                return 1
                            }

                            float al = a.life / a.maxLife
                            float bl = b.life / b.maxLife

                            if (al < bl) {
                                return -1
                            } else if (al > bl) {
                                return 1
                            }
                        } catch (Exception e) {
                            logger.error("Exception:" + e.getMessage())
                        }
                        return 0
                    } as Comparator<? super FightHero>)

                    FightHero minest = opt.orElse(null)
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer == minest) {
                            if (peer != null) { // 找到最小的生命值比例的人
                                long h = peer.tempHarm * eff[1] / 100
                                if (h > 0) {
                                    currentAction.addEffectHarm(peer, BuffInfo.生命比例最低攻击, -h, false)
                                    peer.fixLife(-h)
                                }
                            }
                        }
                    }
                    break
                case 109: // 清除debuff
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        peer.delBadBuff()
                    }
                    break
                case 110: // 清除buff
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        peer.delGoodBuff()
                    }
                    break
                case 111: //减伤Buff
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.add减伤(_hi.heroStruct.hero, eff[3], eff[2])
                            }
                        }
                    }
                    break
                case 112: //清除灼烧
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        peer.delAll灼烧()
                    }
                    break
                case 113: // 对生命低于百分比的友军额外治疗
                    for (FightHero h : peers) {
                        if (!h.isDead()) {
                            float ratio = h.life * 100f / h.maxLife
                            if (ratio < (float) eff[1]) {
                                long harm = (long) h.tempHarm * eff[2] / 100
                                currentAction.addEffectHarm(h, BuffInfo.治疗恢复, harm, false)
                                h.fixLife(harm)
                            }
                        }
                    }

                    break
                case 114: // 每回合恢复治疗量
                    for (FightHero peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            long harm = peer.tempHarm * eff[1] / 100
                            peer.add治疗(eff[2], harm)
                        }
                    }
                    break

            /* 下面的效果都是自带目标，需要重新定位目标，不要再使用攻击默认目标 */

            // 随机友军

                case 200:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机1个己方)
                        for (FightHero peer : peers) {
                            peer.anger += eff[2]
                            currentAction.addEffectHarm(peer, BuffInfo.怒气控制, eff[2] as long, false)
                        }
                    }
                    break
                case 201:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方)
                        for (FightHero peer : peers) {
                            peer.anger += eff[2]
                            currentAction.addEffectHarm(peer, BuffInfo.怒气控制, eff[2] as long, false)
                        }
                    }
                    break
                case 202:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方)
                        for (FightHero peer : peers) {
                            peer.add加伤(_hi.heroStruct.hero, eff[3], eff[2])
                        }
                    }
                    break
                case 203:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方)
                        for (FightHero peer : peers) {
                            peer.add难伤(_hi.heroStruct.hero, eff[3], eff[2])
                        }
                    }
                    break
                case 204: // 概率恢复自身怒气
                    if (getRandom() <= eff[1]) {
                        _hi.anger += eff[2]
                        currentAction.addEffectHarm(_hi, BuffInfo.怒气控制, eff[2] as long, false)
                    }
                    break
                case 205:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方)
                        for (FightHero peer : peers) {
                            peer.add加防(_hi.heroStruct.hero, eff[3], eff[2])
                        }
                    }
                    break
                case 206:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方)
                        for (FightHero peer : peers) {
                            peer.add加攻(_hi.heroStruct.hero, eff[3], eff[2])
                        }
                    }
                    break
                case 207: // 随机三个友军受到伤害降低，难伤Buff +3
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机3个己方)
                        for (FightHero peer : peers) {
                            peer.add难伤(_hi.heroStruct.hero, eff[3], eff[2])
                        }
                    }
                    break
            // 全体友军
                case 301: // 全体友军命中提高
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方)
                    for (FightHero peer : peers) {
                        peer.add命中提高(_hi.heroStruct.hero, eff[2], eff[1])
                    }
                    break
                case 302: // 全体友军抗暴提高
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方)
                    for (FightHero peer : peers) {
                        peer.add抗暴提高(_hi.heroStruct.hero, eff[2], eff[1])
                    }
                    break
                case 303: // 全体友军暴击,命中提高Buff
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方)
                    for (FightHero peer : peers) {
                        peer.add命中提高(_hi.heroStruct.hero, eff[2], eff[1])
                        peer.add暴击提高(_hi.heroStruct.hero, eff[2], eff[1])
                    }
                    break
                case 304: // 全体友军收到伤害减少，难伤Buff
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方)
                    for (FightHero peer : peers) {
                        peer.add难伤(_hi.heroStruct.hero, eff[2], eff[1])
                    }
                    break
                case 305: // 全体友军攻击提高
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方)
                    for (FightHero peer : peers) {
                        peer.add加攻(_hi.heroStruct.hero, eff[2], eff[1])
                    }
                    break
                case 306: // 全体友军攻击提高
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方)
                    for (FightHero peer : peers) {
                        peer.add闪避提高(_hi.heroStruct.hero, eff[2], eff[1])
                    }
                    break
                case 307: // 全体友军 造成伤害提高，加伤Buff
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方)
                    for (FightHero peer : peers) {
                        peer.add加伤(_hi.heroStruct.hero, eff[2], eff[1])
                    }
                    break
                case 401:
                    _hi.add无敌(eff[1])
                    break
            // 自身相关
                case 402: // 自身闪避提高
                    _hi.add闪避提高(_hi.heroStruct.hero, eff[2], eff[1])
                    break
                case 403: // 自身伤害提高
                    _hi.add加伤(_hi.heroStruct.hero, eff[2], eff[1])
                    break
                case 404: // 自身受到伤害减少
                    _hi.add难伤(_hi.heroStruct.hero, eff[2], eff[1])
                    break
                case 501: // 暴击提高Buff
                    eh_crit += eff[1]
                    _hi.critical += eff[1]
                    break
                case 502: // 命中提高Buff
                    eh_aim += eff[1]
                    _hi.aim += eff[1]
                    break
                case 503: // 暴击命中提高
                    eh_crit += eff[1]
                    eh_aim += eff[1]

                    _hi.critical += eff[1]
                    _hi.aim += eff[1]
                    break
                default:
                    logger.error("xxxxxx doHarm中的攻击效果:" + eff[0] + " 不认识，请查看是否更新正确的类型.")
            }
        }

        _hi.critical += eh_crit
        _hi.aim += eh_aim
    }

    /**
     * 根据技能的基础伤害，攻防两方的人物属性，计算本次攻击的伤害
     *
     * @param baseHarm 技能的基础伤害
     * @param self 攻击方
     * @param peer 防御方
     * @return 返回攻击具体伤害值，这个值是可以将对方打成负数的值，客户端自行修正实际扣减
     * 注意返回负数，表示本次攻击被闪避
     */
    private void doHarm(long baseHarm, FightHero self, FightHero peer) throws GameOverException {
        if (self.life <= 0 || peer.life <= 0) {
//			logger.error("玩家:" + self.heroStruct.hero + "已经死亡，又进入了操作.")
            return
        }

        String msg = "|--- 攻击方:" + self.name + " 被攻击:" + peer.name + " 基础攻击力:" + baseHarm

        boolean crit = false
        boolean isdodge = false

        msg += " 命中:" + self.aim + " 对方闪避:" + peer.dodge
        // 1. 计算闪避
        int dodge = (self.aim - peer.dodge)
        if (dodge < 0) { // 闪避生效，进行计算
            int dr = getRandom1000()
            msg += " random1000:" + dr
            if (dr <= -dodge) {
                isdodge = true
                msg += " 闪避成功,r(" + dr + ")aim(" + self.aim + ") - dodge(" + peer.dodge + ")=" + dodge + " "
            }
        }

        long h = baseHarm
        if (!isdodge) {
            // 2. 计算伤害
            // 根据攻击方的模式，选择被攻击方的防御属性

            // 2.1 概率忽视对方防御
            boolean nodef = false
            if (self.forceAttack > 0) {
                if (getRandom() <= self.forceAttack) {
                    nodef = true
                    msg += " 忽视防御 "
                }
            }
            if (!nodef) {
                switch (self.mode) {
                    case 1:
                    case 3: // 物理防御起作用
                        h -= peer.defs
                        if (h < 0) {
                            h = 0
                        }
                        msg += " 对方防御(" + peer.defs + ") 攻击力(" + h + ") "
                        break
                    case 2:
                    case 4: // 魔法防御起作用
                        h -= peer.mdef
                        if (h < 0) {
                            h = 0
                        }
                        msg += " 对方防御(" + peer.mdef + ") 攻击力(" + h + ") "
                        break
                }
            }

            // 加入战斗数值扰动因素
            int r = getRandom()
            h = h * (r % 10 + 95) / 100

            /**
             * er = (增伤 - 减伤Buff + 易伤Buff - 难伤Buff)
             **/
            float er = (self.enHarm + peer.getOverHarm())

            /**
             * h = h * (1 + er/(1000 + |er|)
             */

            // 针对不同的攻击类型 减少伤害
            float decrease = 0;
            if (self.mode == FightHero.物理攻击 || self.mode == FightHero.防御攻击) {
                if (peer.phyLower > 0) {
                    decrease = peer.phyLower / 1000f;
                    if (decrease > 1) {
                        decrease = 1
                    }
                }
            } else {
                if (peer.magLower > 0) {
                    decrease = peer.magLower / 1000f;
                    if (decrease > 1) {
                        decrease = 1
                    }
                }
            }

            // 还的判断 self 对 peer阵营是否有加成
            h = (long) (h * (1f + er / (Math.abs(er) + 1000f)) * (self.sectHarmUp[peer.sect - 1] + peer.harmUp - decrease))

            msg += " 难伤易伤后攻击力(" + h + ")er(" + er + ") "

            // 3. 计算暴击率
            int critical = self.critical - peer.antiCrit
            if (critical > 0) {
                if (getRandom1000() <= critical) {
                    // 4. 计算暴击伤害
                    h = (int) ((150 + self.critHarm) * h / 100f)
                    crit = true
                    msg += " 暴击(" + h + ") "
                }
            }

            // 5. 增加伤害与易伤系数, 检测各种buff与debuff
            for (BuffInfo buff : self.buffs) {
                h = buff.whenAttack(h)
            }

            for (BuffInfo buff : peer.buffs) {
                h = buff.whenBeAttack(h)
            }

            if (self.multiHarmRatio > 0) {
                if (getRandom() <= self.multiHarmRatio) {
                    h *= 2
                    msg += " [双倍伤害] "
                }
            }

            if (h <= 0) {
                h = 1
                msg += " [超低伤害变成1] "
            }
        } else {
            h = 0
        }
        peer.tempHarm = h

        msg += " 本次攻击伤害[" + h + "]"
        currentAction.addEffectHarm(peer, BuffInfo.攻击伤害, -h, crit)
        peer.fixLife(-h)

        if (!peer.isDead() && !isdodge) {
            if (peer.drawRatio > 0) {
                int r = getRandom()
                msg += " [对方吸血概率](" + peer.drawLife + ")(" + peer.drawRatio + ") "
                if (r <= peer.drawRatio) {
                    long hh = (peer.drawLife * peer.maxLife / 100)
                    if (hh > 0) {
                        currentAction.addEffectHarm(peer, BuffInfo.治疗恢复, hh, false)
                        peer.fixLife(hh)
                        msg += " [对方吸血](" + hh + ") "
                    }
                }
            }

            if (self.mode == FightHero.物理攻击 || self.mode == FightHero.防御攻击) {
                if (peer.reflectRatio > 0) {
                    int r = getRandom()
                    if (r < peer.reflectRatio) {
                        long hh = h * peer.reflectPercent / 100
                        if (hh > 0) {
                            currentAction.addEffectHarm(self, BuffInfo.攻击伤害, -hh, false)
                            self.fixLife(-hh)
                            msg += " [对方反伤](" + hh + ") "
                        }
                    }
                }
            } else {
                if (peer.mReflectRatio > 0) {
                    int r = getRandom()
                    if (r < peer.mReflectRatio) {
                        long hh = h * peer.mReflectPercent / 100
                        if (hh > 0) {
                            currentAction.addEffectHarm(self, BuffInfo.攻击伤害, -hh, false)
                            self.fixLife(-hh)
                            msg += " [对方反伤](" + hh + ") "
                        }
                    }
                }
            }
        }
//        println(msg)
    }

    /**
     * 计算恢复的值
     *
     * @param baseHarm
     * @param self
     * @param peer
     * @return
     */
    private void doCure(long baseHarm, FightHero self, FightHero peer, int mode) throws GameOverException {
        // 1. 计算伤害
        // 根据攻击方的模式，选择被攻击方的防御属性
        long h = baseHarm

        h = h * (getRandom() % 10 + 95) / 100

        // 3. 计算暴击率
        int critical = self.critical - peer.antiCrit
        boolean crit = false
        if (critical > 0) {
            if (getRandom1000() <= critical) {
                crit = true
                // 4. 计算暴击伤害
                h = (150 + self.critHarm) * h / 100
            }
        }

        if (self.multiHarmRatio > 0) {
            if (getRandom() <= self.multiHarmRatio) {
                h *= 2
            }
        }
        peer.tempHarm = h

        currentAction.addEffectHarm(peer, mode, h, crit)

        peer.fixLife(h)
    }

    /**
     * 行动前准备
     *
     * @param _hi
     */
    private void preAction(FightHero _hi) {

    }

    /**
     * 行动结束后的结果
     *
     * @param _hi
     */
    private void postAction(FightHero _hi) {

    }

    /**
     * 根据当前阵营，站位，和攻击目标的模式，得到所有目标
     *
     * @param side 阵营 1左边 2右边
     * @param index 站位 [0,5]
     * @param fpe 攻击目标的模式
     * @return 最后需要攻击的目标列表
     * <p>
     * 如果返回@null 表示当前所在的side胜利，对方已经没有可以攻击的对象了
     */
    List<FightHero> findPeer(int side, int index, FindPeerEnum fpe) {
        List<FightHero> self = null
        List<FightHero> other = null
        if (side == 1) {
            other = right
            self = left
        } else {
            other = left
            self = right
        }

        switch (fpe) {
            case FindPeerEnum.前列单体:
                FightHero hi = findOne(other, index)
                if (hi != null) {
                    List<FightHero> rtn = new ArrayList<>()
                    rtn.add(hi)
                    return rtn
                }
                return null
            case FindPeerEnum.后列单体: // 优先后列
                FightHero hi = findOneReverse(other, index)
                if (hi != null) {
                    List<FightHero> rtn = new ArrayList<>()
                    rtn.add(hi)
                    return rtn
                }
                return null
            case FindPeerEnum.全体敌方:
                List<FightHero> rtn = new ArrayList<>()
                for (FightHero hi : other) {
                    if (!hi.isDead()) {
                        rtn.add(hi)
                    }
                }
                if (rtn.size() <= 0) {
                    return null
                }
                return rtn
            case FindPeerEnum.前列敌方:
                return findByCol(other, 前列)
            case FindPeerEnum.后列敌方:
                return findByCol(other, 后列)
            case FindPeerEnum.一行敌方:
                return findByRow(other, index)
            case FindPeerEnum.相邻敌方:
                return findAdjacent(other, index)
            case FindPeerEnum.生命最少敌方:
                return findMinLife(other)
            case FindPeerEnum.随机1个敌方:
                return findRandomPeer(other, 1)
            case FindPeerEnum.随机2个敌方:
                return findRandomPeer(other, 2)
            case FindPeerEnum.随机3个敌方:
                return findRandomPeer(other, 3)
            case FindPeerEnum.随机1个己方:
                return findRandomPeer(self, 1)
            case FindPeerEnum.随机2个己方:
                return findRandomPeer(self, 2)
            case FindPeerEnum.随机3个己方:
                return findRandomPeer(self, 3)
            case FindPeerEnum.生命最少己方:
                return findMinLife(self)
            case FindPeerEnum.全体己方:
                List<FightHero> rtn = new ArrayList<>()
                for (FightHero hi : self) {
                    if (!hi.isDead()) {
                        rtn.add(hi)
                    }
                }
                if (rtn.size() <= 0) {
                    return null
                }
                return rtn
            case FindPeerEnum.单体己方:
                List<FightHero> rtn = new ArrayList<>()
                rtn.add(self.get(index))
                return rtn
            default:
                logger.error("findPeer 查找对手的方式无法识别: " + fpe.toString())
        }

        return null
    }

    /**
     * 找到一个敌方,优先前列
     * <p>
     * 先找对面的第一列，如果死亡，则查询第一列，以1，2，3的顺序攻击
     * 第二列，也是先找正对面的，如果死亡，则查询第二列，以 4、5、6的顺序攻击
     *
     * @return
     */
    private FightHero findOne(List<FightHero> other, int index) {
        int idx = getFirstColumn(index)
        if (idx < other.size()) {
            FightHero hi = other.get(idx)
            if (!hi.isDead()) {
                return hi
            }
        }

        // 在第一列，找可以攻击的目标
        for (int i = 0; i < 3; ++i) {
            if (i != idx && i < other.size()) {
                FightHero hi = other.get(i)
                if (!hi.isDead()) {
                    return hi
                }
            }
        }

        idx = getSecondColumn(index)

        if (idx < other.size()) {
            FightHero hi = other.get(idx)
            if (!hi.isDead()) {
                return hi
            }
        }

        // 在第一列，找可以攻击的目标
        for (int i = 3; i < 6; ++i) {
            if (i != idx && i < other.size()) {
                FightHero hi = other.get(i)
                if (!hi.isDead()) {
                    return hi
                }
            }
        }

        // we count not found the peer in others, so print the other status
        return null
    }

    /**
     * 优先后列，找到一个对手
     *
     * @param other
     * @param index
     * @return
     */
    private FightHero findOneReverse(List<FightHero> other, int index) {
        int idx = getSecondColumn(index)
        if (idx < other.size()) {
            FightHero hi = other.get(idx)
            if (!hi.isDead()) {
                return hi
            }
        }

        // 在后列，找可以攻击的目标
        for (int i = 3; i < 6; ++i) {
            if (i != idx && i < other.size()) {
                FightHero hi = other.get(i)
                if (!hi.isDead()) {
                    return hi
                }
            }
        }

        idx = getFirstColumn(index)
        if (idx < other.size()) {
            FightHero hi = other.get(idx)
            if (!hi.isDead()) {
                return hi
            }
        }

        // 在前列，找可以攻击的目标
        for (int i = 0; i < 3; ++i) {
            if (i != idx && i < other.size()) {
                FightHero hi = other.get(i)
                if (!hi.isDead()) {
                    return hi
                }
            }
        }

        // 此时战斗已经结束，对方没有可以攻击的目标
        return null
    }

    /**
     * 找到对方血量最少的人
     *
     * @param other
     * @return
     */
    private List<FightHero> findMinLife(List<FightHero> other) {
        long min = 99999999999L
        FightHero rtn = null
        for (FightHero hi : other) {
            if (!hi.isDead()) {
                if (hi.life < min) {
                    rtn = hi
                    min = hi.life
                }
            }
        }

        if (rtn == null) {
            return null
        }

        List<FightHero> tmp = new ArrayList<>()
        tmp.add(rtn)
        return tmp
    }

    /**
     * 从列表中随机选取number个元素,
     * 如果列表长度不足，直接返回列表
     *
     * @param other
     * @param number
     * @return
     */
    private List<FightHero> findRandomPeer(List<FightHero> other, int number) {
        List<FightHero> rtn = other.parallelStream().filter({ t -> !t.isDead() }).collect(Collectors.toList())
        if (rtn.size() > number) {
            Collections.shuffle(rtn)
            return rtn.subList(0, number)
        }
        return rtn
    }

    /**
     * 根据列来查找
     *
     * @return
     */
    private List<FightHero> findByCol(List<FightHero> other, int 优先) {
        List<FightHero> rtn = new ArrayList<>()
        if (优先 == 前列) {
            if (findFirstCol(other, rtn)) {
                return rtn
            }
            if (findSecondCol(other, rtn)) {
                return rtn
            }
        } else {
            if (findSecondCol(other, rtn)) {
                return rtn
            }
            if (findFirstCol(other, rtn)) {
                return rtn
            }
        }

        return null
    }

    private boolean findFirstCol(List<FightHero> other, List<FightHero> rtn) {
        boolean flag = false
        int length = Math.min(3, other.size())
        for (int i = 0; i < length; ++i) {
            FightHero hi = other.get(i)
            if (hi != null && !hi.isDead()) {
                rtn.add(hi)
                flag = true
            }
        }
        return flag
    }

    private boolean findSecondCol(List<FightHero> other, List<FightHero> rtn) {
        boolean flag = false
        if (other.size() > 3) {
            int length = Math.min(6, other.size())
            for (int i = 3; i < length; ++i) {
                FightHero hi = other.get(i)
                if (hi != null && !hi.isDead()) {
                    rtn.add(hi)
                    flag = true
                }
            }
        }
        return flag
    }

    /**
     * 找对对位的敌人与其相邻的敌人:
     * <p>
     * 先找到一个可攻击对象，然后找到他的邻居
     *
     * @param other
     * @param index
     * @return
     */
    private List<FightHero> findAdjacent(List<FightHero> other, int index) {
        List<FightHero> rtn = new ArrayList<>()
        FightHero hi = findOne(other, index)
        if (hi == null) {
            return null
        }
        int first = hi.index

        rtn.add(hi)
        int idx = first - 1
        if (idx >= 0 && idx != 2 && idx < other.size()) {
            hi = other.get(idx)
            if (!hi.isDead()) {
                rtn.add(hi)
            }
        }

        idx = first + 1
//		logger.info("搜寻下面位置的人, 搜索到的对方英雄:" + hi.index + " 名字:" + getHeroName(FightHero.右边, hi.index))
        if (idx <= 5 && idx != 3 && idx < other.size()) {
//			logger.info("可以搜寻下面位置的人, idx:" + idx)
            hi = other.get(idx)
            if (!hi.isDead()) {
                rtn.add(hi)
            }
        }

        if (first < 3) {
            idx = first + 3
            if (idx < other.size()) {
                hi = other.get(idx)
                if (!hi.isDead()) {
                    rtn.add(hi)
                }
            }
        } else {
            idx = first - 3
            if (idx < other.size()) {
                hi = other.get(idx)
                if (!hi.isDead()) {
                    rtn.add(hi)
                }
            }
        }

        return rtn
    }

    /**
     * 一行行的找, 优先找本行，其次从0到3
     *
     * @param other
     * @param index 自己站位索引
     * @return
     */
    private List<FightHero> findByRow(List<FightHero> other, int index) {
        List<FightHero> rtn = new ArrayList<>()

        int idx = index
        if (index >= 3) {
            idx = index - 3
        }

        // 查找本行
        boolean flag = false
        if (idx < other.size()) {
            FightHero hi = other.get(idx)
            if (!hi.isDead()) {
                rtn.add(hi)
                flag = true
            }
        }

        if (flag) {
            if (idx + 3 < other.size()) {
                FightHero hi = other.get(idx + 3)
                if (!hi.isDead()) {
                    rtn.add(hi)
                }
            }
            return rtn
        }

        // 再从第一横排找起,跳过刚才的那一横排
        for (int i = 0; i < 3; ++i) {
            if (i != idx) {
                flag = false
                if (i < other.size()) {
                    FightHero hi = other.get(i)
                    if (!hi.isDead()) {
                        rtn.add(hi)
                        flag = true
                    }
                }

                if (flag) {
                    if (i + 3 < other.size()) {
                        FightHero hi = other.get(i + 3)
                        if (!hi.isDead()) {
                            rtn.add(hi)
                        }
                    }
                    return rtn
                }
            }
        }

        // 还是没找到 则又从原始第一横排找起
        if (idx + 3 < other.size()) {
            FightHero hi = other.get(idx + 3)
            if (!hi.isDead()) {
                rtn.add(hi)
                return rtn
            }
        }

        for (int i = 0; i < 3; ++i) {
            if (i != idx) {
                if (i + 3 < other.size()) {
                    FightHero hi = other.get(i + 3)
                    if (!hi.isDead()) {
                        rtn.add(hi)
                        return rtn
                    }
                }
            }
        }

        return rtn
    }

    /**
     * 通过当前的位置，找到第一列的对位者
     *
     * @param index
     * @return
     */
    private int getFirstColumn(int index) {
        if (index >= 3) {
            return (index - 3)
        }
        return index
    }

    /**
     * 通过当前的位置，找到第二列的对位者
     *
     * @param index
     * @return
     */
    private int getSecondColumn(int index) {
        if (index < 3) {
            return (index + 3)
        }
        return index
    }

    int getRound() {
        return round
    }

    void setRound(int round) {
        this.round = round
    }

    int getMaxRound() {
        return maxRound
    }

    void setMaxRound(int maxRound) {
        this.maxRound = maxRound
    }

    ActionRecord getCurrent() {
        return currentAction
    }

    /**
     * 返回战斗录像
     *
     * @return
     */
    String getFightData() {
        return JsonUtil.Marshal(frb)
    }

    /**
     * 获取剩余血量
     *
     * @return
     */
    List<Long> getRightLifes() {
        List<Long> lifes = new ArrayList<>()
        for (FightHero hi : right) {
            if (hi != null) {
                lifes.add(hi.life)
            }
        }
        return lifes
    }
}

/**
 * 战斗中的英雄信息
 */
class FightHero {
    /**
     * 英雄攻击类型的四种分类
     */
    static final int 物理攻击 = 1
    static final int 法术攻击 = 2
    static final int 防御攻击 = 3
    static final int 辅助攻击 = 4

    static final int 左边 = 1
    static final int 右边 = 2

    /**
     * 战局
     */
    private GroovyFightSystem battle

    /**
     * 具体信息
     */
    HeroStruct heroStruct

    String name = ""

    /**
     * 左边 1
     * 右边 2
     */
    int side = 左边
    /**
     * 站位序号
     */
    int index
    /**
     * 英雄的攻击模式
     */
    int mode
    /**
     * 英雄阵营
     */
    int sect
    /**
     * 生命上限
     */
    long maxLife
    /**
     * 生命
     */
    long life
    /**
     * 攻击力
     */
    int attack
    /**
     * 物理防御
     */
    int defs
    /**
     * 魔法防御
     */
    int mdef
    /**
     * 暴击率 /1000
     */
    float critical
    /**
     * 抗暴击率 /1000
     */
    float antiCrit
    /**
     * 闪避率 /1000
     */
    float dodge
    /**
     * 命中率 /1000
     */
    float aim
    /**
     * 暴击伤害
     */
    float critHarm
    /**
     * 当前初始怒气
     */
    int anger = 1
    /**
     * 每个回合恢复的怒气
     */
    int angerCover
    /**
     * 概率忽视防御 /100
     */
    int forceAttack
    /**
     * 高倍伤害机率 /100
     */
    int multiHarmRatio

    /**
     * 易伤比例 /1000
     *
     * 难伤是负数，负数越多越难伤
     * 易伤是正数，
     */
    float overHarm

    /**
     * 增伤比例 /1000
     */
    float enHarm

    /**
     * 不能操作的索引，比如眩晕之后，这个索引+1，回合内不能操作，冰冻之后再+1，
     * 当buff消失后，索引-1，只有等于0的时候，本回合不受到buff的影响，可以操作
     */
    private int forbidAction

    /**
     * 组合技是否激活
     */
    private boolean groupSkill

    /**
     * 超级组合技是否激活
     */
    private boolean superGroupSkill

    /**
     * 天命激活的技能提升
     */
    int[] fateSkillUp = [0, 0, 0, 0]

    /**
     * 对特殊阵营的英雄提升伤害
     */
    float[] sectHarmUp = [1f, 1f, 1f, 1f]

    /**
     * 龙buff导致的，自身被攻击的时候要增加的伤害，是一个debuff字段
     */
    float harmUp = 0

    /**
     * 速度
     */
    int speed = 0

    /**
     * 物理反伤机率
     */
    int reflectRatio
    /**
     * 物理反伤比例
     */
    int reflectPercent

    /**
     * 魔法反伤机率
     */
    int mReflectRatio
    /**
     * 魔法反伤比例
     */
    int mReflectPercent

    /**
     * 物理伤害降低率
     */
    int phyLower

    /**
     * 魔法伤害降低率
     */
    int magLower

    /**
     * 攻击回血
     */
    int drawLife
    /**
     * 攻击回血机率
     */
    int drawRatio

    /**
     * 当前存在的buff与debuff
     */
    List<BuffInfo> buffs = new ArrayList<>()

    /**
     *  临时变量
     */
    /**
     * 临时伤害，被攻击后，可能附带dot灼烧，中毒等伤害，这个值记录了攻击的伤害，下次计算灼烧的时候，以
     * 此临时伤害进行计算百分比.
     */
    long tempHarm = 0

    /**
     * 暂存的个人增加百分比，
     * 需要等待全体英雄计算完毕，得到了全体提升和阵营提升之后，再合并到这里来.
     */
    private HashMap<Integer, Float> aloneattrs = new HashMap<Integer, Float>()

    /**
     * 怪物 副本英雄构建
     *
     * @param _side
     * @param _index
     * @param _hero
     * @param _battle
     */
    FightHero(GroovyFightSystem _battle, int _side, int _index, DirectHeroStruct _hero) {
        aloneattrs = new HashMap<Integer, Float>()
        heroStruct = new HeroStruct()
        heroStruct.hero = _hero.hero
        index = _index
        side = _side
        battle = _battle

        HeroConf hib = Readonly.getInstance().findHero(heroStruct.hero)
        mode = hib.type
        sect = hib.sect
        name = hib.name

        life = _hero.life
        maxLife = _hero.life
        attack = _hero.attack
        defs = _hero.def
        mdef = _hero.mdef
        critical = _hero.critical
        antiCrit = _hero.antiCrit
        dodge = _hero.dodge
        aim = _hero.aim
        overHarm = -_hero.overHarm
        enHarm = _hero.enHarm
        anger = 4

        HeroSkillConf hsb = Readonly.getInstance().findSkill(heroStruct.hero)
        if (hsb != null) {
            // 3.1 组合技
            if (hsb.attack3eff.length > 0) { // 首先判断是否存在组合技，然后再判断是否激活
                groupSkill = true
                for (int c : hsb.cost) {
                    if (battle.getTeam(side).stream().noneMatch({ info -> (info.heroStruct.hero == c) })) {
                        groupSkill = false
                        break
                    }
                }
            }

            // 3.1.1 检测超级组合技
            if (heroStruct.grade >= 10 && hsb.attack4eff.length > 0) {
                if (groupSkill) {
                    superGroupSkill = true
                }
            }
        }
    }

    /***
     * 玩家英雄战斗单元
     *
     * @param _side
     * @param _index
     * @param _heroStruct
     * @param _battle
     * @param allattrs
     * @param sectattrs
     */
    FightHero(GroovyFightSystem _battle, int _side, int _index, HeroStruct _heroStruct, Map<Integer, Float> allattrs, Map<Integer, Float> sectattrs, int lineup) {
        aloneattrs = new HashMap<Integer, Float>()
        heroStruct = _heroStruct
        index = _index
        side = _side
        battle = _battle

        /*
        * 根据英雄当前配置进行初始化战斗属性
        * */

        // 有些特殊属性进行暂存，待加法运算结束后，进行乘法运算

        // 1. 英雄自身相关属性
        HeroConf hib = Readonly.getInstance().findHero(heroStruct.hero)

        // 阵营与英雄攻击模式
        sect = hib.sect
        mode = hib.type
        name = hib.name

        int[] gf = hib.growfight
        maxLife += gf[0]
        attack += gf[1]
        defs += gf[2]
        mdef += gf[3]

        // 1.1 根据当前突破等级，得到等级提升的效果
        int[] af = hib.attup[heroStruct.grade]
        int level = heroStruct.level - 1

        maxLife += level * af[0]
        attack += level * af[1]
        defs += level * af[2]
        mdef += level * af[3]

        if (lineup != 0) {
            LineupConf lc = Readonly.getInstance().findLineup(index + 1)
            if (lc != null) {
                preDealAttrs(lc.lineatt[0], lc.lineatt[1] * lineup, allattrs, sectattrs)
            }
        }

        if (heroStruct.skin != 0) {
            MaskConf mc = Readonly.getInstance().findMask(heroStruct.skin)
            // 从皮肤id切到对应模拟的英雄id
            heroStruct.skin = mc.hero

            // 赋值皮肤属性
            for (int i = 0; i < mc.basic.length; i += 2) {
                preDealAttrs(mc.basic[i], mc.basic[i + 1], allattrs, sectattrs)
            }

            int skinlvl = heroStruct.skinLevel - 1
            attack += mc.stratt[0] * skinlvl
            maxLife += mc.stratt[1] * skinlvl
            defs += mc.stratt[2] * skinlvl
            mdef += mc.stratt[3] * skinlvl

            for (int[] bs : mc.bonus) {
                if (heroStruct.skinLevel >= bs[0]) {
                    preDealAttrs(bs[1], bs[2], allattrs, sectattrs)
                }
            }
        } else {
            if (heroStruct.gift >= 26) {
                heroStruct.skin = 1
            }
        }

        // 1.2 觉醒英雄需要增加新的提升
        if (heroStruct.gift > 0) {
            AwakenConf lb = null
            if (heroStruct.gift > 1) {
                lb = Readonly.getInstance().findAwaken(heroStruct.gift - 1)
            }
            AwakenConf ab = Readonly.getInstance().findAwaken(heroStruct.gift)

            /**
             * 说明: gift觉醒默认是1级，只在对应的四个符文位置被点亮的时候，对应的提升才会被计算在内
             * 否则计算他的gift-1的数值，而gift==0不存在，所有对应不能觉醒的英雄虽然觉醒等级为1，实际还是没有提升。
             *
             * levelup的增加则是直接计算对应本级的数值
             *
             */
            int[] fwcost1 = null
            int[] fwcost2 = null
            if (ab.fwcost1.length == 0) {
                fwcost1 = new int[4]
            } else {
                fwcost1 = ab.fwcost1[1]
            }
            if (ab.fwcost2.length == 0) {
                fwcost2 = new int[4]
            } else {
                fwcost2 = ab.fwcost2[1]
            }

            // 根据符文点亮的索引获取对应的加成
            if (heroStruct.giftrunes[0] != 0) { // 攻击, 本级激活
                if (mode == 物理攻击 || mode == 法术攻击) {
                    attack += level * (fwcost1[0] + ab.levelup1[0])
                } else {
                    attack += level * (fwcost2[0] + ab.levelup2[0])
                }
            } else { // 本级未激活
                if (mode == 物理攻击 || mode == 法术攻击) {
                    attack += level * (((lb == null) ? 0 : lb.fwcost1[1][0]) + ab.levelup1[0])
                } else {
                    attack += level * (((lb == null) ? 0 : lb.fwcost2[1][0]) + ab.levelup2[0])
                }
            }

            if (heroStruct.giftrunes[1] != 0) { // 血量, 本级激活
                if (mode == 物理攻击 || mode == 法术攻击) {
                    maxLife += level * (fwcost1[1] + ab.levelup1[1])
                } else {
                    maxLife += level * (fwcost2[1] + ab.levelup2[1])
                }
            } else { // 本级未激活
                if (mode == 物理攻击 || mode == 法术攻击) {
                    maxLife += level * (((lb == null) ? 0 : lb.fwcost1[1][1]) + ab.levelup1[1])
                } else {
                    maxLife += level * (((lb == null) ? 0 : lb.fwcost2[1][1]) + ab.levelup2[1])
                }
            }

            if (heroStruct.giftrunes[2] != 0) { // 物理防御, 本级激活
                if (mode == 物理攻击 || mode == 法术攻击) {
                    defs += level * (fwcost1[2] + ab.levelup1[2])
                } else {
                    defs += level * (fwcost2[2] + ab.levelup2[2])
                }
            } else { // 本级未激活
                if (mode == 物理攻击 || mode == 法术攻击) {
                    defs += level * (((lb == null) ? 0 : lb.fwcost1[1][2]) + ab.levelup1[2])
                } else {
                    defs += level * (((lb == null) ? 0 : lb.fwcost2[1][2]) + ab.levelup2[2])
                }
            }

            if (heroStruct.giftrunes[3] != 0) { // 魔法防御, 本级激活
                if (mode == 物理攻击 || mode == 法术攻击) {
                    mdef += level * (fwcost1[3] + ab.levelup1[3])
                } else {
                    mdef += level * (fwcost2[3] + ab.levelup2[3])
                }
            } else { // 本级未激活
                if (mode == 物理攻击 || mode == 法术攻击) {
                    mdef += level * (((lb == null) ? 0 : lb.fwcost1[1][3]) + ab.levelup1[3])
                } else {
                    mdef += level * (((lb == null) ? 0 : lb.fwcost2[1][3]) + ab.levelup2[3])
                }
            }
        }

        // 1.3 境界等级
        StateupConf sub = Readonly.getInstance().findStateup(heroStruct.fate)
        if (sub != null && sub.bonusatt != null) {
            for (int i = 0; i < sub.bonusatt.length; i += 2) {
                int key = sub.bonusatt[i]
                int val = sub.bonusatt[i + 1]
                preDealAttrs(key, val, allattrs, sectattrs)
            }
        }

        // 记录套装对应的数量，key: 套装第一个装备的id, val:套装对应共有几个配件满足
        HashMap<Integer, List<Integer>> suit = new HashMap<>()

        // 记录4种共鸣级别
        // 依次为 装备的强化，精炼，宝物的强化，精炼 最小等级
        int[] resonance = [-1, -1, -1, -1]
        // 装备个数
        int eCount = 0
        // 宝物个数
        int tCount = 0

        // 2. 装备
        for (EquipStruct eb : heroStruct.equipStructs) {
            if (eb == null) {
                continue
            }

            EquipConf ebinfo = Readonly.getInstance().findEquip(eb.id)

            for (int jj = 0; jj < ebinfo.base.length; jj += 2) {
                // 2.1 基础属性
                preDealAttrs(ebinfo.base[jj], ebinfo.base[jj + 1], allattrs, sectattrs)
            }

            for (int jj = 0; jj < ebinfo.str.length; jj += 2) {
                // 2.2 强化属性
                preDealAttrs(ebinfo.str[jj], ebinfo.str[jj + 1] * (eb.level - 1), allattrs, sectattrs)
            }

            for (int jj = 0; jj < ebinfo.refine.length; jj += 2) {
                // 2.3 精炼属性
                preDealAttrs(ebinfo.refine[jj], ebinfo.refine[jj + 1] * eb.grade, allattrs, sectattrs)
            }

            // 2.4 精炼奖励属性
            for (int[] bonus : ebinfo.bonus) {
                if (eb.grade >= bonus[0]) {
                    preDealAttrs(bonus[1], bonus[2], allattrs, sectattrs)
                }
            }

            // 新增觉醒属性
            if (eb.wake > 0) {
                int[] wi = ebinfo.wakenadd[eb.wake - 1]
                for (int jj = 0; jj < wi.length; jj += 2) {
                    preDealAttrs(wi[jj], wi[jj + 1], allattrs, sectattrs)
                }
            }

            // 2.5 套装检测
            if (ebinfo.suit.length > 0) {
                List<Integer> list = suit.getOrDefault(ebinfo.suit[0], null)
                if (list == null) {
                    list = new ArrayList<>()
                    suit.put(ebinfo.suit[0], list)
                }

                if (!list.contains(ebinfo.key)) {
                    list.add(ebinfo.key)
                }
            }

            // 检测共鸣
            if (eb.id < 20000) { // 装备
                ++eCount
                if (resonance[0] == -1) {
                    resonance[0] = eb.level
                } else if (eb.level < resonance[0]) {
                    resonance[0] = eb.level
                }
                if (resonance[1] == -1) {
                    resonance[1] = eb.grade
                } else if (eb.grade < resonance[1]) {
                    resonance[1] = eb.grade
                }
            } else { // 宝物
                ++tCount
                if (resonance[2] == -1) {
                    resonance[2] = eb.level
                } else if (eb.level < resonance[2]) {
                    resonance[2] = eb.level
                }
                if (resonance[3] == -1) {
                    resonance[3] = eb.grade
                } else if (eb.grade < resonance[3]) {
                    resonance[3] = eb.grade
                }
            }
        }

        // 2.6 套装属性加成
        suit.forEach({ key, ls ->
            int val = ls.size()

            if (val > 1) { // 套装属性2件开始算起，2件就可以有suitadd第一个属性加成，3件有2个属性，4件有全部4个属性
                EquipConf ebinfo = Readonly.getInstance().findEquip(key)
                val -= 1
                if (val == 3) {
                    val = ebinfo.suitadd.length / 2
                }

                for (int i = 0; i < val; ++i) {
                    int id = ebinfo.suitadd[i * 2]
                    int vl = ebinfo.suitadd[i * 2 + 1]
                    preDealAttrs(id, vl, allattrs, sectattrs)
                }
            }

        })

        // 2.7 装备与宝物的共鸣

        // 装备必须4个
        if (eCount >= 4) {
            for (int i = 0; i < 2; ++i) {
                int[] rb = Readonly.getInstance().findResonance(resonance[i], i)
                if (rb != null) {
                    for (int j = 0; j < rb.length; j += 2) {
                        preDealAttrs(rb[j], rb[j + 1], allattrs, sectattrs)
                    }
                }
            }
        }

        // 宝物必须2个
        if (tCount >= 2) {
            for (int i = 2; i < 4; ++i) {
                int[] rb = Readonly.getInstance().findResonance(resonance[i], i)
                if (rb != null) {
                    for (int j = 0; j < rb.length; j += 2) {
                        preDealAttrs(rb[j], rb[j + 1], allattrs, sectattrs)
                    }
                }
            }
        }
    }

    /**
     * 个人属性，突破，升级，装备，合体，缘分导致的全体属性部分暂存; 而单体数值进行处理.
     *
     * @param key
     * @param val
     * @param allattrs
     * @param sectattrs
     */
    public void preDealAttrs(int key, float val, Map<Integer, Float> allattrs, Map<Integer, Float> sectattrs) {
        switch (key) {
            case 21: // 概率反弹物理百分比伤害
                reflectRatio += val / 1000
                reflectPercent += val % 1000
                break
            case 22: // 概率反弹法术百分比伤害
                mReflectRatio += val / 1000
                mReflectPercent += val % 1000
                break
            case 23: // 四属性百分比提高
                aloneattrs.merge(48, val, { a, b -> a + b })
                aloneattrs.merge(49, val, { a, b -> a + b })
                aloneattrs.merge(57, val, { a, b -> a + b })
                aloneattrs.merge(58, val, { a, b -> a + b })
                break
            case 24: // 被攻击时，概率回血
                drawRatio += val / 1000
                drawLife += val % 1000
                break

            case 40: //生命数值
                maxLife += val
                break
            case 41: //攻击数值
                attack += val
                break
            case 42: //防御数值
                defs += val
                mdef += val
                break
            case 43: //暴击提高Buff
                critical += val
                break
            case 44: //命中提高Buff
                aim += val
                break
            case 45: //闪避提高Buff
                dodge += val
                break
            case 46: //抗暴提高Buff
                antiCrit += val
                break
            case 47: //初始怒气增加
                anger += val
                break
            case 51: // 增加伤害
                enHarm += val
                break
            case 52: // 难伤配置，易伤存储
                overHarm -= val
                break
            case 53: //回合恢复怒气增加
                angerCover += val
                break
            case 54: //法防
                mdef += val
                break
            case 55: //物防
                defs += val
                break
            case 56: //机率造成双倍伤害
                multiHarmRatio += val
                break
            case 59: // 速度提高
                speed += val
                break

        // 以下是个人百分比提高,等待全体与阵营的合并
            case 50: // 防御
                aloneattrs.merge(57, val, { a, b -> a + b })
                aloneattrs.merge(58, val, { a, b -> a + b })
                break
            case 37: // 攻击 /100
                aloneattrs.merge(48, val / 100f as float, { a, b -> a + b })
                break;
            case 38: // 生命 /100
                aloneattrs.merge(49, val / 100f as float, { a, b -> a + b })
                break;
            case 39: // 防御 /100
                aloneattrs.merge(57, val / 100 as float, { a, b -> a + b })
                aloneattrs.merge(58, val / 100 as float, { a, b -> a + b })
                break;
            case 35: // 魔法伤害降低
            case 36: // 物理伤害降低
            case 48: // 攻击
            case 49: // 生命
            case 57: // 法防
            case 58: // 物防
                aloneattrs.merge(key, val, { a, b -> a + b })
                break

        // 全体数值
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
                // 全体百分比
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 171:
            case 172:
            case 173:
            case 174:
            case 154:
            case 155:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
                if (allattrs != null) {
                    allattrs.merge(key, val, { a, b -> a + b })
                }
                break

        // 阵营数值
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
                // 阵营百分比
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 181:
            case 182:
            case 183:
            case 184:
                if (sectattrs != null) {
                    sectattrs.merge(key, val, { a, b -> a + b })
                }
                break

            default: //其他增加百分比的，最后计算
                println("xxxxx 处理特殊属性增强的时候，属性id:" + key + " 没有处理，可能是新增未加入代码!")
                break
        }
    }

    /**
     * 技能与缘分处理 (检查完毕)
     *
     * @param allattrs
     * @param sectattrs
     */
    void handleSkills(List<HeroStruct> his, HashMap<Integer, Float> allattrs, HashMap<Integer, Float> sectattrs) {

        // 3. 英雄技能上绑定的属性：突破效果，组合技与缘分
        // 受到皮肤影响的参数
        int heroId = heroStruct.hero

        // 领主的皮肤不为0，模拟该英雄
        if (Defs.isLordID(heroId) && heroStruct.skin != 0) {
            heroId = heroStruct.skin
        }

        HeroSkillConf hsc = Readonly.getInstance().findSkill(heroId)
        if (hsc != null) {
            // 3.1 组合技
            if (hsc.attack3eff.length > 0) { // 首先判断是否存在组合技，然后再判断是否激活
                groupSkill = true
                for (int c : hsc.cost) {
                    if (battle.getTeam(side).stream().noneMatch({ FightHero info -> (
                            info.heroStruct.hero == c || (info.heroStruct.skin == c)
                    ) })) {
                        groupSkill = false
                        break
                    }
                }
            }

            // 3.1.1 检测超级组合技
            if (heroStruct.grade >= 10 && hsc.attack4eff.length > 0) {
                if (groupSkill) {
                    superGroupSkill = true
                }
            }

            // 3.2 天命导致的技能系数增幅
            for (int i = 0; i < hsc.skillup.length; ++i) {
                fateSkillUp[i] = (heroStruct.fate - 1) * hsc.skillup[i]
            }
        }

        // 皮肤不能影响的参数
        HeroSkillConf hsb = Readonly.getInstance().findSkill(heroStruct.hero)
        if (hsb != null) {
            // 3.3 突破效果 self
            if (heroStruct.gift < 25) {
                for (int i = 0; i < heroStruct.grade; ++i) {
                    int[] be = hsb.breakeff[i]
                    preDealAttrs(be[0], be[1], allattrs, sectattrs)
                }
            } else {
                for (int i = 0; i < heroStruct.grade; ++i) {
                    int[] be = hsb.breakeff2[i]
                    preDealAttrs(be[0], be[1], allattrs, sectattrs)
                }
            }

            // 3.4 缘分 self
            // 以下四个是英雄缘分, 如果是领主 前4个缘分是装备
            boolean isLord = Defs.isLordID(heroStruct.hero)

            if (hsb.gk1eff.length > 0) {
                if (isLord) {
                    if (heroStruct.equipStructs.stream().anyMatch({ eq -> (eq != null && eq.id == hsb.gk1cost[0]) })) {
                        for (int ii = 0; ii < hsb.gk1eff.length; ii += 2) {
                            int id = hsb.gk1eff[ii]
                            int vl = hsb.gk1eff[ii + 1]
                            preDealAttrs(id, vl, allattrs, sectattrs)
                        }
                    }
                } else {
                    boolean flag = true
                    for (int c : hsb.gk1cost) {
                        if (his.stream().noneMatch({ info -> info.hero == c })) {
                            flag = false
                            break
                        }
                    }

                    if (flag) {
                        for (int ii = 0; ii < hsb.gk1eff.length; ii += 2) {
                            int id = hsb.gk1eff[ii]
                            int vl = hsb.gk1eff[ii + 1]
                            preDealAttrs(id, vl, allattrs, sectattrs)
                        }
                    }
                }
            }
            if (hsb.gk2eff.length > 0) {
                if (isLord) {
                    if (heroStruct.equipStructs.stream().anyMatch({ eq -> (eq != null && eq.id == hsb.gk2cost[0]) })) {
                        for (int ii = 0; ii < hsb.gk2eff.length; ii += 2) {
                            int id = hsb.gk2eff[ii]
                            int vl = hsb.gk2eff[ii + 1]
                            preDealAttrs(id, vl, allattrs, sectattrs)
                        }
                    }
                } else {
                    boolean flag = true
                    for (int c : hsb.gk2cost) {
                        if (his.stream().noneMatch({ info -> info.hero == c })) {
                            flag = false
                            break
                        }
                    }

                    if (flag) {
                        for (int ii = 0; ii < hsb.gk2eff.length; ii += 2) {
                            int id = hsb.gk2eff[ii]
                            int vl = hsb.gk2eff[ii + 1]
                            preDealAttrs(id, vl, allattrs, sectattrs)
                        }
                    }
                }
            }
            if (hsb.gk3eff.length > 0) {
                if (isLord) {
                    if (heroStruct.equipStructs.stream().anyMatch({ eq -> (eq != null && eq.id == hsb.gk3cost[0]) })) {
                        for (int ii = 0; ii < hsb.gk3eff.length; ii += 2) {
                            int id = hsb.gk3eff[ii]
                            int vl = hsb.gk3eff[ii + 1]
                            preDealAttrs(id, vl, allattrs, sectattrs)
                        }
                    }
                } else {
                    boolean flag = true
                    for (int c : hsb.gk3cost) {
                        if (his.stream().noneMatch({ info -> info.hero == c })) {
                            flag = false
                            break
                        }
                    }

                    if (flag) {
                        for (int ii = 0; ii < hsb.gk3eff.length; ii += 2) {
                            int id = hsb.gk3eff[ii]
                            int vl = hsb.gk3eff[ii + 1]
                            preDealAttrs(id, vl, allattrs, sectattrs)
                        }
                    }
                }
            }
            if (hsb.gk4eff.length > 0) {
                if (isLord) {
                    if (heroStruct.equipStructs.stream().anyMatch({ eq -> (eq != null && eq.id == hsb.gk4cost[0]) })) {
                        for (int ii = 0; ii < hsb.gk4eff.length; ii += 2) {
                            int id = hsb.gk4eff[ii]
                            int vl = hsb.gk4eff[ii + 1]
                            preDealAttrs(id, vl, allattrs, sectattrs)
                        }
                    }
                } else {
                    boolean flag = true
                    for (int c : hsb.gk4cost) {
                        if (his.stream().noneMatch({ info -> info.hero == c })) {
                            flag = false
                            break
                        }
                    }

                    if (flag) {
                        for (int ii = 0; ii < hsb.gk4eff.length; ii += 2) {
                            int id = hsb.gk4eff[ii]
                            int vl = hsb.gk4eff[ii + 1]
                            preDealAttrs(id, vl, allattrs, sectattrs)
                        }
                    }
                }
            }

            // 以下是装备的四个缘分

            if (hsb.gk5eff.length > 0) {
                if (heroStruct.equipStructs.stream().anyMatch({ eq -> (eq != null && eq.id == hsb.gk5cost) })) {
                    for (int ii = 0; ii < hsb.gk5eff.length; ii += 2) {
                        int id = hsb.gk5eff[ii]
                        int vl = hsb.gk5eff[ii + 1]
                        preDealAttrs(id, vl, allattrs, sectattrs)
                    }
                }
            }
            if (hsb.gk6eff.length > 0) {
                if (heroStruct.equipStructs.stream().anyMatch({ eq -> (eq != null && eq.id == hsb.gk6cost) })) {
                    for (int ii = 0; ii < hsb.gk6eff.length; ii += 2) {
                        int id = hsb.gk6eff[ii]
                        int vl = hsb.gk6eff[ii + 1]
                        preDealAttrs(id, vl, allattrs, sectattrs)
                    }
                }
            }
            if (hsb.gk7eff.length > 0) {
                if (heroStruct.equipStructs.stream().anyMatch({ eq ->
                    return (eq != null && eq.id == hsb.gk7cost)
                })) {
                    for (int ii = 0; ii < hsb.gk7eff.length; ii += 2) {
                        int id = hsb.gk7eff[ii]
                        int vl = hsb.gk7eff[ii + 1]
                        preDealAttrs(id, vl, allattrs, sectattrs)
                    }
                }
            }
            if (hsb.gk8eff.length > 0) {
                if (heroStruct.equipStructs.stream().anyMatch({ eq ->
                    return (eq != null && eq.id == hsb.gk8cost)
                })) {

                    for (int ii = 0; ii < hsb.gk8eff.length; ii += 2) {
                        int id = hsb.gk8eff[ii]
                        int vl = hsb.gk8eff[ii + 1]
                        preDealAttrs(id, vl, allattrs, sectattrs)
                    }
                }
            }
        }
    }

    /**
     * 全体与阵营提升合并
     *
     * @param allattrs
     * @param sectattrs
     */
    void merge(HashMap<Integer, Float> allattrs, HashMap<Integer, Float> sectattrs) {
        // 合并 全体提升效果 和 阵营提升效果
        Iterator itr1 = allattrs.entrySet().iterator()
        while (itr1.hasNext()) {
            Map.Entry<Integer, Float> entry = (Map.Entry<Integer, Float>) itr1.next()
            int key = 0
            Object o1 = (entry.getKey())
            if (o1 instanceof String) {
                key = Integer.parseInt(o1)
            } else {
                key = o1
            }
            float val = 0
            Object o2 = (entry.getValue())
            if (o2 instanceof String) {
                val = Float.parseFloat(o2)
            } else {
                val = o2
            }
            switch (key) {
            // 全体数值
                case 60:
                    maxLife += val
                    break
                case 61:
                    attack += val
                    break
                case 62:
                    defs += val
                    mdef += val
                    break
                case 63:
                    critical += val
                    break
                case 64:
                    aim += val
                    break
                case 65:
                    dodge += val
                    break
                case 66:
                    antiCrit += val
                    break
                case 67:
                    anger += val
                    break
                case 68:// 全体百分比
                    aloneattrs.merge(48, val, { a, b -> a + b })
                    break
                case 69:
                    aloneattrs.merge(49, val, { a, b -> a + b })
                    break
                case 70:
                    aloneattrs.merge(57, val, { a, b -> a + b })
                    aloneattrs.merge(58, val, { a, b -> a + b })
                    break
                case 150:
                    aloneattrs.merge(150, val, { a, b -> a + b })
                    break
                case 151:
                    aloneattrs.merge(48, val/100f as float, { a, b -> a + b })
                    break
                case 152:
                    aloneattrs.merge(49, val/100f as float, { a, b -> a + b })
                    break
                case 153:
                    aloneattrs.merge(57, val/100f as float, { a, b -> a + b })
                    aloneattrs.merge(58, val/100f as float, { a, b -> a + b })
                    break
                case 154:
                    aloneattrs.merge(35, val, { a, b -> a + b })
                    break
                case 155:
                    aloneattrs.merge(36, val, { a, b -> a + b })
                    break

                case 71:
                    enHarm += val
                    break
                case 72:
                    overHarm -= val
                    break
                case 73:
                    angerCover += val
                    break
                case 171:
                    sectHarmUp[0] += val / 100f
                    break
                case 172:
                    sectHarmUp[1] += val / 100f
                    break
                case 173:
                    sectHarmUp[2] += val / 100f
                    break
                case 174:
                    sectHarmUp[3] += val / 100f
                    break
                case 75:
                    defs += val
                    break
                case 76:
                    mdef += val
                    break
                case 77:
                    speed += val
                    break
            }
        }

        if (sectattrs != null) {
            Iterator itr = sectattrs.entrySet().iterator()
            while (itr.hasNext()) {
                Map.Entry<Integer, Float> entry = (Map.Entry<Integer, Float>) (itr.next())
                int key = entry.getKey()
                float val = entry.getValue()
                switch (key) {
                // 全体数值
                    case 80:
                        maxLife += val
                        break
                    case 81:
                        attack += val
                        break
                    case 82:
                        defs += val
                        mdef += val
                        break
                    case 83:
                        critical += val
                        break
                    case 84:
                        aim += val
                        break
                    case 85:
                        dodge += val
                        break
                    case 86:
                        antiCrit += val
                        break
                    case 87:
                        anger += val
                        break
                    case 88:// 全体百分比
                        aloneattrs.merge(48, val, {a, b -> a + b})
                        break
                    case 89:
                        aloneattrs.merge(49, val, {a, b -> a + b})
                        break
                    case 90:
                        aloneattrs.merge(57, val, { a, b -> a + b })
                        aloneattrs.merge(58, val, { a, b -> a + b })
                        break
                    case 91:
                        enHarm += val
                        break
                    case 92:
                        overHarm -= val
                        break
                    case 93:
                        angerCover += val
                        break
                    case 181:
                        sectHarmUp[0] += val / 100f
                        break
                    case 182:
                        sectHarmUp[1] += val / 100f
                        break
                    case 183:
                        sectHarmUp[2] += val / 100f
                        break
                    case 184:
                        sectHarmUp[3] += val / 100f
                        break
                }
            }
        }

        // 以上的合并完成后，再遍历自身的所有百分比提升作为总结

        for (Map.Entry<Integer, Float> entry : aloneattrs.entrySet()) {
            int key = entry.getKey()
            float val = entry.getValue()

            switch (key) {
                case 35:
                    magLower += val
                    break
                case 36:
                    phyLower += val
                    break
                case 48:
                    attack += (int) (attack / 100f * val)
                    break
                case 49:
                    maxLife += (int) (maxLife / 100f * val)
                    break
                case 57:
                    mdef += (int) (mdef / 100f * val)
                    break
                case 58:
                    defs += (int) (defs / 100f * val)
                    break
                case 150:
                    speed += (int) (speed / 100f * val)
                    break
                default:
                    break
            }
        }
        aloneattrs.clear()

        // 生命值同步, 最大生命同步到当前生命
        life = maxLife
    }

    /**
     * 判断角色是否死亡
     *
     * @return true: 死亡
     * false: 没有死亡
     */
    boolean isDead() {
        if (life <= 0) {
            return true
        }
        return false
    }

    /**
     * 血量扣减与增加，触发对应的检测
     *
     * @param _life
     */
    void fixLife(long _life) throws GameOverException {
        boolean flag = false

        life += _life
        if (life <= 0) {
            life = 0
            if (battle.checkOver(side)) {
                flag = true
            }
        }

        if (life > maxLife) {
            life = maxLife
        }

        if (flag) {
            int s = 左边
            if (side == 左边) {
                s = 右边
            }
            throw new GameOverException(s)
        }
    }

    GroovyFightSystem getBattle() {
        return battle
    }

    /**
     * 增加Buff
     * <p>
     * 1. 技能与突破带的buff
     * 2. 发动攻击后，产生的buff.
     *
     * @param buff
     */
    protected void addBuff(BuffInfo buff) {
        /**
         * 如果buff的索引源不为0，则需要去重原来已有的这个源
         */
        if (buff.getSource() != 0) {
            for (int i = 0; i < buffs.size();) {
                BuffInfo bi = buffs.get(i)
                if (bi.getClass() == buff.getClass() && bi.getSource() == buff.getSource()) {
//                    println(this.name + " 同源 去掉buff:" + bi.class.name + " i:" + i)
                    delBuff(i)
                } else {
                    ++i
                }
            }
        }

        if (buffs.add(buff)) {
            buff.onAddStatus()
        }
    }

    /**
     * 增加眩晕
     *
     * @param round 眩晕回合数
     */
    void add眩晕(int round) {
        for (BuffInfo buff : buffs) {
            if (buff instanceof 眩晕Buff) {
                buff.round = round
                return
            }
        }

        addBuff(new 眩晕Buff(this, round))
    }

    /**
     * 增加灼烧
     *
     * @param round 回合
     */
    void add灼烧(int round, long harm) {
        addBuff(new 灼烧Buff(this, round, harm))
    }

    /**
     * 增加中毒
     *
     * @param round 回合
     */
    void add中毒(int round, long harm) {
        addBuff(new 中毒Buff(this, round, harm))
    }

    /**
     * 增加降防
     *
     * @param round 回合
     */
    void add降防(int source, int round, int ratio) {
        addBuff(new 降防Buff(this, source, round, ratio))
    }

    void add加防(int source, int round, int ratio) {
        addBuff(new 加防Buff(this, source, round, ratio))
    }

    /**
     * 增加降攻
     *
     * @param round 回合
     */
    void add降攻(int source, int round, int ratio) {
        addBuff(new 降攻Buff(this, source, round, ratio))
    }

    void add加攻(int source, int round, int ratio) {
        addBuff(new 加攻Buff(this, source, round, ratio))
    }

    /**
     * 增加易伤
     *
     * @param round 回合
     */
    void add易伤(int source, int round, int ratio) {
        addBuff(new 易伤Buff(this, source, round, ratio))
    }

    void add难伤(int source, int round, int ratio) {
        addBuff(new 难伤Buff(this, source, round, ratio))
    }

    void add闪避提高(int source, int round, int ratio) {
        addBuff(new 闪避提高Buff(this, source, round, ratio))
    }

    void add暴击提高(int source, int round, int ratio) {
        addBuff(new 暴击提高Buff(this, source, round, ratio))
    }

    void add抗暴提高(int source, int round, int ratio) {
        addBuff(new 抗暴提高Buff(this, source, round, ratio))
    }

    void add命中提高(int source, int round, int ratio) {
        addBuff(new 命中提高Buff(this, source, round, ratio))
    }

    void add无敌(int round) {
        addBuff(new 无敌(this, round))
    }

    void add龙(int speed, int harm, int round) {
        addBuff(new 龙Buff(this, speed, harm, round))
    }

    void add神器1(int round) {
        addBuff(new 神器1Buff(this, round))
    }
    void add神器2(int round) {
        addBuff(new 神器2Buff(this, round))
    }
    void add神器3(int round) {
        addBuff(new 神器3Buff(this, round))
    }

    /**
     * 增加减伤
     *
     * @param round 回合
     */
    void add减伤(int source, int round, int ratio) {
        addBuff(new 减伤Buff(this, source, round, ratio))
    }

    void add加伤(int source, int round, int ratio) {
        addBuff(new 加伤Buff(this, source, round, ratio))
    }

    void add治疗(int round, long harm) {
        addBuff(new 治疗Buff(this, round, harm))
    }

    /**
     * 删除此buff的时候触发
     *
     * @param buff
     */
    void delBuff(BuffInfo buff) {
        if (buffs.remove(buff)) {
            buff.onDelStatus()
        }
    }

    void delBuff(int i) {
        BuffInfo bi = buffs.remove(i)
        if (bi != null) {
//            println(this.name + " 去掉buff:" + bi.class.name + " i:" + i)
            bi.onDelStatus()
        }
    }

    /**
     * 清除所有buff
     */
    void delGoodBuff() {
        for (int i = 0; i < buffs.size();) {
            if (buffs.get(i).goodness && !buffs.get(i).cantDel) {
                delBuff(i)
            } else {
                ++i
            }
        }
    }

    void delAll灼烧() {
        for (int i = 0; i < buffs.size();) {
            if (buffs.get(i).getClass().getName() == "灼烧Buff") {
                delBuff(i)
            } else {
                ++i
            }
        }
    }

    /**
     * 清除所有debuff
     */
    void delBadBuff() {
        for (int i = 0; i < buffs.size();) {
            if (!buffs.get(i).goodness && !buffs.get(i).cantDel) {
                delBuff(i)
            } else {
                ++i
            }
        }
    }

    int getForbidAction() {
        return forbidAction
    }

    void setForbidAction(int forbidAction) {
        this.forbidAction = forbidAction
    }

    int fixForbidAction(int delta) {
        this.forbidAction += delta
        return this.forbidAction
    }

    void checkBuffsRoundBefore(int round) throws GameOverException {
        // 本回合开始，将本次攻击数字重置

        for (BuffInfo buff : buffs) {
            buff.onRoundBefore(round)
        }
    }

    void checkBuffsRoundEnd(int round) {
        anger += angerCover
        battle.getCurrent().anger = anger

        if (buffs != null) {
            for (int i = 0; i < buffs.size();) {
                BuffInfo buff = buffs.get(i)
                if (--(buff.round) <= 0) {
//					println("---- round:" + round + " " + this.name + " 取消buff:" + buff.toString())
                    delBuff(i)
                    buff.onRoundEnd(round)
                } else {
                    ++i
                }
            }
        } else {
//            println("英雄:(" + index + "), 方向(" + side + ") 没有buffs.")
        }
    }

    boolean isGroupSkill() {
        return groupSkill
    }

    void setGroupSkill(boolean groupSkill) {
        this.groupSkill = groupSkill
    }

    boolean isSuperGroupSkill() {
        return superGroupSkill
    }

    void setSuperGroupSkill(boolean superGroupSkill) {
        this.superGroupSkill = superGroupSkill
    }


    String debug(double power) {
        return "战斗力[${power}]  英雄[${name}] 血量:${maxLife} 攻击:${attack} 物理防御:${defs} 法防:${mdef} 暴击率:${critical} 命中率:${aim} 闪避率:${dodge} 抗爆率:${antiCrit} 增伤率:${enHarm} 易伤率:${overHarm} 速度:${speed}\n" + heroStruct.toString() + "\n"
    }

    /**
     * 攻击类型的汉字表示
     *
     * @return
     */
    String modeName() {
        switch (mode) {
            case 1:
                return "物理攻击"
            case 2:
                return "法术攻击"
            case 3:
                return "防御攻击"
            case 4:
                return "辅助攻击"
        }
        return "未知攻击类型:" + mode
    }

    @Override
    String toString() {
        return "FightHero{" +
                "battle=" + battle +
//                ", heroStruct=" + heroStruct +
                ", name='" + name + '\'' +
                ", side=" + side +
                ", index=" + index +
                ", mode=" + mode +
                ", sect=" + sect +
                ", maxLife=" + maxLife +
                ", life=" + life +
                ", attack=" + attack +
                ", defs=" + defs +
                ", mdef=" + mdef +
                ", critical=" + critical +
                ", antiCrit=" + antiCrit +
                ", dodge=" + dodge +
                ", aim=" + aim +
                ", critHarm=" + critHarm +
                ", anger=" + anger +
                ", angerCover=" + angerCover +
                ", forceAttack=" + forceAttack +
                ", multiHarmRatio=" + multiHarmRatio +
                ", overHarm=" + overHarm +
                ", enHarm=" + enHarm +
                ", forbidAction=" + forbidAction +
                ", groupSkill=" + groupSkill +
                ", superGroupSkill=" + superGroupSkill +
                ", fateSkillUp=" + Arrays.toString(fateSkillUp) +
                ", sectHarmUp=" + Arrays.toString(sectHarmUp) +
                ", speed=" + speed +
                ", reflectRatio=" + reflectRatio +
                ", reflectPercent=" + reflectPercent +
                ", mReflectRatio=" + mReflectRatio +
                ", mReflectPercent=" + mReflectPercent +
                ", drawLife=" + drawLife +
                ", drawRatio=" + drawRatio +
                ", buffs=" + buffs +
                ", tempHarm=" + tempHarm +
                ", aloneattrs=" + aloneattrs +
                '}'
    }
}

/**
 * 攻击模式
 */
enum FindPeerEnum {
    前列单体,
    后列单体,
    全体敌方,
    前列敌方,
    后列敌方,
    一行敌方,
    相邻敌方,
    生命最少敌方,

    随机3个敌方,
    随机2个敌方,
    随机1个敌方,

    单体己方,
    全体己方,
    生命最少己方,
    随机3个己方,
    随机2个己方,
    随机1个己方,
}

/**
 * 战斗结束
 */
class GameOverException extends Exception {
    private int side

    GameOverException(int _side) {
        side = _side
    }

    int getSide() {
        return side
    }

    void setSide(int side) {
        this.side = side
    }
}

/**
 * Created by Administrator on 2017/2/22 0022.
 *
 * 规则，序号最高级排序，如果序号相同，则排序攻击速度，如果也相同，则攻击方优先就是左边的先出手
 *
 */
class HeroInfoComparator implements Comparator<FightHero> {

    @Override
    int compare(FightHero o1, FightHero o2) {
        if (o1.speed > o2.speed) {
            return -1
        } else if (o1.speed < o2.speed) {
            return 1
        } else { // 顺序相同，比较
            if (o1.index < o2.index) {
                return -1
            } else if (o1.index > o2.index) {
                return 1
            } else {
                if (o1.side == 1) {
                    return -1
                }
                return 1
            }
        }
    }
}

class EffectRecord {
    /**
     * 受到效果
     */
    private int side
    /**
     * 受到效果的序号
     */
    private int index
    /**
     * 效果类型,分为两种:
     * 1.指示英雄被赋予了一种buff或者debuff, 此时 val值，正数表示赋予，负数表示取消,
     * 对应的客户端应该根据对应的效果，处理英雄身上的特效.
     *
     * 2.指示本次回合内，某个效果导致的伤害，比如灼烧效果被赋予后，每个回合，他会有一定的伤害，此时
     * val表示的伤害具体值, 正数是加血，负数是扣血
     *
     * 综上所属，灼烧一个效果会有2个eff表示，一个是提示客户端展示特效，取消特效；一个是提示客户端本次伤害的特效具体数值
     *
     */
    private int eff

    /**
     * 如果效果带伤害，这个值表示伤害的数值
     */
    private long val

    /**
     * 是否暴击
     */
    private boolean crit = false

    int getIndex() {
        return index
    }

    void setIndex(int index) {
        this.index = index
    }

    int getEff() {
        return eff
    }

    void setEff(int eff) {
        this.eff = eff
    }

    long getVal() {
        return val
    }

    void setVal(long val) {
        this.val = val
    }

    int getSide() {
        return side
    }

    void setSide(int side) {
        this.side = side
    }

    boolean isCrit() {
        return crit
    }

    void setCrit(boolean crit) {
        this.crit = crit
    }

    EffectRecord() {}

    EffectRecord(int _side, int _index, int _eff, long _val, boolean _crit) {
        side = _side
        index = _index
        eff = _eff
        val = _val
        crit = _crit
    }

    @Override
    String toString() {
        return "EffectRecord{" + "side=" + side + ", index=" + index + ", eff=" + eff + ", val=" + val + '}'
    }
}

/**
 * Created by Administrator on 2017/3/1 0001.
 * 回合内的一次行动, 包含主动的攻击(或者不能攻击)，本次的跳过与buff的演算
 */
class ActionRecord {
    /**
     * 行动方
     */
    int side
    /**
     * 英雄序号
     */
    int index
    /**
     * 当前回合
     */
    int round
    /**
     * 攻击模式:
     * 1. 普通攻击
     * 2. 怒气技能
     * 3. 团技
     * 4. 超级团技
     */
    int attackMode
    /**
     * 技能效果表:
     * <p>
     * 1到13描述的各种不同的攻打目标的方式
     */
    int skillMode

    /**
     * 本次行动产生和结束的各种Buff
     */
    List<EffectRecord> effs = new ArrayList<>()

    /**
     * 本次结束后anger的实际值
     */
    int anger

//    @JsonIgnore
//    private String cacheMsg = ""

    ActionRecord(int _round, int _side, int _index) {
        round = _round
        side = _side
        index = _index
    }

    /**
     * 增加效果伤害:
     * <p>
     * 灼烧Buff
     * 中毒Buff
     */
//     void addEffectHarm(int side, int index, int mode, long harm) {
//        effs.add(new EffectRecord(side, index, mode, harm, false))
//    }

    void addEffectHarm(FightHero hi, int mode, long harm, boolean crit) {
        effs.add(new EffectRecord(hi.side, hi.index, mode, harm, crit))
    }

    String debug(GroovyFightSystem battle, int _ri) {
        String msg = "Round:[" + battle.getRound() + "." + _ri + "] "

        msg += " [" + "] 行动 >>"

        if (effs.size() <= 0) {
            msg += " 没有任何效果记录，叼炸天.\n"
        }

        for (EffectRecord eff : effs) {
            FightHero hi = battle.getHeroInfo(eff.getSide(), eff.getIndex())
//            msg += "\t[" + battle.ff.getSide(), eff.getIndex()) + ".(" + eff.getIndex() + ")" + "]"
            if (eff.getEff() < 1000) { // buff类型
                if (eff.getVal() > 0) {
                    msg += " 增加了状态: " + effToString(eff.getEff())
                } else {
                    msg += " 去掉了状态: " + effToString(eff.getEff())
                }
            } else if (eff.getEff() >= 1000) {
                if (eff.getVal() < 0) {
                    msg += " 受到了 " + effToString(eff.getEff()) + " 的伤害: " + eff.getVal() + " 点" +
                            " 剩余血量:" + hi.life
                } else {
                    msg += " 受到了 " + effToString(eff.getEff()) + " 的恢复: " + eff.getVal() + " 点" +
                            " 剩余血量:" + hi.life
                }
            } else {
                msg += " 不知道干了什么:" + eff.toString()
            }
            msg += "\n"
        }

//        cacheMsg = msg
        return msg
    }

    static String effToString(int eff) {
        switch (eff) {
            case BuffInfo.灼烧类型:
                return "{灼烧Buff}"
            case BuffInfo.中毒类型:
                return "{中毒Buff}"
            case BuffInfo.眩晕类型:
                return "{眩晕Buff}"
            case BuffInfo.减伤类型:
                return "{减伤Buff}"
            case BuffInfo.易伤类型:
                return "{易伤Buff}"
            case BuffInfo.降防类型:
                return "{降防Buff}"
            case BuffInfo.治疗类型:
                return "{疗伤}"
            case BuffInfo.加攻类型:
                return "{加攻Buff}"
            case BuffInfo.加防类型:
                return "{加防Buff}"
            case BuffInfo.加伤类型:
                return "{加伤Buff}"
            case BuffInfo.难伤类型:
                return "{难伤Buff}"
            case BuffInfo.命中提高:
                return "{提高命中}"
            case BuffInfo.命中降低:
                return "{降低命中}"
            case BuffInfo.抗暴提高:
                return "{提高抗暴}"
            case BuffInfo.抗暴降低:
                return "{降低抗暴}"
            case BuffInfo.闪避提高:
                return "{提高闪避}"
            case BuffInfo.闪避降低:
                return "{降低闪避}"
            case BuffInfo.暴击提高:
                return "{提高暴击}"
            case BuffInfo.暴击降低:
                return "{降低暴击}"
            case BuffInfo.无敌类型:
                return "{无敌}"

            case BuffInfo.攻击伤害:
                return "{攻击}"
            case BuffInfo.灼烧伤害:
                return "{灼烧Buff}"
            case BuffInfo.中毒伤害:
                return "{中毒Buff}"
            case BuffInfo.怒气控制:
                return "{修改怒气}"
            case BuffInfo.生命比例最低攻击:
                return "{欺凌弱小}"
            case BuffInfo.治疗恢复:
                return "{疗伤}"

            case BuffInfo.群疗恢复:
                return "{群体治疗}"
            default:
                return "这尼玛不认识:" + eff
        }
    }
}

/**
 * 战斗结果结构
 */
class FightResultStruct {
    /**
     * 胜利所属的方
     * <p>
     * 1: 左方
     * 2: 右方
     */
    int win

    /**
     * 你所在的方向，客户端接受到后，自己交换left,right的位置,填充到不同的位置
     * 你可能是右方的 ，但是实际播放，需要将right放置在左边,
     * <p>
     * 服务器记录的战斗过程都是真实的左右方向，客户端只是在自己是右方的时候交换他们的位置，播放效果即刻。
     */
    int yours = 1

    List<SimpleHero> left = new ArrayList<>()

    List<SimpleHero> right = new ArrayList<>()

    /**
     * 行动列表
     */
    List<ActionRecord> actions = new ArrayList<>()
}

/**
 * Created by Administrator on 2017/2/22 0022.
 *
 * 战斗中英雄所带的状态
 *
 */
class BuffInfo {
    static final int 灼烧类型 = 100
    static final int 中毒类型 = 101
    static final int 眩晕类型 = 102
    static final int 减伤类型 = 103
    static final int 易伤类型 = 104
    static final int 降攻类型 = 105
    static final int 降防类型 = 106
    static final int 治疗类型 = 107
    static final int 加攻类型 = 108
    static final int 加防类型 = 109
    static final int 加伤类型 = 110
    static final int 难伤类型 = 111
    static final int 命中提高 = 112
    static final int 命中降低 = 113
    static final int 抗暴提高 = 114
    static final int 抗暴降低 = 115
    static final int 闪避提高 = 116
    static final int 闪避降低 = 117
    static final int 暴击提高 = 118
    static final int 暴击降低 = 119
    static final int 无敌类型 = 120


    // 以上是多个回合的buff/debuff，以下是一次性的

    static final int 灼烧伤害 = 1000
    static final int 中毒伤害 = 1001
    static final int 怒气控制 = 1002
    static final int 生命比例最低攻击 = 1003
    static final int 治疗恢复 = 1004
    static final int 攻击伤害 = 1005
    static final int 群疗恢复 = 1006

    // 以下是神器buff
    static final int 龙 = 5000
    static final int 神器1 = 5001
    static final int 神器2 = 5002
    static final int 神器3 = 5003

    int round; // buff剩余回合数

    /**
     * buff源
     */
    protected int source

    /**
     * true: 增益
     * false: 减益
     */
    boolean goodness

    /**
     * 是否可以被清理掉
     */
    boolean cantDel

    FightHero heroInfo

    BuffInfo(FightHero hi) {
        heroInfo = hi
    }

    /**
     * 状态新增的时候触发
     */
    void onAddStatus() {

    }

    /**
     * 状态消失的时候触发
     */
    void onDelStatus() {

    }

    /**
     * 每个回合开始时 触发
     * @param round
     */
    void onRoundBefore(int round) throws GameOverException {

    }

    /**
     * 每个回合结束时 触发
     */
    void onRoundEnd(int round) {
    }

    /**
     *
     * 当攻击时，调用
     * @return
     *
     */
    long whenAttack(long harm) {
        return harm
    }

    /**
     * 当被攻击时，调用
     * @param harm
     * @return
     */
    long whenBeAttack(long harm) {
        return harm
    }

    int getSource() {
        return source
    }

    void setSource(int source) {
        this.source = source
    }
}

class 中毒Buff extends BuffInfo {
    /**
     * 灼烧伤害
     */
    private long harm

    中毒Buff(FightHero hi, int _round, long _harm) {
        super(hi)
        round = _round
        harm = _harm
        goodness = false
    }

    @Override
    void onRoundBefore(int round) throws GameOverException {
        super.onRoundBefore(round)
        if (harm > 0) {
            heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 中毒伤害, -harm, false)
            heroInfo.fixLife(-harm)
        }
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 中毒类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 中毒类型, -1L, false)
    }
}

class 减伤Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    减伤Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = false
//        source = _source
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.enHarm -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 减伤类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.enHarm += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 减伤类型, -1L, false)
    }

}

class 加伤Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    加伤Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
//        source = _source
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.enHarm += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 加伤类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.enHarm -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 加伤类型, -1L, false)
    }

}

class 加攻Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int attack

    加攻Buff(FightHero hi, int source, int round, int ratio) {
        super(hi)
        this.round = round
        attack = hi.attack * ratio / 100
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack += attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 加攻类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack -= attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 加攻类型, -1L, false)
    }
}

class 加防Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int defs
    private int mdef

    加防Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        defs = hi.defs * ratio / 100
        mdef = hi.mdef * ratio / 100
//        source = _source

        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.defs += defs
        heroInfo.mdef += mdef
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 加防类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.defs -= defs
        heroInfo.mdef -= mdef
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 加防类型, -1L, false)
    }
}

class 命中提高Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    命中提高Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
//        source = _source
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.aim += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 命中提高, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.aim -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 命中提高, -1L, false)
    }
}

class 抗暴提高Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    抗暴提高Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
//        source = _source
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.antiCrit += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 抗暴提高, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.antiCrit -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 抗暴提高, -1L, false)
    }

}

class 无敌 extends BuffInfo {
    无敌(FightHero hi, int round) {
        super(hi)
        this.round = round
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 无敌类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 无敌类型, -1L, false)
    }

    @Override
    long whenBeAttack(long harm) {
        return 0
    }
}

class 易伤Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private float harm

    易伤Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = false
//        source = _source
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.overHarm += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 易伤类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.overHarm -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 易伤类型, -1L, false)
    }
}

class 暴击提高Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    暴击提高Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
//        source = _source
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.critical += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 暴击提高, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.critical -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 暴击提高, -1L, false)
    }

}

class 治疗Buff extends BuffInfo {
    /**
     * 灼烧伤害
     */
    private long harm

    long getHarm() {
        return harm
    }

    void setHarm(long harm) {
        this.harm = harm
    }

    治疗Buff(FightHero hi, int round, long _harm) {
        super(hi)
        this.round = round
        harm = _harm
        goodness = true
    }

    @Override
    void onRoundBefore(int round) throws GameOverException {
        super.onRoundBefore(round)
        if (!heroInfo.isDead() && harm > 0) {
            heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 治疗恢复, harm, false)
            heroInfo.fixLife(harm)
        }
    }


    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 治疗类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 治疗类型, -1L, false)
    }
}

class 灼烧Buff extends BuffInfo {
    /**
     * 灼烧伤害
     */
    private long harm

    long getHarm() {
        return harm
    }

    void setHarm(long harm) {
        this.harm = harm
    }

    void fixHarm(long _harm) {
        this.harm += _harm
    }

    灼烧Buff(FightHero hi, int round, long _harm) {
        super(hi)
        this.round = round
        harm = _harm
        goodness = false
    }

    @Override
    void onRoundBefore(int round) throws GameOverException {
        super.onRoundBefore(round)
        if (harm > 0) {
            heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 灼烧伤害, -harm, false)
            heroInfo.fixLife(-harm)
        }
    }


    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 灼烧类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 灼烧类型, -1L, false)
    }
}

class 眩晕Buff extends BuffInfo {

    眩晕Buff(FightHero hi, int round) {
        super(hi)
        this.round = round
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.fixForbidAction(1)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 眩晕类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.fixForbidAction(-1)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 眩晕类型, -1L, false)
    }
}

class 闪避提高Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    闪避提高Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
//        source = _source
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.dodge += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 闪避提高, 1L, false)
//        println("英雄:" + heroInfo.name + " 闪避提高:" + harm + " 现在是:" + heroInfo.dodge)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.dodge -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 闪避提高, -1L, false)
//        println("英雄:" + heroInfo.name + " 闪避降低:" + harm + " 现在是:" + heroInfo.dodge)
    }

}

class 降攻Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int attack

    降攻Buff(FightHero hi, int source, int round, int ratio) {
        super(hi)
        this.round = round
        attack = hi.attack * ratio / 100
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack -= attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 降攻类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack += attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 降攻类型, -1L, false)
    }
}

class 降防Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int defs
    private int mdef

    降防Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        defs = hi.defs * ratio / 100
        mdef = hi.mdef * ratio / 100
//        source = _source

        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.defs -= defs
        heroInfo.mdef -= mdef
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 降防类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.defs += defs
        heroInfo.mdef += mdef
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 降防类型, -1L, false)
    }
}

class 难伤Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private float harm

    难伤Buff(FightHero hi, int _source, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
//        source = _source
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.overHarm -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 难伤类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.overHarm += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 难伤类型, -1L, false)
    }
}

class 龙Buff extends BuffInfo {
    /**
     * 加伤
     */
    private int harmUp
    private int speed

    龙Buff(FightHero hi, int _speed, int harm, int round) {
        super(hi)
        this.round = round
        harmUp = harm / 100f
        this.speed = _speed

        goodness = false
        cantDel = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.harmUp += harmUp
//        speed = (heroInfo.speed * speed / 100f) as int
        heroInfo.speed -= speed
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 龙, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.harmUp -= harmUp
        heroInfo.speed += speed
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 龙, -1L, false)
    }
}

class 神器1Buff extends BuffInfo {
    神器1Buff(FightHero hi, int round) {
        super(hi)
        this.round = round

        goodness = false
        cantDel = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 神器1, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 神器1, -1L, false)
    }
}

class 神器2Buff extends BuffInfo {
    神器2Buff(FightHero hi, int round) {
        super(hi)
        this.round = round

        goodness = false
        cantDel = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 神器2, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 神器2, -1L, false)
    }
}

class 神器3Buff extends BuffInfo {
    神器3Buff(FightHero hi, int round) {
        super(hi)
        this.round = round

        goodness = false
        cantDel = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 神器3, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 神器3, -1L, false)
    }
}
















