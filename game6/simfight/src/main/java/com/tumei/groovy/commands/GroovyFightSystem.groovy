package com.tumei.groovy.commands

import com.tumei.common.fight.ArtcomStruct
import com.tumei.common.fight.ArtifactStruct
import com.tumei.common.fight.DirectHeroStruct
import com.tumei.common.fight.EquipStruct
import com.tumei.common.fight.FightResult
import com.tumei.common.fight.HeroStruct
import com.tumei.common.fight.HerosStruct
import com.tumei.common.fight.RelicStruct
import com.tumei.common.fight.SimpleHero
import com.tumei.common.utils.Defs
import com.tumei.common.utils.JsonUtil
import com.tumei.common.utils.RandomUtil
import com.tumei.groovy.contract.IFightSystem
import com.tumei.modelconf.*
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.util.stream.Collectors

/**
 * Created by Leon on 2018/3/8.
 *
 * 英雄无敌挂机战斗
 *
 *  注意事项:
 *  1. buff 分为是否互斥，互斥就是同样的 盾buff 如果再来一个，应该和前一个怎么处理的问题，这里使用mix函数进行处理
 *  2. 反戈一击生效必须在当前角色攻击的所有效果之后，再来处理，所以addBuff要将最后一个永远保持为反戈一击，同样的针对第一点反戈一击是互斥的
 *
 */
@Component
@Scope(value = "prototype")
class GroovyFightSystem implements IFightSystem {
    private static final Log log = LogFactory.getLog(GroovyFightSystem.class)

    public static final int 物理攻击 = 1
    public static final int 法术攻击 = 2
    public static final int 防御攻击 = 3
    public static final int 辅助攻击 = 4

    static final int 前列 = 1
    static final int 后列 = 2

    public static final int 左边 = 1
    public static final int 右边 = 2

    @Autowired
    Readonly readonly

    /**
     * 用来记录临时的全体值，个体值，最后合并处理
     */
    SimStruct sim = new SimStruct()

    private int weak = 0

    private int[][] relics = new int[2][]

    // 左方神器
    private List<int[]> leftArt = new ArrayList<>()

    // 右方神器
    private List<int[]> rightArt = new ArrayList<>()


    private List<Roler> left = new ArrayList<>()

    private List<Roler> right = new ArrayList<>()

    private LinkedList<Roler> order_list = new LinkedList<>()

    /**
     * 当前回合
     */
    int round
    /**
     * 当前回合内的小索引
     */
    int round_index
    /**
     * 最大回合
     */
    int max_round = 11
    /**
     * 是否boss战斗
     */
    boolean isBoss = false

    /**
     * 随机种子
     */
    private long seed
    /**
     * 本场战斗的随机
     */
    private Random random
    /**
     * 战斗结果
     */
    private int win_result
    /**
     * 胜利条件
     */
    private int condition

    /**
     * 返回战斗初始状态和结果
     */
    private ResultStruct frb = new ResultStruct()

    private List<ActionRecord> actions = new ArrayList<>()

    /**
     * 当前动作
     */
    private ActionRecord current_action = null

    ActionRecord getCurrent() {
        if (current_action == null) {
            log.error("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx 初始状态无法发送buff xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
        }
        return current_action
    }

    @PostConstruct
    void init() {
        round = 1
        round_index = 0

        seed = System.currentTimeMillis()
        random = new Random(seed)
    }

    /**
     * 100以内的随机数
     * @return
     */
    int getRandom() {
        return Math.abs(random.nextInt() % 100) + 1
    }

    /**
     * 一千以内的随机数
     * @return
     */
    int getRandom1000() {
        return Math.abs(random.nextInt() % 1000) + 1
    }

    private double randDouble() {
        return Math.abs(random.nextDouble());
    }

    /**
     * 获取指定的队伍
     * @param side
     * @return
     */
    List<Roler> getTeam(int side) {
        if (side == 1) {
            return left
        }
        return right
    }

    List<Roler> getPeer(int side) {
        if (side == 1) {
            return right
        }
        return left
    }

    /**
     * 得到队伍中指定位置的英雄角色
     * @param side
     * @param idx
     * @return
     */
    Roler getTeamRole(int side, int idx) {
        List<Roler> team = getTeam(side)
        return team.stream().filter({ rto -> rto.index == idx }).findFirst().orElse(null)
    }

    /**
     * 根据英雄实体，创建战斗角色
     *
     * @param uid
     * @param side
     * @param herosBean
     * @param weak 虚弱指数
     */
    void build(int side, HerosStruct herosBean) {
        sim.reset()
        List<SimpleHero> shs
        List<Roler> his
        if (side == 1) {
            his = left
            shs = frb.left
        } else {
            his = right
            shs = frb.right
        }

        // +++ 将herosBean中的全局属性加到all中
        sim.merge(herosBean.buffs)

        // 检测缘分
        Set<Integer> heros = new HashSet<>()

        for (int i = 0; i < 6; ++i) {
            HeroStruct hb = herosBean.heros[i]
            if (hb != null) {

                int skin = 0
                int skin_level = 0
                if (Defs.isLordID(hb.hero)) {
                    skin = herosBean.skin
                    skin_level = herosBean.skins.getOrDefault(skin, 1)
                }

                // 检查是否有英雄武装, 以及是否有传奇英雄觉醒影响的属性
                boolean wuz = false
                List<int[]> wake_attrs = new ArrayList<>()
                for (RelicStruct relic : herosBean.relics) {
                    if (relic.hero > 0) {
                        LegendHero lh = readonly.findLegendHero(relic.hero)
                        if (relic.hlvl >= 10) {
                            for (int wa : lh.wakatt) {
                                if (wa == hb.hero) {
                                    wuz = true
                                    break
                                }
                            }
                        }

                        if (wuz && relic.hwlvl > 0) {
                            for (int ii = 0; ii < lh.wakeff.length; ii += 2) {
                                int[] tmp = new int[2]
                                tmp[0] = lh.wakeff[ii]
                                tmp[1] = lh.wakeff[ii+1] * relic.hwlvl
                                wake_attrs.add(tmp)
                            }
                        }
                    }
                }

                Roler role = null
                if (wuz) {
                    def hc = readonly.findHero(hb.hero)
                    switch (hc.name) {
                        case "乌尔奇奥拉":
                            role = new 乌尔奇奥拉(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "哈迪斯":
                            role = new 哈迪斯(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "雅典娜":
                            role = new 雅典娜(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "鸣人":
                            role = new 鸣人(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "纲手":
                            role = new 纲手(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "女帝":
                            role = new 女帝(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "大蛇丸":
                            role = new 大蛇丸(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "佐助":
                            role = new 佐助(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "黑岩射手":
                            role = new 黑岩射手(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "貂蝉":
                            role = new 貂蝉(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "诸葛亮":
                            role = new 诸葛亮(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "马超":
                            role = new 马超(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "黄忠":
                            role = new 黄忠(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "刘备":
                            role = new 刘备(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "张飞":
                            role = new 张飞(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "周瑜":
                            role = new 周瑜(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "司马懿":
                            role = new 司马懿(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "赵云":
                            role = new 赵云(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "杨过":
                            role = new 杨过(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "小骨":
                            role = new 小骨(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "杀阡陌":
                            role = new 杀阡陌(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "天山童姥":
                            role = new 天山童姥(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "白子画":
                            role = new 白子画(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "小龙女":
                            role = new 小龙女(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "赵敏":
                            role = new 赵敏(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "周芷若":
                            role = new 周芷若(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "张无忌":
                            role = new 张无忌(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "刀锋女王":
                            role = new 刀锋女王(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "魂魄妖梦":
                            role = new 魂魄妖梦(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "赤瞳":
                            role = new 赤瞳(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "一护":
                            role = new 一护(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "杀生丸":
                            role = new 杀生丸(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "伊利丹":
                            role = new 伊利丹(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "艾露莎":
                            role = new 艾露莎(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "吉尔伽美什":
                            role = new 吉尔伽美什(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "索隆":
                            role = new 索隆(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "自来也":
                            role = new 自来也(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "鼬":
                            role = new 鼬(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "吕布":
                            role = new 吕布(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "关羽":
                            role = new 关羽(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "东方不败":
                            role = new 东方不败(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "张三丰":
                            role = new 张三丰(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "更木剑八":
                            role = new 更木剑八(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        case "亚瑟王":
                            role = new 亚瑟王(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                        default:
                            log.error("可以武装的英雄无法找到对应的武装角色:" + hc.name)
                            role = new Roler(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                            break
                    }
                    role.wuz = 1
                } else {
                    role = new Roler(this, side, i, hb, herosBean.lineups[i], skin, skin_level)
                }

                for (int[] wz : wake_attrs) {
                    deal(role, wz[0], wz[1])
                }

                his.add(role)
                heros.add(hb.hero)
                order_list.add(role)
            }
        }

        // +++ 添加辅助英雄到set中，用于缘分判断
        for (int hbid : herosBean.assists) {
            heros.add(hbid)
        }

        for (int i = 0; i < 6; ++i) {
            HeroStruct hb = herosBean.heros[i]
            if (hb != null) {
                Roler role = getTeamRole(side, i)
                if (role != null) {
                    role.handle_skills(hb, heros)
                }
            }
        }

        // ----------------- begin 神器 -----------------
        List<ArtifactStruct> ass = herosBean.arts
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
                            sim.all.merge(tmp[0], tmp[1] * lvup as float, {a, b -> a + b})
                        }
                        // 强化
                        if (lv > 0) {
                            for (int m = 0; m < conf.stratt.length; ++m) {
                                int[] tmp = conf.stratt[m]
                                sim.all.merge(tmp[0], tmp[1] * lv * lvup as float, {a, b -> a + b})
                            }
                        }

                        // 附加英雄，遍历自己的所有英雄，只要满足conf.advtag,则附加
                        his.forEach({h ->
                            // 只要满足，则增加附加属性给该英雄
                            if (Arrays.stream(conf.advtag).anyMatch({a -> a == h.id})) {
                                for (int m = 0; m < conf.advatt.length; ++m) {
                                    int[] tmp = conf.advatt[m]
                                    if (ac.level >= tmp[0]) {
                                        deal(h, tmp[1], tmp[2])
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
                            sim.all.merge(conf.batt[m], conf.batt[m+1], {a, b -> a + b })
                        }

                        // 强化
                        if (af.level > 1) {
                            for (int m = 0; m < conf.attstr.length; m += 2) {
                                sim.all.merge(conf.attstr[m], conf.attstr[m+1] * (af.level - 1), {a, b -> a + b })
                            }
                        }

                        // 附加
                        for (int m = 0; m < conf.satt.length; ++m) {
                            int[] tmp = conf.satt[m]
                            if (af.level >= tmp[0]) {
                                sim.all.merge(tmp[1], tmp[2], {a, b -> a + b })
                            }
                        }
                        if (conf.bateff.length > 0) {
                            if (side == 左边) {
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




        // 圣物
        for (RelicStruct relic : herosBean.relics) {
            if (relic != null) {
                int rl = 0
                if (relic.star >= 5) {
                    rl = 2
                } else if (relic.star >= 3) {
                    rl = 1
                }

                HolyConf holyConf = readonly.findHoly(relic.id)
                if (holyConf != null) {
                    if (side == 1 && relics[0] == null) {
                        relics[0] = holyConf.skilleff[rl]
                        frb.relics[0] = holyConf.key
                    } else if (side == 2 && relics[1] == null) {
                        relics[1] = holyConf.skilleff[rl]
                        frb.relics[1] = holyConf.key
                    }

                    int lvl = relic.level - 1
                    for (int i = 0; i < holyConf.basatt.size(); i += 2) {
                        int key = holyConf.basatt[i]
                        int val = holyConf.basatt[i + 1] + holyConf.stratt[i + 1] * lvl
                        sim.all.merge(key, val, { a, b -> a + b })
                    }

                    // 查看这个圣物对哪些英雄有影响
                    for (int i = 0; i < holyConf.addhero.size(); ++i) {
                        int ah = holyConf.addhero[i]
                        for (Roler roler in his) {
                            if (roler.id == ah) {
                                // 包含三个属性,需要知道每个属性的提高的次数
                                int[] addatt = holyConf.addatt[i]
                                for (int j = 0; j < 3; ++j) {
                                    int idx = i * 3 + j
                                    int cc = relic.attrs[idx] // 提高的次数
                                    int k = addatt[j * 2]
                                    int v = (addatt[j * 2 + 1] / holyConf.addlimit) * cc
                                    deal(roler, k, v)
                                }
                            }
                        }
                    }
                }
            }
        }

        int speed = 600_0000
        float total_attack = 0
        for (Roler role : his) {
            role.merge()
            if (side == 2) {
                if (weak > 0) {
                    double w = 1.0 - weak / 100.0
                    role.max_life *= w
                    role.life = role.max_life
                    role.attack *= w
                    role.defs *= w
                    role.mdef *= w
                }
            }
            speed += role.speed
            total_attack += role.attack
        }
        total_attack /= 6

        if (herosBean.relics.size() > 0) {
            RelicStruct relic = herosBean.relics[0]
            if (relic != null && relic.hero > 0) {
                // 激活并带上的了传奇英雄 则增加传奇英雄
                Roler roler = new Roler(this, side)
                roler.speed = speed
                order_list.add(roler)

                roler.id = relic.hero
                // 根据传奇英雄数据等级，构建信息
                LegendHero lh = readonly.findLegendHero(roler.id)

                // 一定的三个属性，[伤害值, 暴击率， 暴击伤害]
                roler.attack = total_attack * (lh.basatt[0] + lh.basattup[0] * relic.hlvl) / 100
                roler.crit = lh.basatt[1] + lh.basattup[1] * relic.hlvl
                roler.crit_harm = lh.basatt[2] + lh.basattup[2] * relic.hlvl
                roler.max_life = 1
                roler.life = 1

                // 负数标识激活了传奇英雄
                if (side == 1) {
                    frb.relics[0] = -frb.relics[0]
                } else if (side == 2) {
                    frb.relics[1] = -frb.relics[1]
                }
            }
        }

        // 遍历左边阵营，将全体与阵营提升合并进去
//        log.info("***** 英雄列表 ========================")
        for (Roler hi : his) {
            hi.life = hi.max_life
//            print(hi.debug(0))
            shs.add(new SimpleHero(hi.id, (long) hi.max_life, hi.anger, hi.skin, hi.grade, hi.wuz))
        }

//        log.info("***** 结束英雄列表 ========================")
    }

    void build(List<DirectHeroStruct> _heroStructs, int _relic, int _star, int _legend, int _hlvl) {
        List<Roler> his = right

        int speed = 600_0000
        float total_attack = 0
        int i = 0
        for (DirectHeroStruct hb : _heroStructs) {
            if (hb != null) {
                Roler hi = new Roler(this, 2, i, hb)
                his.add(hi)
                order_list.add(hi)
                speed += hi.speed
                total_attack += hi.attack
                ++i
            }
        }

        total_attack /= 6

        if (_relic > 0) {
            // 1. 圣物
            int rl = 0
            if (_star >= 5) {
                rl = 2
            } else if (_star >= 3) {
                rl = 1
            }
            HolyConf holyConf = readonly.findHoly(_relic)
            if (holyConf != null) {
                relics[1] = holyConf.skilleff[rl]
                frb.relics[1] = holyConf.key
            }

            // 2. 传奇英雄激活
            if (_legend > 0) {
                // 激活并带上的了传奇英雄 则增加传奇英雄
                Roler roler = new Roler(this, 2)
                roler.speed = speed
                order_list.add(roler)

                roler.id = _legend
                // 根据传奇英雄数据等级，构建信息
                LegendHero lh = readonly.findLegendHero(roler.id)

                // 一定的三个属性，[伤害值, 暴击率， 暴击伤害]
                roler.attack = total_attack * (lh.basatt[0] + lh.basattup[0] * _hlvl) / 100
                roler.crit = lh.basatt[1] + lh.basattup[1] * _hlvl
                roler.crit_harm = lh.basatt[2] + lh.basattup[2] * _hlvl
                roler.max_life = 1
                roler.life = 1

                // 负数标识激活了传奇英雄
                frb.relics[1] = -frb.relics[1]
            }
        }


        // 遍历左边阵营，将全体与阵营提升合并进去
        for (Roler hi : his) {
            hi.life = hi.max_life
//            print(hi.debug(0))
            frb.right.add(new SimpleHero(hi.id, (long) hi.max_life, hi.anger, hi.skin, hi.grade, hi.wuz))
        }
    }

    void deal(Roler role, int key, double val) {
        sim.deal(role, key, val)
    }

    /**
     * 计算战斗力,就是把数据填充到左边,计算左边英雄的战斗力
     * @param herosBean
     * @return
     */
    @Override
    long calc_power(HerosStruct herosBean) {
        build(1, herosBean)
        long power = 0
//        log.warn(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
        for (Roler role : left) {
            double p = (role.max_life * 0.02 + role.attack * 0.3 + role.defs * 0.5 + role.mdef * 0.5) * (1.0 + role.crit / 1000 +
                    role.aim / 1000 + role.dodge / 1000 + role.anti_crit / 1000 + role.en_harm / 1000 + role.over_harm / 1000 + role.crit_harm / 100)
//            log.warn("英雄[" + role.name + "]index(" + role.index + ") 战斗力:" + p)
//            log.warn(role.debug(p))
            power += p
        }
//        log.warn("总战斗力:" + power + "  return value:" + (long) power)
//        log.warn("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        return (long) power
    }

    @Override
    FightResult doSceneBattle(HerosStruct herosBean, List<DirectHeroStruct> enemy, int condition, boolean boss, int relic, int star, int legend, int level) {
        isBoss = boss
        FightResult result = new FightResult()
        try {
            build(左边, herosBean)
            build(enemy, relic, star, legend, level)
            this.condition = condition
            result.win = run()
            result.data = JsonUtil.Marshal(frb)
            result.lifes.addAll(getRightLifes())
        } catch (Exception ex) {
            result.win = -1
            result.data = "战斗错误" + ex.getMessage()
        }
        return result
    }

    @Override
    FightResult doBattle(HerosStruct herosBean, HerosStruct other) {
        FightResult result = new FightResult()
        try {
            build(左边, herosBean)
            build(右边, other)
            this.condition = condition
            result.win = run()
            result.data = JsonUtil.Marshal(frb)
            result.lifes.addAll(getRightLifes())
        } catch (Exception ex) {
            result.win = -1
            result.data = "战斗错误" + ex.getMessage()
        }
        return result
    }

    @Override
    FightResult doBattle(HerosStruct herosBean, HerosStruct other, int weak) {
        this.weak = weak

        FightResult result = new FightResult()
        try {
            build(左边, herosBean)
            build(右边, other)
            this.condition = condition
            result.win = run()
            result.data = JsonUtil.Marshal(frb)
            result.lifes.addAll(getRightLifes())
        } catch (Exception ex) {
            result.win = -1
            result.data = "战斗错误" + ex.getMessage()
        }
        return result
    }

    void doRelicAction(boolean rightFirst) {
        if (rightFirst) {
            if (relics[1] != null) {
                current_action = new ActionRecord(0, 右边, -1)
                current_action.attackMode = 1
                current_action.skillMode = relics[1][0]
                doRelicAction(右边, relics[1])
                actions.addAll(current_action)
            }

            if (relics[0] != null) {
                current_action = new ActionRecord(0, 左边, -1)
                current_action.attackMode = 1
                current_action.skillMode = relics[0][0]
                doRelicAction(左边, relics[0])
                actions.addAll(current_action)
            }
        } else {
            if (relics[0] != null) {
                current_action = new ActionRecord(0, 左边, -1)
                current_action.attackMode = 1
                current_action.skillMode = relics[0][0]
                doRelicAction(左边, relics[0])
                actions.addAll(current_action)
            }
            if (relics[1] != null) {
                current_action = new ActionRecord(0, 右边, -1)
                current_action.attackMode = 1
                current_action.skillMode = relics[1][0]
                doRelicAction(右边, relics[1])
                actions.addAll(current_action)
            }
        }
    }

    /**
     * 圣物行动
     * @param _hi
     * @return
     */
    void doArtAction(int side, int[] arts) {
        try {
            switch (arts[0]) {
                case 601:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.全体敌方, 0)
                    if (peers != null) {
                        for (Roler fh : peers) {
                            if (!fh.isDead()) {
                                if (RandomUtil.getBetween(1, 100) <= arts[1]) {
                                    fh.addBuff(new 龙Buff(fh, arts[2], arts[3], arts[4]))
                                }
                            }
                        }
                    }
                    break
                case 801:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.全体己方, 0)
                    if (peers != null) {
                        for (Roler fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 神器1Buff(fh, arts[1]))
                            }
                        }
                    }
                    break
                case 802:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.全体己方, 0)
                    if (peers != null) {
                        for (Roler fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 神器2Buff(fh, arts[1]))
                            }
                        }
                    }
                    break
                case 803:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.全体己方, 0)
                    if (peers != null) {
                        for (Roler fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 神器3Buff(fh, arts[1]))
                            }
                        }
                    }
                    break
            }
        } catch (Exception ex) {
            log.error("神器行动错误, 原因:", ex)
        }
    }

    /**
     * 圣物行动
     * @param _hi
     * @return
     */
    void doRelicAction(int side, int[] arts) {
        try {
            switch (arts[0]) {
                case 811:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.随机友军, arts[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new Type811(fh, arts))
                            }
                        }
                    }
                    break
                case 812:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.前列敌方, 0)
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                int speed = fh.speed * arts[1] / 100
                                fh.addBuff(new 速度降低Buff(fh, arts[2], speed))
                            }
                        }
                    }
                    break
                case 814:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.随机友军, arts[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addAnger((int) arts[2])
                                getCurrent().addEffectHarm(fh, BuffInfo.怒气控制, fh.anger, false)
                            }
                        }
                    }
                    break
                case 813:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.随机友军, arts[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                double shield = fh.max_life * arts[2] / 100
                                fh.addBuff(new ShieldBuff(fh, arts[4], shield))
                                fh.addBuff(new 闪避提高Buff(fh, arts[4], arts[3]))
                            }
                        }
                    }
                    break
                case 815:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.随机友军, arts[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new Shield2Buff(fh, arts[3], arts[2]))
                            }
                        }
                    }
                    break
                case 816:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.随机敌军, arts[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new Type816(fh, arts))
                            }
                        }
                    }
                    break
                case 817:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.随机友军, arts[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new Type817(fh, arts))
                            }
                        }
                    }
                    break
                case 818:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.随机敌军, arts[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 士气降低Buff(fh, arts[3], arts[2]))
                            }
                        }
                    }
                    break
                case 819:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.随机友军, arts[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 士气提高Buff(fh, arts[3], arts[2]))
                            }
                        }
                    }
                    break
                case 820:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.后列敌方, 0)
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addAnger((int) -arts[1])
                                getCurrent().addEffectHarm(fh, BuffInfo.怒气控制, fh.anger, false)
                                fh.addBuff(new Type820(fh, arts))
                            }
                        }
                    }
                    break
                case 821:
                    List<Roler> peers = findPeer(side, 0, FindPeerEnum.随机友军, arts[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new Type821(fh, arts))
                            }
                        }
                    }
                    break
            }
        } catch (Exception ex) {
            log.error("圣器行动错误, 原因:", ex)
        }
    }

    int run() {
        try {
            if (left.size() <= 0) {
                log.info("xxxxx 攻击方部队没有英雄 xxxxxx")
                win_result = 2
            } else if (right.size() <= 0) {
                log.info("xxxxx 防守方部队没有英雄 xxxxxx")
                win_result = 2
            } else {
                win_result = 0 // 默认没有胜利方

                /* --------   神器  -------- */

                // 神器释放 左边
                for (int j = 0; j < leftArt.size(); ++j) {
                    int[] artatt = leftArt.get(j);
                    current_action = new ActionRecord(0, 左边, -2)
                    current_action.attackMode = 1
                    current_action.skillMode = artatt[0]
                    doArtAction(左边, artatt)
                    actions.addAll(current_action)
                }

                // 神器释放 右边
                for (int j = 0; j < rightArt.size(); ++j) {
                    int[] artatt = rightArt.get(j);
                    current_action = new ActionRecord(0, 右边, -2)
                    current_action.attackMode = 1
                    current_action.skillMode = artatt[0]
                    doArtAction(右边, artatt)
                    actions.addAll(current_action)
                }
                /* --------  神器释放  -------- */



                HeroInfoComparator hc = new HeroInfoComparator()

                int lspeed = 9999999
                int rspeed = 9999999
                for (Roler r : order_list) {
                    if (r.side == 1) {
                        lspeed += r.speed
                    } else {
                        rspeed += r.speed
                    }
                    r.onRound()
                }

                // 圣物开场技能释放
                doRelicAction((rspeed > lspeed))

                order_list.sort(hc) // 每个人出手前都要进行计算顺序
                for (Roler r : order_list) {
                    if (!r.isDead()) {
                        r.onFirstRound()
                    }
                }

                while (true) { // 每次循环都是找一个可以行动的角色
                    Roler roler = null;
                    order_list.sort(hc) // 每个人出手前都要进行计算顺序
                    for (Roler r : order_list) {
                        if (!r.isDead() && !r.isActiond) {
                            roler = r;
                            break;
                        }
                    }

                    if (roler == null) { // 标识全部行动了一次
                        round_index = 0
                        if (++round == max_round) {
                            break
                        }

                        for (Roler r : order_list) {
                            r.onRound()
                        }
                    } else {
                        roler.isActiond = true
                        current_action = new ActionRecord(round, roler.side, roler.index)
                        win_result = doAction(roler)
                        ++round_index
//    					println(current_action.debug(this, roler, round_index))
                        actions.add(current_action)

                        if (win_result != 0) { // 战斗结束或者发生了错误
                            break
                        }
                    }
                }

                if (win_result == 0) { // 表示平局，则防守方胜利
                    win_result = 2
                }
            }

            if (win_result == 1) {
                // 在进攻方胜利的时候，如果判定胜利条件不为0，则需要重新判定
                switch (condition) {
                    case 2: // 6个回合内战斗胜利
                        if (round >= 7) {
                            win_result = 2
                        }
                        break
                    case 3: // 我方总血量高于50%
                        if (getLifeRatioBySide(1) < 0.5) {
                            win_result = 2
                        }
                        break
                    case 4: // 我方死亡人数不超过2人
                        if (getDeadsBySide(左边) > 2) {
                            win_result = 右边
                        }
                        break
                    case 5: // 我方总血量高于70%
                        if (getLifeRatioBySide(左边) < 0.7) {
                            win_result = 右边
                        }
                        break
                    case 6: // 我方死亡人数不超过1人
                        if (getDeadsBySide(左边) > 1) {
                            win_result = 右边
                        }
                        break
                    case 7: // 5个回合内战斗胜利
                        if (round >= 5) {
                            win_result = 右边
                        }
                        break
                    case 8: // 我方死亡人数不超过0人
                        if (getDeadsBySide(左边) > 0) {
                            win_result = 右边
                        }
                        break
                    case 9: // 4个回合内战斗胜利
                        if (round >= 5) {
                            win_result = 右边
                        }
                        break
                    case 10: // 我方总血量高于80%
                        if (getLifeRatioBySide(左边) < 0.8) {
                            win_result = 右边
                        }
                        break
                }
            } else if (win_result == 2) {

            } else {
                log.error("***** 发生错误,中途停止 *****")
                win_result = -1
            }

            frb.win = win_result
            frb.actions = actions
        } catch (Exception ex) {
            log.error("sim battle.Run error:" + ex.getMessage() + " stack:" + ex.getStackTrace().toString())
            throw ex
        }

        return frb.win
    }

    /**
     * 当前生命百分比
     * @param side
     * @return
     */
    private double getLifeRatioBySide(int side) {
        List<Roler> his = getTeam(side)

        long now = 0
        long total = 0
        for (Roler fh : his) {
            now += fh.life
            total += fh.max_life
        }
        return now / total
    }

    /**
     * 死亡人数
     * @param side
     * @return
     */
    private int getDeadsBySide(int side) {
        List<Roler> his = getTeam(side)
        long now = 0
        for (def fh : his) {
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
    private boolean checkRoundCanAct(Roler _hi) {
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
    private int doAction(Roler _hi) {
        try {
            _hi.checkBuffsRoundBefore(round)

            if (!checkRoundCanAct(_hi)) {
//                logger.warn("+++ 英雄：" + _hi.name + " 被眩晕了，本回合:" + round + " 无法行动")
                _hi.checkBuffsRoundEnd(round)
                return 0
            }

            int heroId = _hi.getRealHero()
            if (heroId < 100) { // 传奇英雄
                LegendHero lh = readonly.findLegendHero(heroId)
                makeLegendHarm(_hi, lh)
            } else {
                boolean jump = false
                if (_hi.moral < 0) {
                    double ratio = _hi.moral / (_hi.moral - 100.0)
                    if (randDouble() <= ratio) {
                        // 士气低落
                        current_action.addEffectHarm(_hi, BuffInfo.士气变动, -1, false)
                        jump = true
                    }
                }

                if (!jump) {
                    HeroSkillConf hsb = readonly.findSkill(heroId)
                    // 1. 检查当前怒气值，判断是否应该使用普攻
                    if (_hi.anger < (4 + _hi.anger_extra)) {
                        // 普通攻击
                        _hi.addAnger(2)
                        // 普通攻击多恢复2点怒气
                        current_action.attackMode = 1
                        current_action.anger = _hi.anger
                        makeHarm(_hi, hsb.attack1eff, _hi.fate_skill_up[0])
                    } else {
                        boolean fee = true // 是否消耗怒气
                        // 特殊攻击
                        if (_hi instanceof 索隆) {
                            HeroSkillConf hc = readonly.findSkill(_hi.id)
                            if (getRandom() <= hc.skillzeff[0]) {
                                fee = false
                            }
                        }

                        if (fee) {
                            _hi.addAnger(-(4 + _hi.anger_extra))
                        }

                        current_action.anger = _hi.anger
                        // 根据当前英雄可以释放的技能，顺序检测:
                        // 超级组合技>组合技>怒气技
                        if (_hi.super_group_skill) {
                            current_action.attackMode = 4
                            makeHarm(_hi, hsb.attack4eff, _hi.fate_skill_up[3])
                        } else if (_hi.group_skill) {
                            current_action.attackMode = 3
                            makeHarm(_hi, hsb.attack3eff, _hi.fate_skill_up[2])
                        } else {
                            current_action.attackMode = 2
                            makeHarm(_hi, hsb.attack2eff, _hi.fate_skill_up[1])
                        }
                    }


                    if (_hi.moral > 0 && !_hi.alreadyMoralAction) {
                        double ratio = _hi.moral / (100.0 + _hi.moral)
                        if (randDouble() <= ratio) {
                            // 士气高涨
                            _hi.isActiond = false
                            current_action.addEffectHarm(_hi, BuffInfo.士气变动, 1, false)
                            _hi.alreadyMoralAction = true
                        }
                    }
                }
            }
        } catch (GameOverException goe) {
            return goe.getSide()
        } catch (Exception ex) {
            log.error("英雄行动错误，当前所在回合:" + round + " 英雄:" + _hi.toString() + " 原因:", ex)
            return 3
        }

        _hi.checkBuffsRoundEnd(round)
        return 0
    }

    /**
     * 检查 side 所在的队伍是否死光
     *
     * @param side
     * @return
     */
    int checkOver(int side) {
        if (side == 右边) {
            for (def hi : right) {
                if (!hi.isDead()) {
                    return 0
                }
            }
            return 1
        } else {
            for (def hi : left) {
                if (!hi.isDead()) {
                    return 0
                }
            }
            return 2
        }
    }

    /**
     * 处理传奇英雄的攻击
     * @param _hi
     * @param lh
     * @throws GameOverException
     */
    private void makeLegendHarm(Roler _hi, LegendHero lh) throws GameOverException {
        List<Roler> peers = null

//        println("+++回合[" + round + "]索引[" + round_index + "] 英雄(" + _hi.name + "): 本次暴击提升(" +  + ") 命中提升(" + eh_aim + "). 基本攻击力:" + _hi.attack)
        current_action.skillMode = 0 // 传奇英雄不用告知攻击是群攻 单体还是什么
        // 计算本次攻击伤害
        long h = (long) _hi.attack

        switch (lh.mode) {
            case 1:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.前列单体, 0)
                break
            case 2:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体敌方, 0)
                break
            case 3:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.前列敌方, 0)
                break
            case 4:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.一行敌方, 0)
                break
            case 5:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机3个敌方, 0)
                break
            case 6:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个敌方, 0)
                break
            case 7:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机1个敌方, 0)
                break
            case 8:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.相邻敌方, 0)
                break
            case 9:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.后列敌方, 0)
                break
            default:
                peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体敌方, 0)
                break
        }
        if (peers != null) {
            for (def peer : peers) {
                doLegendHarm(h, _hi, peer)
            }
        }

        current_action.skillMode = 0 // 特殊表示传奇英雄是否激活了他的特技
        int[] eff = lh.bateff
        // 根据不同的传奇英雄进行特殊处理
        switch (_hi.id) {
            case 1: // 随机释放烈火神盾
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机友军, eff[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 烈火神盾Buff(fh, eff[3], eff[2]))
                            }
                        }
                    }
                }
                break
            case 2: // 反击
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机友军, eff[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 反戈一击Buff(fh, eff[4], eff[2], eff[3]))
                            }
                        }
                    }
                }
                break
            case 3: // 嗜血狂野
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机友军, eff[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 嗜血狂野Buff(fh, eff[5], eff[2], eff[3], eff[4]))
                            }
                        }
                    }
                }
                break
            case 4: // 祈祷
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机友军, eff[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 祈祷Buff(fh, eff[4], eff[2], eff[3]))
                            }
                        }
                    }
                }
                break
            case 5: // 护体神盾
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机友军, eff[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new Shield2Buff(fh, eff[2], 1))
                            }
                        }
                    }
                }
                break
            case 6: // 霹雳闪电+眩晕
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机敌军, eff[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.fixLife(null, -eff[2], BuffInfo.霹雳闪电, false)
                                fh.addBuff(new 眩晕Buff(fh, eff[3]))
                            }
                        }
                    }
                }
                break
            case 7: // 流星火雨
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机敌军, eff[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.fixLife(null, -eff[2], BuffInfo.流星火雨, false)
                                fh.addBuff(new 速度降低Buff(fh, eff[4], eff[3]))
                            }
                        }
                    }
                }
                break
            case 8: // 悲痛欲绝
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机敌军, eff[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 士气降低Buff(fh, eff[2], eff[1]))
                            }
                        }
                    }
                }
                break
            case 9: // 欢欣鼓舞
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机友军, eff[1])
                    if (peers != null) {
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.addBuff(new 士气提高Buff(fh, eff[2], eff[1]))
                            }
                        }
                    }
                }
                break
            case 10: // 连锁闪电
                // 计算触发概率
                if (getRandom() <= eff[0]) {
                    current_action.skillMode = 1

                    peers = findPeer(_hi.side, 0, FindPeerEnum.随机敌军, eff[1])
                    if (peers != null) {
                        long harm = eff[2]
                        for (def fh : peers) {
                            if (!fh.isDead()) {
                                fh.fixLife(null, -harm, BuffInfo.连锁闪电, false)
                                if (!fh.isDead()) {
                                    fh.addAnger((int) -eff[3])
                                    current_action.addEffectHarm(fh, BuffInfo.怒气控制, fh.anger, false)
                                }
                            }
                            harm = (harm / 2) as long
                        }
                    }
                }
                break
            case 11: // 王者祝福
                // 计算触发概率
                current_action.skillMode = 1
                peers = findPeer(_hi.side, 0, FindPeerEnum.随机友军, eff[0])
                if (peers != null) {
                    for (def fh : peers) {
                        if (!fh.isDead()) {
                            fh.addBuff(new 王者祝福Buff(fh, eff[3], eff[1], eff[2]))
                        }
                    }
                }
                break
        }
    }

    /**
     * 攻击效果应用到本次攻击上
     *
     * @param _hi
     * @param effs
     */
    private void makeHarm(Roler _hi, int[][] effs, double skillUp) throws GameOverException {
        List<Roler> peers = null

        /**
         * 501,502,503 只临时提高本次攻击的暴击和命中，在本次doHarm结束后还原
         */
        int eh_crit = 0
        int eh_aim = 0

//        println("+++回合[" + round + "]索引[" + round_index + "] 英雄(" + _hi.name + "): 本次暴击提升(" + eh_crit + ") 命中提升(" + eh_aim + "). 基本攻击力:" + _hi.attack + " 怒气:" + _hi.anger)

        current_action.skillMode = effs[0][0]
        for (int[] eff : effs) {
            switch (eff[0]) {
                case 1:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.前列单体, 0)
                    if (peers == null) {
                        log.error("side:" + _hi.side + " index:" + _hi.index + " 前列单体没找到")
                        if (_hi.side == 2) {
                            String err = ""
                            if (left.size() == 0) {
                                err = "己方没有部队"
                            } else {
                                for (int ii = 0; ii < left.size(); ++ii) {
                                    def fh = left.get(ii)
                                    if (fh != null) {
                                        err += fh.toString() + "\n"
                                    }
                                }
                            }
                            println(err)
                        }
                    } else {
                        for (def peer : peers) {
                            doHarm(harm, _hi, peer)
                        }
                    }
                    break
                case 2:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体敌方, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 3:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.前列敌方, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 4:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.一行敌方, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 5:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机3个敌方, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 6:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个敌方, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 7:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机1个敌方, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 8:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.相邻敌方, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 9:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.后列敌方, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 10:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.后列单体, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 11:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.生命最少敌方, 0)
                    for (def peer : peers) {
                        doHarm(harm, _hi, peer)
                    }
                    break
                case 12:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.生命最少己方, 0)
                    for (def peer : peers) {
                        doCure(harm, _hi, peer, BuffInfo.治疗恢复)
                    }
                    break
                case 13:
                    // 计算本次攻击伤害
                    long harm = (long) _hi.attack * (eff[1] + skillUp) / 100
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方, 0)
                    for (def peer : peers) {
                        doCure(harm, _hi, peer, BuffInfo.群疗恢复)
                    }
                    break
            /* 下面一组的效果，根据上面一组获取的目标和伤害，是否闪避来处理 */

                case 101: // 概率减少目标怒气
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.addAnger((int) -eff[2])
                                current_action.addEffectHarm(peer, BuffInfo.怒气控制, peer.anger, false)
                            }
                        }
                    }
                    break
                case 102: // 概率眩晕
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.addBuff(new 眩晕Buff(peer, eff[2]))
                            }
                        }
                    }
                    break
                case 103: // 概率灼烧
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                long harm = peer.tempHarm * eff[2] / 100
                                peer.addBuff(new 灼烧Buff(peer, eff[3], harm))
                            }
                        }
                    }
                    break
                case 104: // 概率中毒
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                long harm = peer.tempHarm * eff[2] / 100
                                peer.addBuff(new 中毒Buff(peer, eff[3], harm))
                            }
                        }
                    }
                    break
                case 105: // 概率降低防御
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.addBuff(new 降防Buff(peer, eff[3], eff[2]))
                            }
                        }
                    }
                    break
                case 106: // 概率降低攻击
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.addBuff(new 降攻Buff(peer, eff[3], eff[2]))
                            }
                        }
                    }
                    break
                case 107: // 概率易伤
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.addBuff(new 易伤Buff(peer, eff[3], eff[2]))
                            }
                        }
                    }
                    break
                case 108: // 本次攻击的目标如果是敌方生命值最低的单位，触发该效果
                    List<Roler> heros = getTeam(_hi.side)

                    // 1. 找到敌方所有生命值最低的
                    Optional<Roler> opt = heros.stream().min({ a, b ->
                        try {
                            if (a.life <= 0) {
                                return 1
                            }

                            float al = a.life / a.max_life
                            float bl = b.life / b.max_life

                            if (al < bl) {
                                return -1
                            } else if (al > bl) {
                                return 1
                            }
                        } catch (Exception e) {
                            log.error("Exception:" + e.getMessage())
                        }
                        return 0
                    } as Comparator<? super Roler>)

                    def minest = opt.orElse(null)
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer == minest) {
                            if (peer != null) { // 找到最小的生命值比例的人
                                long h = peer.tempHarm * eff[1] / 100
                                if (h > 0) {
                                    peer.fixLife(null, -h, BuffInfo.生命比例最低攻击, false)
                                }
                            }
                        }
                    }
                    break
                case 109: // 清除debuff
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        peer.delBadBuff()
                    }
                    break
                case 110: // 清除buff
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        peer.delGoodBuff()
                    }
                    break
                case 111: //减伤Buff
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            if (getRandom() <= eff[1]) {
                                peer.addBuff(new 减伤Buff(peer, eff[3], eff[2]))
                            }
                        }
                    }
                    break
                case 112: // 清除灼烧
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        peer.delAll灼烧()
                    }
                    break
                case 113: // 对生命低于百分比的友军额外治疗
                    for (def h : peers) {
                        if (!h.isDead()) {
                            float ratio = h.life * 100f / h.max_life
                            if (ratio < (float) eff[1]) {
                                long harm = (long) h.tempHarm * eff[2] / 100
                                h.fixLife(null, harm, BuffInfo.治疗恢复, false)
                            }
                        }
                    }
                    break
                case 114: // 每回合恢复治疗量
                    for (def peer : peers) {
                        if (peer.isDead()) {
                            continue
                        }
                        if (peer.tempHarm > 0) {
                            long harm = peer.tempHarm * eff[1] / 100
                            peer.addBuff(new 治疗Buff(peer, eff[2], harm))
                        }
                    }
                    break

            /* 下面的效果都是自带目标，需要重新定位目标，不要再使用攻击默认目标 */

            // 随机友军

                case 200:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机1个己方, 0)
                        for (def peer : peers) {
                            if (!peer.isDead()) {
                                peer.addAnger((int) eff[2])
                                current_action.addEffectHarm(peer, BuffInfo.怒气控制, peer.anger, false)
                            }
                        }
                    }
                    break
                case 201:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方, 0)
                        for (def peer : peers) {
                            if (!peer.isDead()) {
                                peer.addAnger((int) eff[2])
                                current_action.addEffectHarm(peer, BuffInfo.怒气控制, peer.anger, false)
                            }
                        }
                    }
                    break
                case 202:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方, 0)
                        for (def peer : peers) {
                            peer.addBuff(new 加伤Buff(peer, eff[3], eff[2]))
                        }
                    }
                    break
                case 203:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方, 0)
                        for (def peer : peers) {
                            peer.addBuff(new 难伤Buff(peer, eff[3], eff[2]))
                        }
                    }
                    break
                case 204: // 概率恢复自身怒气
                    if (!_hi.isDead() && getRandom() <= eff[1]) {
                        _hi.addAnger((int) eff[2])
                        current_action.addEffectHarm(_hi, BuffInfo.怒气控制, _hi.anger, false)
                    }
                    break
                case 205:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方, 0)
                        for (def peer : peers) {
                            peer.addBuff(new 加防Buff(peer, eff[3], eff[2]))
                        }
                    }
                    break
                case 206:
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机2个己方, 0)
                        for (def peer : peers) {
                            peer.addBuff(new 攻击提高Buff(peer, eff[3], eff[2]))
                        }
                    }
                    break
                case 207: // 随机三个友军受到伤害降低，难伤Buff +3
                    if (getRandom() <= eff[1]) {
                        // 计算本次攻击伤害
                        peers = findPeer(_hi.side, _hi.index, FindPeerEnum.随机3个己方, 0)
                        for (def peer : peers) {
                            peer.addBuff(new 难伤Buff(peer, eff[3], eff[2]))
                        }
                    }
                    break
            // 全体友军
                case 301: // 全体友军命中提高
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方, 0)
                    for (def peer : peers) {
                        peer.addBuff(new 命中提高Buff(peer, eff[2], eff[1]))
                    }
                    break
                case 302: // 全体友军抗暴提高
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方, 0)
                    for (def peer : peers) {
                        peer.addBuff(new 抗暴提高Buff(peer, eff[2], eff[1]))
                    }
                    break
                case 303: // 全体友军暴击,命中提高Buff
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方, 0)
                    for (def peer : peers) {
                        peer.addBuff(new 命中提高Buff(peer, eff[2], eff[1]))
                        peer.addBuff(new 暴击提高Buff(peer, eff[2], eff[1]))
                    }
                    break
                case 304: // 全体友军收到伤害减少，难伤Buff
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方, 0)
                    for (def peer : peers) {
                        peer.addBuff(new 难伤Buff(peer, eff[2], eff[1]))
                    }
                    break
                case 305: // 全体友军攻击提高
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方, 0)
                    for (def peer : peers) {
                        peer.addBuff(new 攻击提高Buff(peer, eff[2], eff[1]))
                    }
                    break
                case 306: // 全体友军攻击提高
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方, 0)
                    for (def peer : peers) {
                        peer.addBuff(new 闪避提高Buff(peer, eff[2], eff[1]))
                    }
                    break
                case 307: // 全体友军 造成伤害提高，加伤Buff
                    peers = findPeer(_hi.side, _hi.index, FindPeerEnum.全体己方, 0)
                    for (def peer : peers) {
                        peer.addBuff(new 加伤Buff(peer, eff[2], eff[1]))
                    }
                    break
                case 401:
                    _hi.addBuff(new 无敌(_hi, eff[1]))
                    break
            // 自身相关
                case 402: // 自身闪避提高
                    _hi.addBuff(new 闪避提高Buff(_hi, eff[2], eff[1]))
                    break
                case 403: // 自身伤害提高
                    _hi.addBuff(new 加伤Buff(_hi, eff[2], eff[1]))
                    break
                case 404: // 自身受到伤害减少
                    _hi.addBuff(new 难伤Buff(_hi, eff[2], eff[1]))
                    break
                case 501: // 暴击提高Buff
                    eh_crit += eff[1]
                    _hi.crit += eff[1]
                    break
                case 502: // 命中提高Buff
                    eh_aim += eff[1]
                    _hi.aim += eff[1]
                    break
                case 503: // 暴击命中提高
                    eh_crit += eff[1]
                    eh_aim += eff[1]

                    _hi.crit += eff[1]
                    _hi.aim += eff[1]
                    break
                default:
                    log.error("xxxxxx doHarm中的攻击效果:" + eff[0] + " 不认识，请查看是否更新正确的类型.")
            }
        }

        _hi.crit += eh_crit
        _hi.aim += eh_aim
    }

    /**
     * 传奇英雄攻击
     * @param baseHarm
     * @param self
     * @param peer
     * @throws GameOverException
     */
    private void doLegendHarm(double baseHarm, Roler self, Roler peer) throws GameOverException {
        if (self.life <= 0 || peer.life <= 0) {
            return
        }

//      String msg = "|--- 攻击方:" + self.name + " 被攻击:" + peer.name + " 基础攻击力:" + baseHarm

        boolean crit = false
        double h = baseHarm

        // 计算暴击率
        int critical = self.crit - peer.anti_crit
        if (critical > 0) {
            if (getRandom1000() <= critical) {
                // 4. 计算暴击伤害
                h = (int) ((150 + self.crit_harm) * h / 100f)
                crit = true
            }
        }

        if (h <= 0) {
            h = 0
        }
        peer.tempHarm = h

        long realHarm = (long) h
        peer.fixLife(null, -realHarm, BuffInfo.攻击伤害, crit)
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
    void doHarm(double baseHarm, Roler self, Roler peer) throws GameOverException {
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

        baseHarm = self.beforeDoHarm(baseHarm)

        double h = baseHarm
        if (!isdodge) {
            // 2. 计算伤害
            // 根据攻击方的模式，选择被攻击方的防御属性

            // 2.1 概率忽视对方防御
            boolean nodef = false
            if (self.force_attack > 0) {
                if (getRandom() <= self.force_attack) {
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
            //h = h * (r % 10 + 95) / 100

            /**
             * er = (增伤 - 减伤Buff + 易伤Buff - 难伤Buff)
             **/
            // 针对不同的攻击类型 减少伤害
            float decrease = 0;
            if (self.mode == 物理攻击 || self.mode == 防御攻击) {
                if (peer.phy_lower != 0) {
                    decrease = peer.phy_lower / 100f;
                    if (decrease > 1) {
                        decrease = 1
                    }
                }
            } else {
                if (peer.mag_lower != 0) {
                    decrease = peer.mag_lower / 100f;
                    if (decrease > 1) {
                        decrease = 1
                    }
                }
            }

            float er = self.en_harm
            if (peer.over_harm > 0 && nodef) { // 难伤, 无视防御的时候，难伤去掉
            } else {
                er -= peer.over_harm
            }
            msg += " 攻击者增伤(" + self.en_harm + ")被击者减伤(" + peer.over_harm + ") "

            /**
             * h = h * (1 + er/(1000 + |er|)
             */
            // 还的判断 self 对 peer阵营是否有加成
            if (peer.sect > 0) {
                h = (h * (1f + er / (Math.abs(er) + 1000f)) * (self.sect_harm_up[peer.sect - 1] + peer.harm_up - decrease))
            } else {
                h = (h * (1f + er / (Math.abs(er) + 1000f)) * (1 + peer.harm_up - decrease))
            }

            msg += " 难伤易伤后攻击力(" + h + ")er(" + er + ") "

            // 3. 计算暴击率
            int critical = self.crit - peer.anti_crit
            if (critical > 0) {
                if (getRandom1000() <= critical) {
                    // 4. 计算暴击伤害
                    h = (int) ((150 + self.crit_harm) * h / 100f)
                    crit = true
                    msg += " 暴击(" + h + ") "
                }
            }

            if (self.multi_harm_ratio > 0) {
                if (getRandom() <= self.multi_harm_ratio) {
                    h *= 2
                    msg += " [双倍伤害] "
                }
            }

            // 5. 增加伤害与易伤系数, 检测各种buff与debuff
            for (BuffInfo buff : self.buffs) {
                if (!buff.deleted) {
                    h = buff.whenAttack(h)
                }
            }

            if (h < 1) {
                h = 1
            }
        } else {
            h = 0
        }

        msg += " 本次攻击伤害[" + h + "]"
        if (!isdodge) {
            if (h > 0) {
                for (BuffInfo buff : peer.buffs) {
                    if (!buff.deleted) {
                        h = buff.whenBeAttack(h)
                    }
                }
            }

            h = self.onHarm(peer, h, crit)
            if (h < 0) { // 特殊技能闪避
                isdodge = true
                current_action.addEffectHarm(peer, BuffInfo.攻击躲避, 0, crit)
                h = 0
            }
        } else {
            current_action.addEffectHarm(peer, BuffInfo.攻击躲避, 0, crit)
        }
        peer.tempHarm = h

        boolean dead = peer.isDead()
        if (!dead && !isdodge) {
            if (peer.draw_ratio > 0) {
                int r = getRandom()
                msg += " [对方吸血概率](" + peer.draw_life + ")(" + peer.draw_ratio + ") "
                if (r <= peer.draw_ratio) {
                    long hh = (peer.draw_life * peer.max_life / 100)
                    if (hh > 0) {
                        peer.fixLife(null, hh, BuffInfo.治疗恢复, false)
                        msg += " [对方吸血](" + hh + ") "
                    }
                }
            }

            if (self.mode == 物理攻击 || self.mode == 防御攻击) {
                if (peer.reflect_ratio > 0) {
                    int r = getRandom()
                    if (r < peer.reflect_ratio) {
                        long hh = h * peer.reflect_percent / 100
                        if (hh > 0) {
                            self.fixLife(null, -hh, BuffInfo.攻击伤害, false)
                            msg += " [对方反伤](" + hh + ") "
                        }
                    }
                }
            } else {
                if (peer.m_reflect_ratio > 0) {
                    int r = getRandom()
                    if (r < peer.m_reflect_ratio) {
                        long hh = h * peer.m_reflect_percent / 100
                        if (hh > 0) {
                            self.fixLife(null, -hh, BuffInfo.攻击伤害, false)
                            msg += " [对方反伤](" + hh + ") "
                        }
                    }
                }
            }
        }

        self.afterDoHarm(peer, h, isdodge, dead, crit)

        peer.afterPunch(self, h, isdodge, dead)

        // 攻击后, 可能反戈一击
        for (BuffInfo buff : peer.buffs) {
            if (!buff.deleted) {
                buff.afterBeAttack(self, h)
                if (peer.isDead()) {
                    break
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
    private void doCure(long baseHarm, Roler self, Roler peer, int mode) throws GameOverException {
        // 1. 计算伤害
        // 根据攻击方的模式，选择被攻击方的防御属性
        long h = baseHarm

        h = h * (getRandom() % 10 + 95) / 100

        // 3. 计算暴击率
        int critical = self.crit - peer.anti_crit
        boolean crit = false
        if (critical > 0) {
            if (getRandom1000() <= critical) {
                crit = true
                // 4. 计算暴击伤害
                h = (150 + self.crit_harm) * h / 100
            }
        }

        if (self.multi_harm_ratio > 0) {
            if (getRandom() <= self.multi_harm_ratio) {
                h *= 2
            }
        }
        peer.tempHarm = h
        peer.fixLife(null, h, mode, crit)

        self.afterDoHarm(peer, h, false, false, crit)
    }

    /**
     * 根据当前阵营，站位，和攻击目标的模式，得到所有目标
     *
     * @param side 阵营 1左边 2右边
     * @param index 站位 [0,5]
     * @param fpe 攻击目标的模式
     * @param param 随机友军或者敌军的具体个数 其他fpe不影响
     * @return 最后需要攻击的目标列表
     * <p>
     * 如果返回@null 表示当前所在的side胜利，对方已经没有可以攻击的对象了
     */
    List<Roler> findPeer(int side, int index, FindPeerEnum fpe, int param) {
        index %= 6  // index 有可能为6 此时是传奇英雄攻击,选择目标当作他是0号位置的人进行选择
        List<Roler> self = null
        List<Roler> other = null
        if (side == 1) {
            other = right
            self = left
        } else {
            other = left
            self = right
        }

        switch (fpe) {
            case FindPeerEnum.前列单体:
                Roler hi = findOne(other, index)
                if (hi != null) {
                    List<Roler> rtn = new ArrayList<>()
                    rtn.add(hi)
                    return rtn
                }
                return null
            case FindPeerEnum.后列单体: // 优先后列
                Roler hi = findOneReverse(other, index)
                if (hi != null) {
                    List<Roler> rtn = new ArrayList<>()
                    rtn.add(hi)
                    return rtn
                }
                return null
            case FindPeerEnum.全体敌方:
                List<Roler> rtn = new ArrayList<>()
                for (Roler hi : other) {
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
            case FindPeerEnum.随机敌军:
                return findRandomPeer(other, param)
            case FindPeerEnum.随机1个敌方:
                return findRandomPeer(other, 1)
            case FindPeerEnum.随机2个敌方:
                return findRandomPeer(other, 2)
            case FindPeerEnum.随机3个敌方:
                return findRandomPeer(other, 3)
            case FindPeerEnum.随机友军:
                return findRandomPeer(self, param)
            case FindPeerEnum.随机1个己方:
                return findRandomPeer(self, 1)
            case FindPeerEnum.随机2个己方:
                return findRandomPeer(self, 2)
            case FindPeerEnum.随机3个己方:
                return findRandomPeer(self, 3)
            case FindPeerEnum.生命最少己方:
                return findMinLife(self)
            case FindPeerEnum.全体己方:
                List<Roler> rtn = new ArrayList<>()
                for (Roler hi : self) {
                    if (!hi.isDead()) {
                        rtn.add(hi)
                    }
                }
                if (rtn.size() <= 0) {
                    return null
                }
                return rtn
            case FindPeerEnum.单体己方:
                List<Roler> rtn = new ArrayList<>()
                rtn.add(self.get(index))
                return rtn
            default:
                log.error("findPeer 查找对手的方式无法识别: " + fpe.toString())
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
    private Roler findOne(List<Roler> other, int index) {
        int idx = getFirstColumn(index)
        if (idx < other.size()) {
            Roler hi = other.get(idx)
            if (!hi.isDead()) {
                return hi
            }
        }

        // 在第一列，找可以攻击的目标
        for (int i = 0; i < 3; ++i) {
            if (i != idx && i < other.size()) {
                Roler hi = other.get(i)
                if (!hi.isDead()) {
                    return hi
                }
            }
        }

        idx = getSecondColumn(index)

        if (idx < other.size()) {
            Roler hi = other.get(idx)
            if (!hi.isDead()) {
                return hi
            }
        }

        // 在第一列，找可以攻击的目标
        for (int i = 3; i < 6; ++i) {
            if (i != idx && i < other.size()) {
                Roler hi = other.get(i)
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
    private Roler findOneReverse(List<Roler> other, int index) {
        int idx = getSecondColumn(index)
        if (idx < other.size()) {
            Roler hi = other.get(idx)
            if (!hi.isDead()) {
                return hi
            }
        }

        // 在后列，找可以攻击的目标
        for (int i = 3; i < 6; ++i) {
            if (i != idx && i < other.size()) {
                Roler hi = other.get(i)
                if (!hi.isDead()) {
                    return hi
                }
            }
        }

        idx = getFirstColumn(index)
        if (idx < other.size()) {
            Roler hi = other.get(idx)
            if (!hi.isDead()) {
                return hi
            }
        }

        // 在前列，找可以攻击的目标
        for (int i = 0; i < 3; ++i) {
            if (i != idx && i < other.size()) {
                Roler hi = other.get(i)
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
    private List<Roler> findMinLife(List<Roler> other) {
        long min = 99999999999L
        Roler rtn = null
        for (Roler hi : other) {
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

        List<Roler> tmp = new ArrayList<>()
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
    private List<Roler> findRandomPeer(List<Roler> other, int number) {
        List<Roler> rtn = other.parallelStream().filter({ t -> !t.isDead() }).collect(Collectors.toList())
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
    private List<Roler> findByCol(List<Roler> other, int 优先) {
        List<Roler> rtn = new ArrayList<>()
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

    /**
     * 搜寻第一列
     * @param other
     * @param rtn
     * @return
     */
    private boolean findFirstCol(List<Roler> other, List<Roler> rtn) {
        boolean flag = false
        int length = Math.min(3, other.size())
        for (int i = 0; i < length; ++i) {
            Roler hi = other.get(i)
            if (hi != null && !hi.isDead()) {
                rtn.add(hi)
                flag = true
            }
        }
        return flag
    }

    private boolean findSecondCol(List<Roler> other, List<Roler> rtn) {
        boolean flag = false
        if (other.size() > 3) {
            int length = Math.min(6, other.size())
            for (int i = 3; i < length; ++i) {
                Roler hi = other.get(i)
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
    private List<Roler> findAdjacent(List<Roler> other, int index) {
        List<Roler> rtn = new ArrayList<>()
        Roler hi = findOne(other, index)
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
    private List<Roler> findByRow(List<Roler> other, int index) {
        List<Roler> rtn = new ArrayList<>()

        int idx = index
        if (index >= 3) {
            idx = index - 3
        }

        // 查找本行
        boolean flag = false
        if (idx < other.size()) {
            Roler hi = other.get(idx)
            if (!hi.isDead()) {
                rtn.add(hi)
                flag = true
            }
        }

        if (flag) {
            if (idx + 3 < other.size()) {
                Roler hi = other.get(idx + 3)
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
                    Roler hi = other.get(i)
                    if (!hi.isDead()) {
                        rtn.add(hi)
                        flag = true
                    }
                }

                if (flag) {
                    if (i + 3 < other.size()) {
                        Roler hi = other.get(i + 3)
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
            Roler hi = other.get(idx + 3)
            if (!hi.isDead()) {
                rtn.add(hi)
                return rtn
            }
        }

        for (int i = 0; i < 3; ++i) {
            if (i != idx) {
                if (i + 3 < other.size()) {
                    Roler hi = other.get(i + 3)
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
        for (Roler hi : right) {
            if (hi != null) {
                lifes.add((long) hi.life)
            }
        }
        return lifes
    }
}

/**
 * 战斗角色
 */
class Roler {
    /**
     * 战斗索引
     */
    GroovyFightSystem battle
    /***
     * 武装
     */
    int wuz
    /**
     * 英雄id
     */
    int id
    /**
     * 英雄名
     */
    String name
    /**
     * 突破等级
     */
    int grade
    /**
     * 1 左边
     * 2 右边
     */
    int side
    /**
     * 位置索引
     */
    int index
    /**
     * 攻击模式
     */
    int mode
    /**
     * 阵营
     */
    int sect
    /**
     * 当前皮肤, >0 就是有皮肤了，或者变成了红色
     */
    int skin
    /**
     * 生命
     */
    double max_life

    /**
     * 四维
     */
    double life
    double attack
    double defs
    double mdef

    /**
     * 暴击率 /1000
     */
    double crit
    /**
     * 抗暴击率 /1000
     */
    double anti_crit
    /**
     * 闪避率 /1000
     */
    double dodge
    /**
     * 命中率 /1000
     */
    double aim
    /**
     * 易伤比例 /1000  自己的属性
     * 难伤是负数 负数越多越难伤
     */
    double over_harm
    /**
     * 增伤, 对敌人的属性 /1000
     */
    double en_harm
    /**
     * 物理伤害降低率 /100
     */
    double phy_lower
    /**
     * 魔法伤害降低率 /100
     */
    double mag_lower
    /**
     * 暴击伤害
     */
    double crit_harm
    /**
     * 怒气
     */
    int anger = 1
    /**
     * 回怒
     */
    int anger_cover
    /**
     * 每次消耗更多的怒气
     */
    int anger_extra
    /**
     * 概率忽视防御 /100
     */
    double force_attack
    /**
     * 高倍伤害几率 /100
     */
    double multi_harm_ratio
    /**
     * 是否激活组合技
     */
    boolean group_skill
    /**
     * 是否激活超级组合技
     */
    boolean super_group_skill
    /**
     * 天命激活的技能提升
     */
    double[] fate_skill_up = [0, 0, 0, 0]
    /**
     * 特殊阵营的英雄提升
     */
    double[] sect_harm_up = [1.0, 1.0, 1.0, 1.0]
    /**
     * 阵营提升的攻击
     */
    double harm_up
    /**
     * 速度
     */
    int speed = 1000
    /**
     * 士气
     */
    int moral
    /**
     * 物理反伤几率
     */
    double reflect_ratio
    /**
     * 物理反伤比例
     */
    double reflect_percent
    /**
     * 魔法反伤几率
     */
    double m_reflect_ratio
    /**
     * 魔法反伤比例
     */
    double m_reflect_percent
    /**
     * 攻击回血
     */
    double draw_life
    /**
     * 攻击回血几率
     */
    double draw_ratio
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
     * 1 眩晕 2 石化
     */
    int forbidAction

    int getRealHero() {
        if (skin > 1) {
            return skin
        }
        return id
    }

    void addAnger(int _anger) {
        anger += _anger
        if (anger < 0) {
            anger = 0
        } else if (anger > 8) {
            anger = 8
        }
    }

    void addMoral(int _moral) {
        moral += _moral
    }

    /**
     * 本回合是否行动过
     */
    boolean isActiond = false

    /**
     * 本回合是否士气高涨过
     */
    boolean alreadyMoralAction = false

    Roler(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        this.battle = battle
        this.side = side
        this.index = idx
        HeroConf hc = battle.readonly.findHero(hb.hero)
        this.id = hc.key
        this.sect = hc.sect
        this.mode = hc.type
        this.name = hc.name
        this.grade = hb.grade

        // +++ 基础
        int[] gf = hc.growfight
        this.max_life += gf[0]
        this.attack += gf[1]
        this.defs += gf[2]
        this.mdef += gf[3]

        // +++ 突破
        int[] af = hc.attup[hb.grade]
        int level = hb.level - 1
        this.max_life += level * af[0]
        this.attack += level * af[1]
        this.defs += level * af[2]
        this.mdef += level * af[3]

        // +++ 阵型增强
        if (lineup != 0) {
            LineupConf lc = battle.readonly.findLineup(idx)
            if (lc != null) {
                battle.deal(this, lc.lineatt[0], lc.lineatt[1] * lineup)
            }
        }

        // +++ 皮肤
        if (skin > 0) { // 领主带皮肤
            MaskConf mc = battle.readonly.findMask(skin)
            // 从皮肤id切到对应模拟的英雄id
            skin = mc.hero

            // 赋值皮肤属性
            for (int i = 0; i < mc.basic.length; i += 2) {
                battle.deal(this, mc.basic[i], mc.basic[i + 1])
            }

            skin_level = skin_level - 1
            if (skin_level > 0) {
                this.attack += mc.stratt[0] * skin_level
                this.max_life += mc.stratt[1] * skin_level
                this.defs += mc.stratt[2] * skin_level
                this.mdef += mc.stratt[3] * skin_level
            }

            for (int[] bs : mc.bonus) {
                if (skin_level > bs[0]) {
                    battle.deal(this, bs[1], bs[2])
                }
            }
        } else {
            if (hb.gift >= 26) {
                skin = 1
            }
        }

        /**
         * 1. 领主穿皮肤，skin标识对应的英雄的id
         * 2. 英雄金色觉醒到红色，则skin标识为1，标识已经觉醒
         */
        this.skin = skin

        // +++ 觉醒英雄需要增加新的提升
        if (hb.gift > 0) {
            AwakenConf lb = null
            if (hb.gift > 1) {
                lb = battle.readonly.findAwaken(hb.gift - 1)
            }
            AwakenConf ab = battle.readonly.findAwaken(hb.gift)

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
            if (hb.giftrunes[0] != 0) { // 攻击, 本级激活
                if (mode == GroovyFightSystem.物理攻击 || mode == GroovyFightSystem.法术攻击) {
                    attack += level * (fwcost1[0] + ab.levelup1[0])
                } else {
                    attack += level * (fwcost2[0] + ab.levelup2[0])
                }
            } else { // 本级未激活
                if (mode == GroovyFightSystem.物理攻击 || mode == GroovyFightSystem.法术攻击) {
                    attack += level * (((lb == null) ? 0 : lb.fwcost1[1][0]) + ab.levelup1[0])
                } else {
                    attack += level * (((lb == null) ? 0 : lb.fwcost2[1][0]) + ab.levelup2[0])
                }
            }

            if (hb.giftrunes[1] != 0) { // 血量, 本级激活
                if (mode == GroovyFightSystem.物理攻击 || mode == GroovyFightSystem.法术攻击) {
                    max_life += level * (fwcost1[1] + ab.levelup1[1])
                } else {
                    max_life += level * (fwcost2[1] + ab.levelup2[1])
                }
            } else { // 本级未激活
                if (mode == GroovyFightSystem.物理攻击 || mode == GroovyFightSystem.法术攻击) {
                    max_life += level * (((lb == null) ? 0 : lb.fwcost1[1][1]) + ab.levelup1[1])
                } else {
                    max_life += level * (((lb == null) ? 0 : lb.fwcost2[1][1]) + ab.levelup2[1])
                }
            }

            if (hb.giftrunes[2] != 0) { // 物理防御, 本级激活
                if (mode == GroovyFightSystem.物理攻击 || mode == GroovyFightSystem.法术攻击) {
                    defs += level * (fwcost1[2] + ab.levelup1[2])
                } else {
                    defs += level * (fwcost2[2] + ab.levelup2[2])
                }
            } else { // 本级未激活
                if (mode == GroovyFightSystem.物理攻击 || mode == GroovyFightSystem.法术攻击) {
                    defs += level * (((lb == null) ? 0 : lb.fwcost1[1][2]) + ab.levelup1[2])
                } else {
                    defs += level * (((lb == null) ? 0 : lb.fwcost2[1][2]) + ab.levelup2[2])
                }
            }

            if (hb.giftrunes[3] != 0) { // 魔法防御, 本级激活
                if (mode == GroovyFightSystem.物理攻击 || mode == GroovyFightSystem.法术攻击) {
                    mdef += level * (fwcost1[3] + ab.levelup1[3])
                } else {
                    mdef += level * (fwcost2[3] + ab.levelup2[3])
                }
            } else { // 本级未激活
                if (mode == GroovyFightSystem.物理攻击 || mode == GroovyFightSystem.法术攻击) {
                    mdef += level * (((lb == null) ? 0 : lb.fwcost1[1][3]) + ab.levelup1[3])
                } else {
                    mdef += level * (((lb == null) ? 0 : lb.fwcost2[1][3]) + ab.levelup2[3])
                }
            }
        }

        // +++ 境界等级
        StateupConf sub = battle.readonly.findStateup(hb.fate)
        if (sub != null && sub.bonusatt != null) {
            for (int i = 0; i < sub.bonusatt.length; i += 2) {
                int key = sub.bonusatt[i]
                int val = sub.bonusatt[i + 1]
                battle.deal(this, key, val)
            }
        }

        // 记录套装对应的数量，key: 套装第一个装备的id, val:套装对应共有几个配件满足
        HashMap<Integer, List<Integer>> suit = new HashMap<>()

        // 记录4种共鸣级别
        // 依次为 装备的强化，精炼，宝物的强化，精炼 最小等级
        int[] resonance = [-1, -1, -1, -1]
        // 装备个数
        int e_count = 0
        // 宝物个数
        int t_count = 0

        // +++ 装备各种加成属性
        for (EquipStruct eb : hb.equipStructs) {
            if (eb == null) {
                continue
            }

            EquipConf ebinfo = battle.readonly.findEquip(eb.id)

            for (int jj = 0; jj < ebinfo.base.length; jj += 2) {
                // 2.1 基础属性
                battle.deal(this, ebinfo.base[jj], ebinfo.base[jj + 1])
            }

            for (int jj = 0; jj < ebinfo.str.length; jj += 2) {
                // 2.2 强化属性
                battle.deal(this, ebinfo.str[jj], ebinfo.str[jj + 1] * (eb.level - 1))
            }

            for (int jj = 0; jj < ebinfo.refine.length; jj += 2) {
                // 2.3 精炼属性
                battle.deal(this, ebinfo.refine[jj], ebinfo.refine[jj + 1] * eb.grade)
            }

            //  精炼奖励属性
            for (int[] bonus : ebinfo.bonus) {
                if (eb.grade >= bonus[0]) {
                    battle.deal(this, bonus[1], bonus[2])
                }
            }

            // 新增觉醒属性
            if (eb.wake > 0) {
                int[] wi = ebinfo.wakenadd[eb.wake - 1]
                for (int jj = 0; jj < wi.length; jj += 2) {
                    battle.deal(this, wi[jj], wi[jj + 1])
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
                ++e_count
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
                ++t_count
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

        // +++ 套装属性加成
        suit.forEach({ key, ls ->
            int val = ls.size()

            if (val > 1) { // 套装属性2件开始算起，2件就可以有suitadd第一个属性加成，3件有2个属性，4件有全部4个属性
                EquipConf ebinfo = battle.readonly.findEquip(key)
                val -= 1
                if (val == 3) {
                    val = ebinfo.suitadd.length / 2
                }

                for (int i = 0; i < val; ++i) {
                    int id = ebinfo.suitadd[i * 2]
                    int vl = ebinfo.suitadd[i * 2 + 1]
                    battle.deal(this, id, vl)
                }
            }
        })

        // +++ 装备与宝物的共鸣

        // 装备必须4个
        if (e_count >= 4) {
            for (int i = 0; i < 2; ++i) {
                int[] rb = battle.readonly.findResonance(resonance[i], i)
                if (rb != null) {
                    for (int j = 0; j < rb.length; j += 2) {
                        battle.deal(this, rb[j], rb[j + 1])
                    }
                }
            }
        }

        // 宝物必须2个
        if (t_count >= 2) {
            for (int i = 2; i < 4; ++i) {
                int[] rb = battle.readonly.findResonance(resonance[i], i)
                if (rb != null) {
                    for (int j = 0; j < rb.length; j += 2) {
                        battle.deal(this, rb[j], rb[j + 1])
                    }
                }
            }
        }
    }

    Roler(GroovyFightSystem battle, int side, int idx, DirectHeroStruct hb) {
        this.battle = battle
        this.side = side
        this.index = idx

        HeroConf hc = battle.readonly.findHero(hb.hero)
        this.id = hc.key
        this.sect = hc.sect
        this.mode = hc.type
        this.name = hc.name

        life = hb.life
        max_life = hb.life
        attack = hb.attack
        defs = hb.def
        mdef = hb.mdef
        crit = hb.critical
        anti_crit = hb.antiCrit
        dodge = hb.dodge
        aim = hb.aim
        over_harm = hb.overHarm
        en_harm = hb.enHarm
        anger = 4
        //check_group_skills()
    }

    /**
     * 传奇英雄
     * @param battle
     * @param side
     */
    Roler(GroovyFightSystem battle, int side) {
        this.battle = battle
        this.side = side
        this.index = 6 // 第七个人
    }

    /***
     * 检测组合技，超级组合技是否能生效
     * @param role
     */
    void check_group_skills() {
        int hero_id = getRealHero()

        HeroSkillConf hsc = battle.readonly.findSkill(hero_id)
        if (hsc != null) {
            // 3.1 组合技
            if (hsc.attack3eff.length > 0) { // 首先判断是否存在组合技，然后再判断是否激活
                group_skill = true
                for (int c : hsc.cost) {
                    if (battle.getTeam(side).stream().noneMatch({ r ->
                        (!r.isDead() && (r.id == c || (r.skin == c)))
                    })) {
                        group_skill = false
                        break
                    }
                }
            }

            super_group_skill = false
            // 3.1.1 检测超级组合技
            if (grade >= 10 && hsc.attack4eff.length > 0) {
                if (group_skill) {
                    super_group_skill = true
                }
            }
        }
    }

    /**
     * 检测处理一个角色的所有技能
     * @param assist 缘分，包括6个出场英雄和6个缘分英雄
     * @param hb
     * @param role
     */
    void handle_skills(HeroStruct hb, Set<Integer> assist) {
        int hero_id = id
        boolean is_lord = Defs.isLordID(id)

        // 领主的皮肤不为0，模拟该英雄
        if (is_lord && skin > 1) { // 注意1标识觉醒与否
            hero_id = skin
        }

        HeroSkillConf hsc = battle.readonly.findSkill(hero_id)
        if (hsc != null) {
            // 3.1 组合技
            if (hsc.attack3eff.length > 0) { // 首先判断是否存在组合技，然后再判断是否激活
                group_skill = true
                for (int c : hsc.cost) {
                    if (battle.getTeam(side).stream().noneMatch({ r ->
                        (
                                r.id == c || (r.skin == c)
                        )
                    })) {
                        group_skill = false
                        break
                    }
                }
            }

            // 3.1.1 检测超级组合技
            if (grade >= 10 && hsc.attack4eff.length > 0) {
                if (group_skill) {
                    super_group_skill = true
                }
            }

            // 3.2 天命导致的技能系数增幅
            for (int i = 0; i < hsc.skillup.length; ++i) {
                fate_skill_up[i] = (hb.fate - 1) * hsc.skillup[i]
            }
        }

        // +++ 皮肤不能影响的参数, 使用role.id, 不是hero_id
        HeroSkillConf hsb = battle.readonly.findSkill(id)
        if (hsb != null) {
            // 3.3 突破效果 self
            if (hb.gift < 25) {
                for (int i = 0; i < hb.grade; ++i) {
                    int[] be = hsb.breakeff[i]
                    battle.deal(this, be[0], be[1])
                }
            } else { // 突破成红色之后，使用另外一套
                for (int i = 0; i < hb.grade; ++i) {
                    int[] be = hsb.breakeff2[i]
                    battle.deal(this, be[0], be[1])
                }
            }

            // +++ 缘分 self
            // 以下四个是英雄缘分, 如果是领主 前4个缘分是装备
            if (hsb.gk1eff.length > 0) {
                if (is_lord) { // 领主第一个缘分是装备
                    for (EquipStruct eq : hb.equipStructs) {
                        if (eq != null && eq.id == hsb.gk1cost[0]) {
                            for (int ii = 0; ii < hsb.gk1eff.length; ii += 2) {
                                int id = hsb.gk1eff[ii]
                                int vl = hsb.gk1eff[ii + 1]
                                battle.deal(this, id, vl)
                            }
                        }
                    }
                } else { // 英雄第一缘分
                    boolean flag = true
                    for (int c : hsb.gk1cost) {
                        if (!assist.contains(c)) {
                            flag = false
                            break
                        }
                    }

                    if (flag) {
                        for (int ii = 0; ii < hsb.gk1eff.length; ii += 2) {
                            int id = hsb.gk1eff[ii]
                            int vl = hsb.gk1eff[ii + 1]
                            battle.deal(this, id, vl)
                        }
                    }
                }
            }
            if (hsb.gk2eff.length > 0) {
                if (is_lord) {
                    for (EquipStruct eq : hb.equipStructs) {
                        if (eq != null && eq.id == hsb.gk2cost[0]) {
                            for (int ii = 0; ii < hsb.gk2eff.length; ii += 2) {
                                int id = hsb.gk2eff[ii]
                                int vl = hsb.gk2eff[ii + 1]
                                battle.deal(this, id, vl)
                            }
                        }
                    }
                } else {
                    boolean flag = true
                    for (int c : hsb.gk2cost) {
                        if (!assist.contains(c)) {
                            flag = false
                            break
                        }
                    }

                    if (flag) {
                        for (int ii = 0; ii < hsb.gk2eff.length; ii += 2) {
                            int id = hsb.gk2eff[ii]
                            int vl = hsb.gk2eff[ii + 1]
                            battle.deal(this, id, vl)
                        }
                    }
                }
            }
            if (hsb.gk3eff.length > 0) {
                if (is_lord) {
                    for (EquipStruct eq : hb.equipStructs) {
                        if (eq != null && eq.id == hsb.gk3cost[0]) {
                            for (int ii = 0; ii < hsb.gk3eff.length; ii += 2) {
                                int id = hsb.gk3eff[ii]
                                int vl = hsb.gk3eff[ii + 1]
                                battle.deal(this, id, vl)
                            }
                        }
                    }
                } else {
                    boolean flag = true
                    for (int c : hsb.gk3cost) {
                        if (!assist.contains(c)) {
                            flag = false
                            break
                        }
                    }

                    if (flag) {
                        for (int ii = 0; ii < hsb.gk3eff.length; ii += 2) {
                            int id = hsb.gk3eff[ii]
                            int vl = hsb.gk3eff[ii + 1]
                            battle.deal(this, id, vl)
                        }
                    }
                }
            }
            if (hsb.gk4eff.length > 0) {
                if (is_lord) {
                    for (EquipStruct eq : hb.equipStructs) {
                        if (eq != null && eq.id == hsb.gk4cost[0]) {
                            for (int ii = 0; ii < hsb.gk4eff.length; ii += 2) {
                                int id = hsb.gk4eff[ii]
                                int vl = hsb.gk4eff[ii + 1]
                                battle.deal(this, id, vl)
                            }
                        }
                    }
                } else {
                    boolean flag = true
                    for (int c : hsb.gk4cost) {
                        if (!assist.contains(c)) {
                            flag = false
                            break
                        }
                    }

                    if (flag) {
                        for (int ii = 0; ii < hsb.gk4eff.length; ii += 2) {
                            int id = hsb.gk4eff[ii]
                            int vl = hsb.gk4eff[ii + 1]
                            battle.deal(this, id, vl)
                        }
                    }
                }
            }

            // 以下是装备的四个缘分
            if (hsb.gk5eff.length > 0) {
                for (EquipStruct eq : hb.equipStructs) {
                    if (eq != null && eq.id == hsb.gk5cost) {
                        for (int ii = 0; ii < hsb.gk5eff.length; ii += 2) {
                            int id = hsb.gk5eff[ii]
                            int vl = hsb.gk5eff[ii + 1]
                            battle.deal(this, id, vl)
                        }
                    }
                }
            }
            if (hsb.gk6eff.length > 0) {
                for (EquipStruct eq : hb.equipStructs) {
                    if (eq != null && eq.id == hsb.gk6cost) {
                        for (int ii = 0; ii < hsb.gk6eff.length; ii += 2) {
                            int id = hsb.gk6eff[ii]
                            int vl = hsb.gk6eff[ii + 1]
                            battle.deal(this, id, vl)
                        }
                    }
                }
            }
            if (hsb.gk7eff.length > 0) {
                for (EquipStruct eq : hb.equipStructs) {
                    if (eq != null && eq.id == hsb.gk7cost) {
                        for (int ii = 0; ii < hsb.gk7eff.length; ii += 2) {
                            int id = hsb.gk7eff[ii]
                            int vl = hsb.gk7eff[ii + 1]
                            battle.deal(this, id, vl)
                        }
                    }
                }
            }
            if (hsb.gk8eff.length > 0) {
                for (EquipStruct eq : hb.equipStructs) {
                    if (eq != null && eq.id == hsb.gk8cost) {
                        for (int ii = 0; ii < hsb.gk8eff.length; ii += 2) {
                            int id = hsb.gk8eff[ii]
                            int vl = hsb.gk8eff[ii + 1]
                            battle.deal(this, id, vl)
                        }
                    }
                }
            }
        }
    }

    /**
     * 记录的大量个人属性，阵营属性，全体属性，合并到个人真实数值中,
     * 先数值，后百分比的方式计算
     */
    void merge() {
        Map<Integer, Double> self = null
        Map<Integer, Double> sect = null

        if (this.sect > 0) {
            sect = battle.sim.sects[this.sect - 1]
        }

        self = battle.sim.single[index]

        // +++ 处理全体的
        // 全体数值,和百分比都堆积到个人数值和百分比上
        battle.sim.all.forEach({ key, val ->
            switch (key) {
                case 60:
                    self.merge(40, val, { a, b -> a + b })
                    break
                case 61:
                    self.merge(41, val, { a, b -> a + b })
                    break
                case 62:
                    self.merge(42, val, { a, b -> a + b })
                    break
                case 63:
                    self.merge(43, val, { a, b -> a + b })
                    break
                case 64:
                    self.merge(44, val, { a, b -> a + b })
                    break
                case 65:
                    self.merge(45, val, { a, b -> a + b })
                    break
                case 66:
                    self.merge(46, val, { a, b -> a + b })
                    break
                case 67:
                    self.merge(47, val, { a, b -> a + b })
                    break
                case 68:// 全体百分比
                    self.merge(48, val, { a, b -> a + b })
                    break
                case 69:
                    self.merge(49, val, { a, b -> a + b })
                    break
                case 70:
                    self.merge(57, val, { a, b -> a + b })
                    self.merge(58, val, { a, b -> a + b })
                    break
                case 71:
                    self.merge(51, val, { a, b -> a + b })
                    break
                case 72:
                    self.merge(52, val, { a, b -> a + b })
                    break
                case 73:
                    self.merge(53, val, { a, b -> a + b })
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
                case 150: // 注意，个人多了一个150提升速度百分比
                    self.merge(150, val, { a, b -> a + b })
                    break
                case 151:
                    self.merge(48, val / 100, { a, b -> a + b })
                    break
                case 152:
                    self.merge(49, val / 100, { a, b -> a + b })
                    break
                case 153:
                    self.merge(57, val / 100, { a, b -> a + b })
                    self.merge(58, val / 100, { a, b -> a + b })
                    break
                case 154:
                    self.merge(35, val, { a, b -> a + b })
                    break
                case 155:
                    self.merge(36, val, { a, b -> a + b })
                    break
                case 171:
                    sect_harm_up[0] += val / 100
                    break
                case 172:
                    sect_harm_up[1] += val / 100
                    break
                case 173:
                    sect_harm_up[2] += val / 100
                    break
                case 174:
                    sect_harm_up[3] += val / 100
                    break
            }
        })

        // +++ 阵营数值和百分比都堆积到个人
        if (sect != null) {
            sect.forEach({ key, val ->
                switch (key) {
                    case 80:
                        self.merge(40, val, { a, b -> a + b })
                        break
                    case 81:
                        self.merge(41, val, { a, b -> a + b })
                        break
                    case 82:
                        self.merge(42, val, { a, b -> a + b })
                        break
                    case 83:
                        self.merge(43, val, { a, b -> a + b })
                        break
                    case 84:
                        self.merge(44, val, { a, b -> a + b })
                        break
                    case 85:
                        self.merge(45, val, { a, b -> a + b })
                        break
                    case 86:
                        self.merge(46, val, { a, b -> a + b })
                        break
                    case 87:
                        self.merge(47, val, { a, b -> a + b })
                        break
                    case 88:// 全体百分比
                        self.merge(48, val, { a, b -> a + b })
                        break
                    case 89:
                        self.merge(49, val, { a, b -> a + b })
                        break
                    case 90:
                        self.merge(57, val, { a, b -> a + b })
                        self.merge(58, val, { a, b -> a + b })
                        break
                    case 91:
                        self.merge(51, val, { a, b -> a + b })
                        break
                    case 92:
                        self.merge(52, val, { a, b -> a + b })
                        break
                    case 93:
                        self.merge(53, val, { a, b -> a + b })
                        break
                    case 181:
                        sect_harm_up[0] += val / 100
                        break
                    case 182:
                        sect_harm_up[1] += val / 100
                        break
                    case 183:
                        sect_harm_up[2] += val / 100
                        break
                    case 184:
                        sect_harm_up[3] += val / 100
                        break
                }
            })
        }

        // +++ 个人数值

        self.forEach({ key, val ->
            switch (key) {
                case 21: // 概率反弹物理百分比伤害
                    reflect_ratio += val / 1000
                    reflect_percent += val % 1000
                    break
                case 22: // 概率反弹魔法百分比伤害
                    m_reflect_ratio += val / 1000
                    m_reflect_percent += val % 1000
                    break
                case 24: // 被攻击时概率回血
                    draw_ratio += (int) ((int) (val) / 1000)
                    draw_life += (int) (val % 1000)
                    break
                case 40: // life
                    max_life += val
                    break
                case 41:
                    attack += val
                    break
                case 42:
                    defs += val
                    mdef += val
                    break
                case 43:
                    crit += val
                    break
                case 44:
                    aim += val
                    break
                case 45: //闪避提高Buff
                    dodge += val
                    break
                case 46: //抗暴提高Buff
                    anti_crit += val
                    break
                case 47: //初始怒气增加
                    addAnger((int) val)
                    break
                case 51: // 增加伤害
                    en_harm += val
                    break
                case 52: // 难伤配置，易伤存储
                    over_harm += val
                    break
                case 53: //回合恢复怒气增加
                    anger_cover += val
                    break
                case 54: //法防
                    mdef += val
                    break
                case 55: //物防
                    defs += val
                    break
                case 56: //机率造成双倍伤害
                    multi_harm_ratio += val
                    break
                case 59: // 速度提高
                    speed += val
                    break
                case 35: // 魔法伤害降低
                    mag_lower += val
                    break
                case 36: // 物理伤害降低
                    phy_lower += val
                    break
            }
        })

        // ----  个人百分比
        self.forEach({ key, val ->
            switch (key) {
            // ---- 百分比影响部分 ----
                case 48:
                    attack += (attack / 100 * val)
                    break
                case 49:
                    max_life += (max_life / 100 * val)
                    break
                case 57:
                    mdef += (mdef / 100 * val)
                    break
                case 58:
                    defs += (defs / 100 * val)
                    break
                case 150:
                    speed += (speed / 100 * val)
                    break
            }
        })



        self.clear()
        life = max_life
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

    // 特殊事件 子类需要
    void onDead() {
        clearBuffs()
    }

    double onHarm(Roler target, double harm, boolean isCrit) {
        harm = target.onPunch(this, harm)
        if (harm < 0) {
            return harm
        }
        return target.fixLife(null, -harm as long, BuffInfo.攻击伤害, isCrit)
    }

    double onPunch(Roler attacker, double harm) {
        return harm
    }

    double beforeDoHarm(double harm) {
        return harm
    }

    /**
     * 角色攻击后,子类的特殊处理
     * @param harm
     */
    void afterDoHarm(Roler target, double h, boolean isDodge, boolean isPeerDead, boolean isCrit) {
    }

    void afterPunch(Roler attacker, double h, boolean isDodge, boolean isDead) {

    }

    /**
     * 血量扣减与增加，触发对应的检测
     *
     * @param _life 血量增减的具体值
     * @param mode 原因，也是攻击类型，普通伤害，火焰伤害，或者加血等
     * @param crit 是否暴击
     *
     * @returns 返回实际攻击伤害 大于等于0 不论加血还是扣血  负数 标识特殊技能躲避
     */
    long fixLife(Roler attacker, long _life, int mode, boolean crit) throws GameOverException {
        if (_life == 0) {
            return _life
        }

        int flag = 0

        // 先根据自己的buff，求是否有盾之类的可以减少伤害的
        for (BuffInfo buff : buffs) {
            if (!buff.deleted) {
                _life = buff.onFixLife(_life)
            }
        }

        if (_life == 0) {
        } else {
            if (mode == BuffInfo.反击伤害) {
                getBattle().getCurrent().addEffectHarm(attacker, mode, _life, crit)
            } else {
                getBattle().getCurrent().addEffectHarm(this, mode, _life, crit)
            }
        }

        life += _life
        if (life < 1) {
            life = 0
            onDead()
            flag = battle.checkOver(side)
//            println("英雄:" + name + " 死亡 side:" + side + " index:" + index + " flag:" + flag)

            if (flag == 0) {
                // 角色死亡，但是游戏没有结束，需要查询对方是否有乌尔奇奥拉
                List<Roler> peers = getBattle().getTeam((side % 2) + 1)
                for (Roler peer : peers) {
                    if (peer instanceof 乌尔奇奥拉) {
                        HeroSkillConf hc = battle.readonly.findSkill(peer.id)
                        peer.addBuff(new 攻击提高Buff(peer, hc.skillzeff[1], hc.skillzeff[0]))
                    }
                }

                List<Roler> ours = getBattle().getTeam(side)
                for (Roler peer : ours) {
                    if (peer instanceof 张无忌) {
                        HeroSkillConf hc = battle.readonly.findSkill(peer.id)
                        peer.addBuff(new 攻击提高Buff(peer, hc.skillzeff[1], hc.skillzeff[0]))
                    }
                }
            }
        }

        if (life > max_life) {
            life = max_life
        }

        if (flag != 0) {
            throw new GameOverException(flag)
        }
        return Math.abs(_life)
    }

    /**
     * 增加Buff
     * <p>
     * 1. 技能与突破带的buff
     * 2. 发动攻击后，产生的buff.
     *
     * @param buff
     */
    void addBuff(BuffInfo buff) {
        if (isDead()) {
            return
        }

        if (buff.mutex) {
            for (BuffInfo b : buffs) {
                if (!b.deleted && b.getClass() == buff.getClass()) {
                    if (buff.round > b.round) {
                        b.round = buff.round
                    }

                    b.mix(buff)
                    return
                }
            }
        }

        // 反戈一击永远放在最后，因为buff生效的顺序反戈一击是带动作的应该最后出现
        int index = buffs.size() - 1
        if (index >= 0 && buffs.get(index).getClass() == 反戈一击Buff.class) {
            buffs.add(index, buff)
        } else {
            buffs.add(buff)
        }
        buff.onAddStatus()
    }

    void delBuff(int i) {
        BuffInfo bi = buffs.remove(i)
        if (bi != null) {
            bi.onDelStatus()
        }
    }

    /**
     * 清除所有buff，一般在死亡的时候使用
     */
    void clearBuffs() {
        for (BuffInfo buff : buffs) {
            if (!buff.deleted) {
                buff.onDelStatus()
            }
        }

        buffs.clear()
    }

    /**
     * 清除所有buff
     */
    void delGoodBuff() {
        for (int i = 0; i < buffs.size();) {
            BuffInfo buff = buffs.get(i)
            if (!buff.deleted && buff.goodness && !buff.cantDel) {
                delBuff(i)
            } else {
                ++i
            }
        }
    }

    void delAll灼烧() {
        for (int i = 0; i < buffs.size();) {
            BuffInfo buff = buffs.get(i)
            if (!buff.deleted && buff.getClass().getName() == "灼烧Buff") {
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
            BuffInfo buff = buffs.get(i)
            if (!buff.deleted && !buff.goodness && !buff.cantDel) {
                delBuff(i)
            } else {
                ++i
            }
        }
    }

    int fixForbidAction(int delta) {
        if (delta > 0) {
            this.forbidAction |= delta
        } else {
            this.forbidAction &= (~delta)
        }
        return this.forbidAction
    }

    /**
     * 在比赛的一整个回合结束后,恢复回合中的状态
     */
    void onRound() {
        isActiond = false
        alreadyMoralAction = false
        //check_group_skills()
    }

    void onFirstRound() {

    }

    /**
     * 在Action轮到自己后,开始行动前
     * @param round
     * @throws GameOverException
     */
    void checkBuffsRoundBefore(int round) throws GameOverException {
        // 本回合开始，将本次攻击数字重置
        List<BuffInfo> tmp = new ArrayList<>()
        tmp.addAll(buffs);

        for (BuffInfo buff : tmp) {
            if (!buff.deleted) {
                buff.onRoundBefore(round)
            }
        }
    }

    void checkBuffsRoundEnd(int round) {
        addAnger(anger_cover)
        battle.getCurrent().anger = anger

        for (int i = 0; i < buffs.size();) {
            BuffInfo buff = buffs.get(i)
            if (buff.deleted) { // 已经删除的buff不应该再次进入删除逻辑,直接从buffs中硬删除
                buffs.remove(i);
            } else if (--(buff.round) <= 0) {
                delBuff(i)
                buff.onRoundEnd(round)
            } else {
                ++i
            }
        }
    }

    String debug(double power) {
        return "战斗力[${power}]  英雄[${name}] 血量:${max_life} 攻击:${attack} 物理防御:${defs} 法防:${mdef} 暴击率:${crit} 命中率:${aim} 闪避率:${dodge} 抗爆率:${anti_crit} 增伤率:${en_harm} 易伤率:${over_harm} 速度:${speed}\n" + "\n"
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
}

/**
 * 武装角色 特殊处理
 */
class 乌尔奇奥拉 extends Roler {
    乌尔奇奥拉(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }
}

class 哈迪斯 extends Roler {

    哈迪斯(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void onDead() {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        List<Roler> peers = getBattle().findPeer(side, 0, FindPeerEnum.随机友军, hc.skillzeff[2])
        for (Roler peer : peers) {
            long cure = (hc.skillzeff[0] / 100.0 * this.max_life) as long
            peer.fixLife(null, cure, BuffInfo.治疗恢复, false)
            peer.addBuff(new 生命献祭Buff(peer, hc.skillzeff[3], this.attack * hc.skillzeff[1] / 100))
        }

        super.onDead()
    }
}

class 雅典娜 extends Roler {

    雅典娜(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isPeerDead || isDodge || harm < 1) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            battle.getCurrent().wuz = 1
            target.addBuff(new 溢魔Buff(target, hc.skillzeff[2], hc.skillzeff[1]))
        }
    }
}

class 鸣人 extends Roler {

    鸣人(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    double onPunch(Roler attacker, double harm) {
        if (attacker.mode == GroovyFightSystem.法术攻击 || attacker.mode == GroovyFightSystem.辅助攻击) {
            HeroSkillConf hc = battle.readonly.findSkill(this.id)
            if (battle.getRandom() <= hc.skillzeff[0]) {
                // 反弹伤害
                attacker.fixLife(null, -harm as long, BuffInfo.攻击伤害, false)
                return 0
            }
        }
        return harm
    }
}

class 纲手 extends Roler {

    纲手(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isPeerDead || isDodge) return
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        target.addBuff(new ShieldBuff(target, hc.skillzeff[1], harm * hc.skillzeff[0] / 100))
    }
}

class 女帝 extends Roler {

    女帝(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void onDead() {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        List<Roler> ours = battle.getTeam(side)
        for (Roler r : ours) {
            if (!r.isDead()) {
                r.addAnger((int) hc.skillzeff[0])
                battle.current.addEffectHarm(r, BuffInfo.怒气控制, r.anger, false)
            }
        }
        super.onDead()
    }
}

class 大蛇丸 extends Roler {

    大蛇丸(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isPeerDead || isDodge) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            battle.getCurrent().wuz = 1
            long h = harm * hc.skillzeff[1] / 100
            target.fixLife(null, -h, BuffInfo.额外伤害, false)
        }
    }
}

class 佐助 extends Roler {

    佐助(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isPeerDead || isDodge) return
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            target.addBuff(new 缠绕Buff(target, hc.skillzeff[2], hc.skillzeff[1]))
        }
    }
}

class 黑岩射手 extends Roler {

    黑岩射手(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    double onHarm(Roler target, double harm, boolean isCrit) {
        if (battle.getPeer(side).stream().filter({role -> (!role.isDead() && role.index < 3)}).count() <= 0) {
            battle.getCurrent().wuz = 1
            HeroSkillConf hc = battle.readonly.findSkill(this.id)
            harm = harm * hc.skillzeff[0] / 100.0
            harm = target.onPunch(this, harm)
            return target.fixLife(null, -harm as long, BuffInfo.攻击伤害, true)
        }

        return super.onHarm(target, harm, isCrit)
    }
}

class 貂蝉 extends Roler {

    貂蝉(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge || isDead()) return

        if (isPeerDead) {
            battle.getCurrent().wuz = 1
            HeroSkillConf hc = battle.readonly.findSkill(this.id)
            List<Roler> peers = battle.findPeer(side, 0, FindPeerEnum.随机敌军, 1)
            for (Roler r : peers) {
                harm *= hc.skillzeff[0] / 100.0
                r.fixLife(null, -harm as long, BuffInfo.额外伤害, false)
            }
        }
    }
}

class 诸葛亮 extends Roler {

    诸葛亮(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isPeerDead || isDodge || isDead()) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        long extra = this.max_life * hc.skillzeff[0] / 100
        fixLife(null, extra, BuffInfo.治疗恢复, false)
    }
}

class 马超 extends Roler {

    马超(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isPeerDead || isDodge || battle.isBoss) return
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            battle.getCurrent().wuz = 1
            target.addBuff(new 马超毒Buff(target, hc.skillzeff[2], hc.skillzeff[1]))
        }
    }
}

class 黄忠 extends Roler {

    黄忠(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isPeerDead || isDodge) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            target.addBuff(new 石化Buff(target, hc.skillzeff[1]))
        }
    }
}

class 刘备 extends Roler {

    刘备(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void onDead() {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        List<Roler> peers = battle.findPeer(side, 0, FindPeerEnum.随机敌军, hc.skillzeff[0])
        for (Roler r : peers) {
            if (!r.isDead()) {
                r.addAnger((int) -hc.skillzeff[1])
                battle.current.addEffectHarm(r, BuffInfo.怒气控制, r.anger, false)
            }
        }
        super.onDead()
    }
}

class 张飞 extends Roler {

    张飞(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    double onHarm(Roler target, double harm, boolean isCrit) {
        if (!battle.isBoss) {
            HeroSkillConf hc = battle.readonly.findSkill(this.id)
            if (battle.getRandom() <= hc.skillzeff[0]) {
                battle.getCurrent().wuz = 1
                harm = target.life + 0.5
                harm = target.onPunch(this, harm)
                return target.fixLife(null, -harm as long, BuffInfo.死神凝视, false)
            }
        }
        return super.onHarm(target, harm, isCrit)
    }
}

class 周瑜 extends Roler {

    private boolean addBuff = false

    周瑜(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void onRound() {
        super.onRound()
        addBuff = false
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge) return

        if (isPeerDead && !addBuff) {
            battle.getCurrent().wuz = 1
            HeroSkillConf hc = battle.readonly.findSkill(this.id)
            List<Roler> ours = battle.findPeer(side, 0, FindPeerEnum.随机友军, hc.skillzeff[0])
            for (Roler r : ours) {
                double attack = target.attack * hc.skillzeff[1]
                r.addBuff(new 嗜血Buff(r, hc.skillzeff[2], attack))
            }
        }
    }
}

class 司马懿 extends Roler {

    司马懿(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge || !isCrit) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            List<Roler> ours = battle.findPeer(side, 0, FindPeerEnum.随机友军, hc.skillzeff[0])
            for (Roler r : ours) {
                int speed = target.speed * hc.skillzeff[1]
                r.addBuff(new 速度提高Buff(r, hc.skillzeff[2], speed))
            }
        }
    }
}

class 赵云 extends Roler {

    赵云(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge || isPeerDead) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            long h = this.attack * hc.skillzeff[1] / 100
            target.fixLife(null, -h, BuffInfo.霹雳闪电, false)
        }
    }
}

class 杨过 extends Roler {

    杨过(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    double onHarm(Roler target, double harm, boolean isCrit) {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            battle.getCurrent().wuz = 1
            harm = harm * hc.skillzeff[1] / 100
            harm = target.onPunch(this, harm)
            return target.fixLife(null, -harm as long, BuffInfo.攻击伤害, true)
        }

        return super.onHarm(target, harm, isCrit)
    }
}

class 小骨 extends Roler {

    小骨(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        long h = harm * hc.skillzeff[0] / 100
        if (h > 0) {
            fixLife(null, h, BuffInfo.吸血治疗, false)
        }
    }
}

class 杀阡陌 extends Roler {

    杀阡陌(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void onDead() {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        List<Roler> peers = battle.getTeam(side % 2 + 1)
        for (Roler r : peers) {
            r.addBuff(new 诅咒Buff(r, hc.skillzeff[1], hc.skillzeff[0]))
        }
        super.onDead()
    }
}

class 天山童姥 extends Roler {

    天山童姥(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterPunch(Roler attacker, double harm, boolean isDodge, boolean isDead) {
        if (isDodge || harm < 1 || isDead) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        long h = harm * hc.skillzeff[0] / 100
        if (h > 0) {
            attacker.fixLife(null, -h, BuffInfo.灼烧伤害, false)
        }
    }
}

class 白子画 extends Roler {

    白子画(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge || harm < 1 || isPeerDead) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            battle.getCurrent().wuz = 1
            target.addAnger((int) hc.skillzeff[1])
            battle.current.addEffectHarm(target, BuffInfo.怒气控制, target.anger, false)
        }
    }
}

class 小龙女 extends Roler {

    小龙女(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (!isPeerDead) return

        battle.getCurrent().wuz = 1
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        long h = target.max_life * hc.skillzeff[0] / 100
        List<Roler> peers = battle.getTeam(side % 2 + 1)
        for (Roler r : peers) {
            if (!r.isDead()) {
                r.fixLife(null, -h, BuffInfo.中毒伤害, false)
            }
        }
    }
}

class 赵敏 extends Roler {

    赵敏(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    double beforeDoHarm(double harm) {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        long ratio = (1 - this.life / this.max_life) * 100
        if (ratio > 0 && ratio <= 100) {
            return harm + harm * ratio * hc.skillzeff[0] / 100
        }
        return harm
    }
}

class 周芷若 extends Roler {

    周芷若(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge || isPeerDead) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            battle.getCurrent().wuz = 1
            target.addBuff(new 魅惑Buff(target, hc.skillzeff[2], hc.skillzeff[1]))
        }
    }
}

class 张无忌 extends Roler {
    张无忌(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }
}

class 刀锋女王 extends Roler {

    刀锋女王(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (!isPeerDead) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            battle.getCurrent().wuz = 1
            List<Roler> ours = battle.getTeam(side)
            for (Roler r : ours) {
                int speed = r.speed * hc.skillzeff[1] / 100
                r.addBuff(new 速度提高Buff(r, hc.skillzeff[2], speed))
            }
        }
    }
}

class 魂魄妖梦 extends Roler {

    魂魄妖梦(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge || isPeerDead) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            battle.getCurrent().wuz = 1
            target.addBuff(new 魔损Buff(target, hc.skillzeff[2], hc.skillzeff[1]))
        }
    }
}

class 赤瞳 extends Roler {

    赤瞳(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge || isPeerDead) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            target.addBuff(new 蝎毒Buff(target, hc.skillzeff[2], hc.skillzeff[1]))
        }
    }
}

class 一护 extends Roler {

    一护(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void onDead() {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        double shield = this.max_life * hc.skillzeff[0] / 100
        List<Roler> ours = battle.getTeam(side)
        for (Roler r : ours) {
            r.addBuff(new ShieldBuff(r, hc.skillzeff[1], shield))
        }
        super.onDead()
    }
}

class 杀生丸 extends Roler {

    杀生丸(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void checkBuffsRoundBefore(int round) throws GameOverException {
        super.checkBuffsRoundBefore(round)
        if (round == 1) {
            HeroSkillConf hc = battle.readonly.findSkill(this.id)
            List<Roler> ours = battle.getTeam(side)
            int idx = battle.getRandom() % 3
            switch (idx) {
                case 0:
                    for (Roler r : ours) {
                        r.addBuff(new 物损Buff(r, hc.skillzeff[0], hc.skillzeff[1]))
                    }
                    break
                case 1:
                    for (Roler r : ours) {
                        int speed = r.speed * hc.skillzeff[2] / 100
                        r.addBuff(new 速度提高Buff(r, hc.skillzeff[0], speed))
                    }
                    break
                case 2:
                    for (Roler r : ours) {
                        r.addBuff(new 暴击提高Buff(r, hc.skillzeff[0], hc.skillzeff[3]))
                        r.addBuff(new 士气提高Buff(r, hc.skillzeff[0], hc.skillzeff[4]))
                        r.addBuff(new 加防Buff(r, hc.skillzeff[0], hc.skillzeff[5]))
                    }
                    break
            }
        }
    }
}

class 伊利丹 extends Roler {

    伊利丹(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    double onPunch(Roler attacker, double harm) {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            return -1 // 提示特殊技能闪避
        }
        return super.onPunch(attacker, harm)
    }
}

class 艾露莎 extends Roler {

    艾露莎(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
        moral += 8
    }

    void addMoral(int _moral) {
        super.addMoral(_moral)
        if (moral < 0) {
            moral = 0
        }
    }
}

class 吉尔伽美什 extends Roler {

    吉尔伽美什(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterPunch(Roler attacker, double h, boolean isDodge, boolean isDead) {
        if (this.isDead() || isDead || forbidAction != 0) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            long harm = hc.skillzeff[1] * attack / 100
            attacker.fixLife(this, -harm, BuffInfo.反击伤害, false)
        }
    }
}

class 索隆 extends Roler {
    索隆(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }
}

class 自来也 extends Roler {
    private boolean recurrsive = false

    自来也(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void onFirstRound() {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        List<Roler> ours = battle.getTeam(side)
        for (Roler r : ours) {
            r.addBuff(new 士气提高Buff(r, 10, hc.skillzeff[0]))
        }
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (this.isDead()) return

        if (!this.recurrsive) {
//            HeroSkillConf hc = battle.readonly.findSkill(this.id)
            List<Roler> ours = battle.getTeam(side)
            for (Roler r : ours) {
                if (r.isDead()) {
                    this.recurrsive = true
                    r.life = r.max_life
                    r.anger = 2
                    battle.current.addEffectHarm(r, BuffInfo.天使复活, 1, false)
                }
            }
            if (this.recurrsive) {
                battle.getCurrent().wuz = 1
            }
        }
    }
}

class 鼬 extends Roler {
    鼬(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (!isPeerDead) return

        battle.getCurrent().wuz = 1
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        this.addAnger(1)
        battle.current.addEffectHarm(this, BuffInfo.怒气控制, anger, false)
    }
}

class 吕布 extends Roler {

    吕布(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
        this.force_attack = 100
    }
}

class 关羽 extends Roler {
    private double ex_crit

    关羽(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    double beforeDoHarm(double harm) {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        long ratio = (1 - this.life / this.max_life) * 100
        if (ratio > 0 && ratio <= 100) {
            ex_crit = ratio * hc.skillzeff[0]
            crit += ex_crit
        }
        return harm
    }
}

class 东方不败 extends Roler {

    东方不败(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        this.aim += hc.skillzeff[0]
    }

    @Override
    void onFirstRound() {
        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        List<Roler> ours = battle.getPeer(side)
        for (Roler r : ours) {
            r.addBuff(new 士气降低Buff(r, 10, hc.skillzeff[1]))
        }
    }
}

class 张三丰 extends Roler {

    张三丰(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterDoHarm(Roler target, double harm, boolean isDodge, boolean isPeerDead, boolean isCrit) {
        if (isDodge || isPeerDead || battle.isBoss) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        if (battle.getRandom() <= hc.skillzeff[0]) {
            battle.getCurrent().wuz = 1
            target.addBuff(new 衰老Buff(target, hc.skillzeff[2], hc.skillzeff[1]))
        }
    }
}

class 更木剑八 extends Roler {

    更木剑八(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    void afterPunch(Roler attacker, double h, boolean isDodge, boolean isDead) {
        if (this.isDead() || isDead || !isDodge) return

        HeroSkillConf hc = battle.readonly.findSkill(this.id)
        addBuff(new 攻击提高Buff(this, hc.skillzeff[1], hc.skillzeff[0]))
    }
}

class 亚瑟王 extends Roler {
    private boolean once_buff

    亚瑟王(GroovyFightSystem battle, int side, int idx, HeroStruct hb, int lineup, int skin, int skin_level) {
        super(battle, side, idx, hb, lineup, skin, skin_level)
    }

    @Override
    long fixLife(Roler attacker, long _life, int mode, boolean crit) throws GameOverException {
        int rtn = super.fixLife(attacker, _life, mode, crit)

        if (_life < 0 && !once_buff) {
            HeroSkillConf hc = battle.readonly.findSkill(this.id)
            long ratio = this.life * 100 / this.max_life
            if (ratio <= hc.skillzeff[0]) {
                addBuff(new 暴击提高Buff(this, 10, hc.skillzeff[1]))
                addBuff(new 抗暴提高Buff(this, 10, hc.skillzeff[2]))

                once_buff = true
            }
        }

        return rtn
    }
}

/**
 * 数值处理分为:
 * 单人的属性数值提高
 * 阵营的属性数值提高
 * 全体的属性数值提高
 * 数值属性全体求和得到一个人的属性基础值
 * 个人的百分提高
 * 阵营的百分比提高
 * 全体的百分比提高
 * 百分比全体加起来，再对数值属性做一次提升。
 */
class SimStruct {
    private static final Log log = LogFactory.getLog(SimStruct.class)

    // 管理全体的特效
    Map<Integer, Double> all = new HashMap<>()

    // 管理四个阵营的特效
    Map<Integer, Double>[] sects = new HashMap<>[4]

    // 管理左边阵营的单人特效
    Map<Integer, Double>[] single = new HashMap<>[6]

    SimStruct() {
        for (int i = 0; i < 4; ++i) {
            sects[i] = new HashMap<>()
        }
        for (int i = 0; i < 6; ++i) {
            single[i] = new HashMap<>()
        }
    }

    void reset() {
        all.clear()
        for (int i = 0; i < 4; ++i) {
            sects[i].clear()
        }
        for (int i = 0; i < 6; ++i) {
            single[i].clear()
        }
    }

    void merge(HashMap<Integer, Integer> buffs) {
        buffs.forEach({ k, v ->
            all.merge(k, v, { a, b -> a + b })
        })
    }

    void deal(Roler role, int key, double val) {
        deal(role.side, role.index, role.sect, key, val)
    }

    void deal(int side, int idx, int sex, int key, double val) {
        // 阵营一定是[1,4]的值，否则非法
        Map<Integer, Double> sect = null
        if (sex > 0) {
            sect = sects[sex - 1]
        }
        // 自身
        Map<Integer, Double> self = single[idx]

        switch (key) {
        // 个人部分
            case 21:
            case 22:
            case 24:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 59:
            case 35:
            case 36:
            case 48:
            case 49:
            case 57:
            case 58:
                self.merge(key, val, { a, b -> a + b })
                break
        // 全体部分
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
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
                all.merge(key, val, { a, b -> a + b })
                break
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
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
                if (sect != null) {
                    sect.merge(key, val, { a, b -> a + b })
                }
                break

        // 特殊处理部分
            case 23: // 四属性百分比提高
                self.merge(48, val, { a, b -> a + b })
                self.merge(49, val, { a, b -> a + b })
                self.merge(57, val, { a, b -> a + b })
                self.merge(58, val, { a, b -> a + b })
                break
            case 37: // 攻击 /100
                self.merge(48, val / 100, { a, b -> a + b })
                break
            case 38: // 生命 /100
                self.merge(49, val / 100, { a, b -> a + b })
                break
            case 39: // 防御 /100
                self.merge(57, val / 100, { a, b -> a + b })
                self.merge(58, val / 100, { a, b -> a + b })
                break
            case 50: // 防御
                self.merge(57, val, { a, b -> a + b })
                self.merge(58, val, { a, b -> a + b })
                break
            default: //其他增加百分比的，最后计算
                log.error("xxxxx 处理特殊属性增强的时候，属性id:" + key + " 没有处理，可能是新增未加入代码!")
                break
        }
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

    随机友军,
    随机敌军,
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
class HeroInfoComparator implements Comparator<Roler> {

    @Override
    int compare(Roler o1, Roler o2) {
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
    int side
    /**
     * 受到效果的序号
     */
    int index
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
    int eff

    /**
     * 如果效果带伤害，这个值表示伤害的数值
     */
    long val

    /**
     * 是否暴击
     */
    boolean crit = false

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

    /**
     * 本次主动攻击是否激活武装技能
     */
    int wuz

    ActionRecord(int _round, int _side, int _index) {
        round = _round
        side = _side
        index = _index
    }


    void addEffectHarm(Roler hi, int mode, long harm, boolean crit) {
        if (mode == BuffInfo.吸血治疗) {
            for (EffectRecord er : effs) {
                if (er != null && er.side == hi.side && er.index == hi.index && er.eff == mode) {
                    er.val += harm
                    return
                }
            }
        }
        effs.add(new EffectRecord(hi.side, hi.index, mode, harm, crit))
    }

    String debug(GroovyFightSystem battle, Roler roler, int _ri) {
        String msg = "Round:[" + battle.round + "." + _ri + "] anger:" + anger

        msg += " [" + roler.name + "] 行动 >>"

        if (effs.size() <= 0) {
            msg += " 没有任何效果记录，叼炸天.\n"
        }

        for (EffectRecord eff : effs) {
            Roler hi = battle.getTeamRole(eff.side, eff.index)
            msg += "\t[" + hi.name + ":" + hi.getSide() + "-" + eff.getIndex() + "]"
            if (eff.eff < 1000) { // buff类型
                if (eff.val > 0) {
                    msg += " 增加了状态: " + effToString(eff.eff)
                } else {
                    msg += " 去掉了状态: " + effToString(eff.eff)
                }
            } else if (eff.eff >= 1000) {
                if (eff.val < 0) {
                    msg += " 受到了 " + effToString(eff.eff) + " 的伤害: " + eff.val + " 点" + " 剩余血量:" + hi.life
                } else {
                    msg += " 受到了 " + effToString(eff.eff) + " 的恢复: " + eff.val + " 点" + " 剩余血量:" + hi.life
                }
            } else {
                msg += " 不知道干了什么:" + eff.toString()
            }
            msg += "\n"
        }

        return msg
    }

    static String effToString(int eff) {
        switch (eff) {
            case BuffInfo.灼烧类型:
                return "{灼烧Buff}"
            case BuffInfo.中毒类型:
                return "{中毒Buff}"
            case BuffInfo.眩晕:
                return "{眩晕Buff}"
            case BuffInfo.减伤类型:
                return "{减伤Buff}"
            case BuffInfo.易伤类型:
                return "{易伤Buff}"
            case BuffInfo.降防类型:
                return "{降防Buff}"
            case BuffInfo.治疗类型:
                return "{疗伤}"
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
                return "特效:{" + eff + "}"
        }
    }
}

/**
 * 战斗结果结构
 */
class ResultStruct {
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

    // 左方圣物,右方圣物
    public int[] relics = new int[2]

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
    static final int 眩晕 = 102
    static final int 减伤类型 = 103
    static final int 易伤类型 = 104
    static final int 降攻类型 = 105
    static final int 降防类型 = 106
    static final int 治疗类型 = 107
//    static final int 加攻类型 = 108
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

    // 新增通用
    static final int 攻击提高 = 198
    static final int 攻击降低 = 199
    static final int shieldbuff = 200
    static final int shield2buff = 201
    static final int 士气提高 = 202
    static final int 士气降低 = 203
    static final int 速度提高 = 204
    static final int 速度降低 = 205

    // 武装技能
    static final int 溢魔 = 300
    static final int 缠绕 = 301
    static final int 马超毒 = 302  // 马超 互斥 刷新回合
    static final int 石化 = 303
    static final int 嗜血术 = 305
    static final int 诅咒 = 306
    static final int 魅惑 = 307
    static final int 生命献祭 = 308
    static final int 魔损 = 309
    static final int 蝎毒 = 310
    static final int 物损 = 311
    static final int 暴怒 = 312
    static final int 衰老 = 313  // 衰老 互斥 刷新回合 buff加上去的时候传递的不是1，是当前最大血量

    // 传奇英雄
    static final int 烈火神盾 = 400
    static final int 反戈一击 = 401  // 反戈一击 刷新回合 和 次数
    static final int 嗜血 = 402
    static final int 祈祷 = 403
    static final int 王者祝福 = 407 // 王者祝福

    // 圣器
    static final int type811 = 811
    static final int type816 = 816
    static final int type817 = 817
    static final int type820 = 820
    static final int type821 = 821

    //  一次性 ----------------------------------------------
    static final int 灼烧伤害 = 1000
    static final int 中毒伤害 = 1001
    static final int 怒气控制 = 1002
    static final int 生命比例最低攻击 = 1003
    static final int 治疗恢复 = 1004
    static final int 攻击伤害 = 1005
    static final int 群疗恢复 = 1006
    static final int 攻击躲避 = 1007

    static final int 额外伤害 = 1008  // 客户端收到这个,ActionRecord中的人物需要再次播放一次攻击动作后出伤害.

    static final int 士气变动 = 1009  // 正数金色大鸟,负数灰色大鸟
    static final int 霹雳闪电 = 1010  // 负数表示具体伤害
    static final int 天使复活 = 1011  // 直接标识某个英雄被复活
    static final int 死神凝视 = 1012  // 负数标识具体伤害, pvp有效
    static final int 流星火雨 = 1013  // 负数标识具体伤害
    static final int 连锁闪电 = 1014  // 负数标识具体伤害
    static final int 反击伤害 = 1015  // 客户端收到这个,ActionRecord中的人物需要再次播放一次攻击动作后出伤害.

    static final int 盾防吸收 = 1016 //
    static final int 无敌免疫 = 1017 //
    static final int 吸血治疗 = 1018 //

    // 以下是神器buff
    static final int 龙 = 5000
    static final int 神器1 = 5001
    static final int 神器2 = 5002
    static final int 神器3 = 5003


    int round // buff剩余回合数

    /**
     * buff互斥, 一旦互斥,同样的buff增加的时候,如果以前有,则直接比较round,修改成最大的round即可
     */
    protected boolean mutex

    /**
     * true: 增益
     * false: 减益
     */
    boolean goodness

    /**
     * 是否可以被清理掉
     */
    boolean cantDel

    boolean deleted

    Roler heroInfo

    BuffInfo(Roler hi) {
        heroInfo = hi
    }

    // buff的删除,但是不删除角色身上的引用,否则错综复杂的循环修改问题,真实buff在当前删除通知,实际删除在每个roundEnd的时候进行
    void fakeDelete() {
        if (!deleted) {
            deleted = true
            onDelStatus()
        }
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
    double whenAttack(double harm) {
        return harm
    }

    /**
     * 当被攻击时，调用, 比如无敌的时候，修改当前扣血数值, 还有部分临时减伤
     * @param harm
     * @return
     */
    double whenBeAttack(double harm) {
        return harm
    }

    /**
     * 被攻击结束后，增加接入点可以反击或者反弹伤害
     * @param attacker
     * @param harm
     */
    void afterBeAttack(Roler attacker, double harm) {

    }

    /**
     * 盾存在的时候需要使用的
     * @param harm
     * @return
     */
    double onFixLife(double harm) {
        return harm
    }

    void mix(BuffInfo buff) {}
}

class 中毒Buff extends BuffInfo {
    /**
     * 中毒伤害
     */
    private long harm

    中毒Buff(Roler hi, int _round, long _harm) {
        super(hi)
        round = _round
        harm = _harm
        goodness = false
    }

    @Override
    void onRoundBefore(int round) throws GameOverException {
        super.onRoundBefore(round)
        if (harm > 0) {
            heroInfo.fixLife(null, -harm, 中毒伤害, false)
        }
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.battle.getCurrent().addEffectHarm(heroInfo, 中毒类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.battle.getCurrent().addEffectHarm(heroInfo, 中毒类型, -1L, false)
    }
}

class 减伤Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    减伤Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.en_harm -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 减伤类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.en_harm += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 减伤类型, -1L, false)
    }

}

class 加伤Buff extends BuffInfo {
    /**
     *
     */
    private int harm

    加伤Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.en_harm += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 加伤类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.en_harm -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 加伤类型, -1L, false)
    }

}

class 加防Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int defs
    private int mdef

    加防Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        defs = hi.defs * ratio / 100
        mdef = hi.mdef * ratio / 100

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

    命中提高Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
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

class 命中降低Buff extends BuffInfo {
    /**
     */
    private int harm

    命中降低Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.aim -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 命中降低, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.aim += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 命中降低, -1L, false)
    }
}

class 抗暴提高Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    抗暴提高Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.anti_crit += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 抗暴提高, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.anti_crit -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 抗暴提高, -1L, false)
    }
}

class 抗暴降低Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    抗暴降低Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.anti_crit += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 抗暴降低, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.anti_crit -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 抗暴降低, -1L, false)
    }

}

class 无敌 extends BuffInfo {
    无敌(Roler hi, int round) {
        super(hi)
        this.round = round
        goodness = true
        mutex = true
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
    double onFixLife(double harm) {
        if (harm >= 0) {
            return harm
        }

        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 无敌免疫, 0L, false)
        return 0
    }
}

class 易伤Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private float harm

    易伤Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.over_harm += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 易伤类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.over_harm -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 易伤类型, -1L, false)
    }
}

class 暴击提高Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    暴击提高Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.crit += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 暴击提高, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.crit -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 暴击提高, -1L, false)
    }

}

class 治疗Buff extends BuffInfo {
    /**
     * 治疗
     */
    private long harm

    long getHarm() {
        return harm
    }

    void setHarm(long harm) {
        this.harm = harm
    }

    治疗Buff(Roler hi, int round, long _harm) {
        super(hi)
        this.round = round
        harm = _harm
        goodness = true
    }

    @Override
    void onRoundBefore(int round) throws GameOverException {
        super.onRoundBefore(round)
        if (!heroInfo.isDead() && harm > 0) {
            heroInfo.fixLife(null, harm, 治疗恢复, false)
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

    灼烧Buff(Roler hi, int round, long _harm) {
        super(hi)
        this.round = round
        harm = _harm
        goodness = false
    }

    @Override
    void onRoundBefore(int round) throws GameOverException {
        super.onRoundBefore(round)
        if (harm > 0) {
            heroInfo.fixLife(null, -harm, 灼烧伤害, false)
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

    眩晕Buff(Roler hi, int round) {
        super(hi)
        this.round = round
        mutex = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.fixForbidAction(1)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 眩晕, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.fixForbidAction(-1)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 眩晕, -1L, false)
    }
}

class 石化Buff extends BuffInfo {

    石化Buff(Roler hi, int round) {
        super(hi)
        this.round = round
        mutex = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.fixForbidAction(2)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 石化, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.fixForbidAction(-2)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 石化, -1L, false)
    }

    @Override
    double whenBeAttack(double harm) {
        if (harm > 0 && !deleted && ((heroInfo.getForbidAction() & 2) != 0)) {
            heroInfo.fixForbidAction(-2)
            fakeDelete()
        }
        return harm
    }
}

class 闪避提高Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int harm

    闪避提高Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
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

    降攻Buff(Roler hi, int round, int ratio) {
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

    降防Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        defs = hi.defs * ratio / 100
        mdef = hi.mdef * ratio / 100

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

    难伤Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.over_harm -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 难伤类型, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.over_harm += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 难伤类型, -1L, false)
    }
}

/**
 * 盾防
 */
class ShieldBuff extends BuffInfo {
    double shield

    ShieldBuff(Roler hi, int round, double val) {
        super(hi)
        this.round = round

        shield = val
        goodness = true
        mutex = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, shieldbuff, 1L, false)
    }

    @Override
    double onFixLife(double harm) {
        if (harm >= 0) {
            return harm
        }

        harm = -harm
        if (shield >= harm) {
            shield -= harm
            harm = 0
            heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 盾防吸收, 0, false)
        } else if (shield < harm) {
            shield = 0
            harm -= shield
            // 删除自己 ?在循环内删除自己,不知道外部循环还能不能进行下去
            fakeDelete()
        }

        return -harm
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        shield = 0
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, shieldbuff, -1L, false)
    }

    @Override
    void mix(BuffInfo buff) {
        this.shield += ((ShieldBuff) buff).shield
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, shieldbuff, 1L, false)
    }
}

/**
 * 盾抵挡
 */
class Shield2Buff extends BuffInfo {
    int count

    Shield2Buff(Roler hi, int round, int count) {
        super(hi)
        this.round = round
        this.count = count
        goodness = true
        mutex = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, shield2buff, 1L, false)
    }

    @Override
    double onFixLife(double harm) {
        if (harm >= 0) {
            return harm
        }

        harm = -harm

        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 盾防吸收, 0, false)
        if (--count <= 0) {
            fakeDelete()
        }

        return 0
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, shield2buff, -1L, false)
    }

    @Override
    void mix(BuffInfo buff) {
        this.count += ((Shield2Buff) buff).count
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, shield2buff, 1L, false)
    }
}

class Type811 extends BuffInfo {
    /**
     * 暴击 暴击伤害
     */
    private int critical
    private int crit_harm

    Type811(Roler hi, int[] attrs) {
        super(hi)
        this.round = attrs[4]
        critical = attrs[2]
        crit_harm = attrs[3]

        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.crit += critical
        heroInfo.crit_harm += crit_harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type811, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.crit -= critical
        heroInfo.crit_harm += crit_harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type811, -1L, false)
    }
}

class Type816 extends BuffInfo {
    private int dodge
    private int anti_crit

    Type816(Roler hi, int[] attrs) {
        super(hi)
        this.round = attrs[4]
        dodge = attrs[2]
        anti_crit = attrs[3]

        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.dodge -= dodge
        heroInfo.anti_crit -= anti_crit
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type816, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.dodge += dodge
        heroInfo.anti_crit += anti_crit
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type816, -1L, false)
    }
}

class Type817 extends BuffInfo {
    private double attack
    private double aim

    Type817(Roler hi, int[] attrs) {
        super(hi)
        this.round = attrs[4]
        attack = hi.attack * attrs[2] / 100
        aim = attrs[3]

        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack += attack
        heroInfo.aim += aim
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type817, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack -= attack
        heroInfo.aim -= aim
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type817, -1L, false)
    }
}

class Type820 extends BuffInfo {
    private double attack

    Type820(Roler hi, int[] attrs) {
        super(hi)
        this.round = attrs[3]
        attack = hi.attack * attrs[2] / 100

        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack -= attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type820, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack += attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type820, -1L, false)
    }
}

class Type821 extends BuffInfo {
    private int speed
    private int dodge

    Type821(Roler hi, int[] attrs) {
        super(hi)
        this.round = attrs[4]
        speed = (int) (hi.speed * attrs[2] / 100)
        dodge = attrs[3]
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.speed += speed
        heroInfo.dodge += dodge
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type821, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.speed -= speed
        heroInfo.dodge -= dodge
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, type821, -1L, false)
    }
}

class 攻击提高Buff extends BuffInfo {
    /**
     *
     */
    private int attack

    攻击提高Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        attack = hi.attack * ratio / 100
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack += attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 攻击提高, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack -= attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 攻击提高, -1L, false)
    }
}

class 攻击降低Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int attack

    攻击降低Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        attack = hi.attack * ratio / 100
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack -= attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 攻击降低, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack += attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 攻击降低, -1L, false)
    }
}

class 士气提高Buff extends BuffInfo {
    /**
     */
    private int val

    士气提高Buff(Roler hi, int round, int _val) {
        super(hi)
        this.round = round
        val = _val
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.addMoral(val)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 士气提高, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.addMoral(-val)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 士气提高, -1L, false)
    }
}

class 士气降低Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int val

    士气降低Buff(Roler hi, int round, int _val) {
        super(hi)
        this.round = round
        val = _val
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.addMoral(-val)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 士气降低, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.addMoral(val)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 士气降低, -1L, false)
    }
}

class 速度提高Buff extends BuffInfo {
    /**
     */
    private int val

    速度提高Buff(Roler hi, int round, int _val) {
        super(hi)
        this.round = round
        val = _val
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.speed += val
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 速度提高, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.speed -= val
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 速度提高, -1L, false)
    }
}

class 速度降低Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int val

    速度降低Buff(Roler hi, int round, int _val) {
        super(hi)
        this.round = round
        val = _val
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.speed -= val
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 速度降低, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.speed += val
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 速度降低, -1L, false)
    }
}

class 烈火神盾Buff extends BuffInfo {
    /**
     * 反伤比例
     */
    private int val

    烈火神盾Buff(Roler hi, int round, int _val) {
        super(hi)
        this.round = round
        val = _val
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 烈火神盾, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 烈火神盾, -1L, false)
    }

    @Override
    void afterBeAttack(Roler attacker, double harm) {
        if (harm >= 1 && !attacker.isDead()) {
            long h = (harm * val / 100) as long
            heroInfo.fixLife(null, -h, 攻击伤害, false)
        }
    }
}

class 反戈一击Buff extends BuffInfo {
    /**
     * 反击次数
     */
    private int val
    private int ratio

    反戈一击Buff(Roler hi, int round, int _val, int _ratio) {
        super(hi)
        this.round = round
        val = _val
        ratio = _ratio
        goodness = true
        mutex = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 反戈一击, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 反戈一击, -1L, false)
    }

    @Override
    void afterBeAttack(Roler attacker, double harm) {
        if (!attacker.isDead() && !heroInfo.isDead() && heroInfo.forbidAction == 0) {
            long h = (attacker.attack * ratio / 100) as long
            attacker.fixLife(heroInfo, -h, 反击伤害, false)
            --val
            if (val <= 0) {
                fakeDelete()
            }
        }
    }

    @Override
    void mix(BuffInfo buff) {
        this.val = Math.max(this.val, (buff as 反戈一击Buff).val)
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 反戈一击, 1L, false)
    }
}

class 嗜血Buff extends BuffInfo {
    /**
     */
    private double val

    嗜血Buff(Roler hi, int round, double _val) {
        super(hi)
        this.round = round
        val = _val
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack += val
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 嗜血术, 1, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack -= val
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 嗜血术, -1, false)
    }
}

class 嗜血狂野Buff extends BuffInfo {
    /**
     * 增加怒气
     */
    private int val
    private int aim
    private int crit

    嗜血狂野Buff(Roler hi, int round, int _val, int _aim, int _crit) {
        super(hi)
        this.round = round
        val = _val
        aim = _aim
        crit = _crit
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.addAnger(val)
        heroInfo.aim += aim
        heroInfo.crit += crit
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 嗜血, val, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.aim -= aim
        heroInfo.crit -= crit
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 嗜血, -1L, false)
    }
}

class 祈祷Buff extends BuffInfo {
    /**
     * 增加攻击力 速度
     */
    private int attack
    private int speed

    祈祷Buff(Roler hi, int round, int _attack, int _speed) {
        super(hi)
        this.round = round
        attack = _attack
        speed = _speed
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack += attack
        heroInfo.speed += speed
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 祈祷, 1, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack -= attack
        heroInfo.speed -= speed
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 祈祷, -1L, false)
    }
}

class 王者祝福Buff extends BuffInfo {
    /**
     * 增加速度 暴击
     */
    private int speed
    private int crit

    王者祝福Buff(Roler hi, int round, int _speed, int _crit) {
        super(hi)
        this.round = round
        speed = _speed
        crit = _crit
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.speed += speed
        heroInfo.crit += crit
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 王者祝福, 1, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.speed -= speed
        heroInfo.crit -= crit
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 王者祝福, -1, false)
    }
}

class 溢魔Buff extends BuffInfo {
    /**
     * 更多消耗魔法
     */
    private int anger

    溢魔Buff(Roler hi, int round, int _anger) {
        super(hi)
        this.round = round
        anger = _anger
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.anger_extra += anger
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 溢魔, 1, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.anger_extra -= anger
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 溢魔, -1, false)
    }
}

class 缠绕Buff extends BuffInfo {
    /**
     * 降低速度
     */
    private int speed

    缠绕Buff(Roler hi, int round, int _speed) {
        super(hi)
        this.round = round
        this.speed = _speed
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.speed -= speed
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 缠绕, 1, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.speed += speed
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 缠绕, -1, false)
    }
}

class 马超毒Buff extends BuffInfo {
    /**
     * 每个回合降低的生命上限
     */
    private double life

    /**
     * 一共降低的生命上限
     */
    private double total

    马超毒Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        this.life = hi.max_life * ratio / 100
        goodness = false
        mutex = true
    }

    void onRoundBefore(int round) throws GameOverException {
        heroInfo.max_life -= this.life
        this.total += this.life
        double diff = heroInfo.life - heroInfo.max_life
        if (diff >= 1) {
            heroInfo.fixLife(null, -diff as long, 中毒伤害, false)
        }
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 马超毒, 1, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.max_life += this.total
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 马超毒, -1, false)
    }
}

class 诅咒Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int attack

    诅咒Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        attack = hi.attack * ratio / 100
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack -= attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 诅咒, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack += attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 诅咒, -1L, false)
    }
}

class 魅惑Buff extends BuffInfo {
    /**
     */
    private int harm

    魅惑Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.aim -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 魅惑, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.aim += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 魅惑, -1L, false)
    }
}

class 魔损Buff extends BuffInfo {
    /**
     */
    private int harm

    魔损Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = -ratio
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.mag_lower += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 魔损, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.mag_lower -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 魔损, -1L, false)
    }
}

class 物损Buff extends BuffInfo {
    /**
     */
    private int harm

    物损Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        harm = ratio
        goodness = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.phy_lower += harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 物损, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.phy_lower -= harm
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 物损, -1L, false)
    }
}


class 蝎毒Buff extends BuffInfo {
    /**
     * 降低的防御值
     */
    private int attack

    蝎毒Buff(Roler hi, int round, int ratio) {
        super(hi)
        this.round = round
        attack = hi.attack * ratio / 100
        goodness = false
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack -= attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 蝎毒, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack += attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 蝎毒, -1L, false)
    }
}

class 衰老Buff extends BuffInfo {
    /**
     * 一共降低的生命上限
     */
    private double old_max
    private int ratio

    衰老Buff(Roler hi, int round, int _ratio) {
        super(hi)
        this.round = round
        this.ratio = _ratio
        goodness = false
        mutex = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        this.old_max = heroInfo.max_life
        heroInfo.max_life = heroInfo.max_life * this.ratio / 100
        double diff = heroInfo.life - heroInfo.max_life
        if (diff >= 0) {
            heroInfo.fixLife(null, -diff as long, 攻击伤害, false)
        }
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 衰老, heroInfo.max_life as long, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.max_life = this.old_max
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 衰老, -1, false)
    }

    @Override
    void mix(BuffInfo buff) {
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 衰老, heroInfo.max_life as long, false)
    }
}

class 暴怒Buff extends BuffInfo {
    private int ratio

    暴怒Buff(Roler hi, int round, int _ratio) {
        super(hi)
        this.round = round
        this.ratio = _ratio
        goodness = false
        mutex = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 暴怒, 1, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 暴怒, -1, false)
    }

    @Override
    void mix(BuffInfo buff) {
    }
}

class 生命献祭Buff extends BuffInfo {
    private double ex_attack

    生命献祭Buff(Roler hi, int round, double _attack) {
        super(hi)
        this.round = round
        this.ex_attack = _attack

        goodness = true
        mutex = true
    }

    @Override
    void onAddStatus() {
        super.onAddStatus()
        heroInfo.attack += ex_attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 生命献祭, 1, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.attack -= ex_attack
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 生命献祭, -1, false)
    }

    @Override
    void mix(BuffInfo buff) {
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 生命献祭, 1, false)
    }
}


class 龙Buff extends BuffInfo {
    /**
     * 加伤
     */
    private int harmUp
    private int speed

    龙Buff(Roler hi, int _speed, int harm, int round) {
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
        heroInfo.harm_up += harmUp
//        speed = (heroInfo.speed * speed / 100f) as int
        heroInfo.speed -= speed
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 龙, 1L, false)
    }

    @Override
    void onDelStatus() {
        super.onDelStatus()
        heroInfo.harm_up -= harmUp
        heroInfo.speed += speed
        heroInfo.getBattle().getCurrent().addEffectHarm(heroInfo, 龙, -1L, false)
    }
}

class 神器1Buff extends BuffInfo {
    神器1Buff(Roler hi, int round) {
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
    神器2Buff(Roler hi, int round) {
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
    神器3Buff(Roler hi, int round) {
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
