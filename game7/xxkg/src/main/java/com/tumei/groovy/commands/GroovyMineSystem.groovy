package com.tumei.groovy.commands

import com.google.common.base.Strings
import com.tumei.GameConfig
import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.dto.battle.FightResult
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.common.utils.JsonUtil
import com.tumei.common.utils.RandomUtil
import com.tumei.game.GameServer
import com.tumei.game.GameUser
import com.tumei.game.protos.mine.*
import com.tumei.game.protos.mine.structs.*
import com.tumei.game.services.mine.HexNode
import com.tumei.game.services.mine.HexObject
import com.tumei.game.services.mine.MapData
import com.tumei.groovy.contract.IMineSystem
import com.tumei.model.*
import com.tumei.model.beans.AwardBean
import com.tumei.model.beans.mine.MineAwardBean
import com.tumei.model.festival.FestivalBean
import com.tumei.modelconf.*
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.util.ResourceUtils

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer

/**
 *  矿区系统(第一个脚本化的系统)
 *
 *
 */
class GroovyMineSystem implements IMineSystem {
    private static final Log log = LogFactory.getLog(GameServer.class)

    /**
     * 累计update经过的次数，累计到一定的次数，进行一次刷新
     */
    private int flushCum


    /**
     * 累计flushCum经过的次数，累计到一定的次数，进行一次保存
     */
    private int saveCum

    /**
     * 地图静态文件结构
     */
    private MapData mapData

    /**
     * 所有结点的信息
     */
    private HashMap<Integer, HexNode> nodes = new HashMap<>()

    private List<Integer> mineCounts = new ArrayList<>()

    /**
     * 所有在地图中的角色
     */
    private ConcurrentHashMap<Long, MineRole> roles = new ConcurrentHashMap<>()

    /**
     * 传送点
     */
    private HashMap<Integer, Integer> transporter = new HashMap<>();

    // 全局矿脉信息
    private List<MineBean> mines

    @Autowired
    private MineBeanRepository mineBeanRepository

    @PostConstruct
    void init() {
        println("+++++ 正在初始化矿区地图服务 ++++++")

        mines = mineBeanRepository.findAll()
        if (mines == null) {
            mines = new ArrayList<>()
        }

        // 获取矿区刷新配置表
        List<MineRefreshConf> mrcs = Readonly.getInstance().getMineRefreshConfs()
        mrcs.stream().forEach({ mrc ->
            if (mrc.type == 1) { // 标识矿产刷新
                // 要创建矿脉 等级 mrc.level 个数 mrc.num
                switch (mrc.level) {
                    case 1:
                        mineCounts.add(mrc.num)
                        break
                    case 2:
                        mineCounts.add(mrc.num)
                        break
                    case 3:
                        mineCounts.add(mrc.num)
                        break
                    case 4:
                        mineCounts.add(mrc.num)
                        break
                    case 5:
                        mineCounts.add(mrc.num)
                        break
                }
            }
        })

        if (!readMap()) {
            throw new RuntimeException("读取地图失败.")
        }

        flushMines()
    }

    @PreDestroy
    void dispose() {
        println("+++++ 正在保存矿区地图服务.....")
        save()
    }

    void save() {
        // 可以优化成变化的进行保存
        for (int i = 0; i < mines.size();) {
            def amb = mines[i]
            if (amb.key <= 0) {
                mines.remove(i)
                mineBeanRepository.delete(amb.id)
            } else {
                ++i
            }
        }

        for (MineBean amb : mines) {
            mineBeanRepository.save(amb)
        }
    }

    /**
     * 一定的延时进行通报消息
     */
    @Scheduled(fixedDelay = 500L)
    void update() {
        ++saveCum
        ++flushCum
        try {
            roles.forEach({ key, role ->
                role.update()
            })

            if (flushCum % 2 != 0) {
                synchronized (this) {
                    flushMines()
                }
            }

            synchronized (this) {
                if (saveCum > 1000) {
                    saveCum = 0
                    save()
                }
            }
        } catch (Exception ex) {
            println("+++ 矿区刷新任务出现错误:" + ex.getMessage())
        }
    }

    /**
     * 补充矿脉, 在可创建的位置随机创建
     *
     */
    void flushMines() {
        long now = System.currentTimeMillis() / 1000
        MineStoneConf msc = Readonly.getInstance().getMineStoneConfs().get(0)

        HashSet<Integer> pHash = new HashSet<>()
        int[] nums = new int[5]

        // 统计四个级别的矿脉分别有多少个
        for (int i = 0; i < mines.size(); ++i) {
            def amb = mines[i]
            int key = amb.key - 1

            // 如果矿脉为空，或者矿脉关键字key为空, 删除矿脉
            if (amb == null || amb.key <= 0 || amb.key > 5) {
//                log.error("刷新矿脉 key:" + key + " 非法长度。")
                if (amb != null) {
                    amb.key = 0
                }
            } else {
                if (amb.uid != 0) {
                    long dura = now - amb.occupyTime
                    if (dura >= (amb.needToDefende(msc))) {
                        log.warn("矿脉:" + amb.pos + " 收割, dura:" + dura + " 矿脉需要时间:" + (msc.time + amb.enhance - amb.used))
                        long mid = amb.uid
                        List<Integer> awards = harvest(amb)

                        RoleMineBean rmb = DaoGame.getInstance().findRoleMap(mid)
                        if (rmb != null) {
                            rmb.setOccupy(-1)
                            MineAwardBean mab = rmb.getMineAward()
                            mab.awards.clear()
                            mab.awards.addAll(awards)
                            mab.key = amb.key
                            mab.info = null

                            onMineOver(amb, mid, awards)
                        }
                    }
                }
            }

            nums[key] = nums[key] + 1
            pHash.add(amb.pos)
        }

        if (flushCum > 5000) {
            flushCum = 0
            // 根据每个级别矿脉的个数，以及配置需要有的个数进行补充
            for (int i = 0; i <= 4; ++i) {
                int level = i + 1
                int diff = mineCounts.get(i) - nums[i]
                if (diff > 0) { // 对应等级的矿缺少
                    Collections.shuffle(mapData.building)
                    for (HexObject ho : mapData.building) {
                        if (!pHash.contains(ho.pos)) {
                            pHash.add(ho.pos)
                            MineBean spt = new MineBean(ho.pos, level)
                            mines.add(spt)
                            notifyMineChange(spt)
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查矿脉是否存在，且是指定玩家的id拥有
     *
     * @param id
     * @param mine
     * @return
     */
    boolean checkExistsMine(long id, int mine) {
        MineBean mss = mines.find { ab -> (ab.pos == mine && ab.uid == id) }
        if (mss != null) {
            return true
        }
        return false
    }

    /**
     * 矿脉被抢劫后, 通知玩家
     *
     * @param mine
     * @param _id
     * @param robber
     * @param awards
     */
    void onMineOver(MineBean mine, MineRole robber, long _id, int rebirth, List<Integer> awards) {
        MineRole mr = roles.getOrDefault(_id, null)
        if (mr != null) {
            mr.addMineNotify(mine.key, robber.name, robber.grade, rebirth, awards)
        }

        notifyMineChange(mine)
    }

    /**
     * 矿脉自然消失后通知玩家
     *
     * @param mine
     * @param _id
     * @param robber
     * @param grade
     * @param rebirth
     * @param awards
     */
    void onMineOver(MineBean mine, long _id, List<Integer> awards) {
        MineRole mr = roles.getOrDefault(_id, null)
        if (mr != null) {
            mr.addMineNotify(mine.key, awards)
        }

        mine.key = 0

        notifyMineChange(mine)
    }

    /**
     * 通知矿产可见范围内的玩家，某个矿产发生了变化
     * @param mine
     */
    void notifyMineChange(MineBean mine) {
        // 通知这个矿周围的玩家，矿发生了变化
        HexNode hn = getNode(mine.pos)

        if (hn != null) {
            MineInfoStruct ne = new MineInfoStruct()
            MineBean mb = new MineBean(mine)
            mb.used = mine.used
            ne.mines.add(mb)

            List<HexNode> _nodes = neighbours(hn, GameConfig.getInstance().getMine_view())
            HashMap<Long, Integer> tmpRoles = new HashMap<>()
            for (HexNode _node : _nodes) {
                // 可能 k已经在tmpRoles中 一个是身体 一个是眼睛，需要合并通知这个人的状态
                _node.getRoles().forEach({ k, v ->
                    tmpRoles.merge(k, v, { a, b ->
                        return a | b
                    })
                })
            }

            for (long key : tmpRoles.keySet()) {
                int status = tmpRoles.get(key)
                if ((status & 1) != 0) { // 能接收
                    MineRole _mr = roles.getOrDefault(key, null)
                    if (_mr != null) {
                        _mr.addNotify(ne)
//                        println("矿(" + mine.pos + ") 发生变化，通知:" + key + " 当前占领者:" + mine.name + "(" + mine.uid + ") used:" + mine.used)
                    }
                }
            }
        }
    }

    /**
     * 从文件中读取地图基本信息，构建整个地图
     *
     */
    private boolean readMap() {
        try {
            File file = ResourceUtils.getFile("file:xxkg/configs/map.data")

            long l = file.length()
            if (l > 0) {
                byte[] bytes = new byte[(int) l]
                InputStream is = new FileInputStream(file)
                is.read(bytes)
                is.close()

                mapData = JsonUtil.getMapper().readValue(bytes, MapData.class)
                println("地图行数:" + mapData.rows + ", 地图列数:" + mapData.columns)
            }
        } catch (IOException e) {
            log.error("读取地图map.data错误:" + e.message)
            return false
        }

        for (int r = 0; r < mapData.rows; ++r) {
            int r_offset = r / 2
            for (int q = -r_offset; q < (mapData.columns - r_offset); ++q) {
                HexNode n = new HexNode(q, r)
                nodes.put(n.id, n)
            }
        }

        // 启动的时刻将地图传送点做个hash记录，便于查找
        mapData.transporter.forEach({ho -> transporter.put(ho.pos, ho.level)})

        return true
    }

    /***
     * 利用本地锁，在其他地方检测本地的四级矿
     * @param func
     */
    void lookMine(HexNode _node, BiConsumer<Boolean, MineBean> func) {
        int view = GameConfig.getInstance().getMine_view()
        mines.forEach({ mine ->
            if (mine.key > 0) {
                if (distance(_node, mine.pos) <= view) {
                    func.accept(true, mine)
                } else {
                    func.accept(false, mine)
                }
            }
        })
    }

    /**
     * 攻打矿脉
     *
     * @param id
     * @param pos
     * @param level
     * @param rl
     * @return
     */
    synchronized int beatMine(long id, int pos, int level, RequestMineAction.Return rl) {
        MineRole mr = roles.getOrDefault(id, null)
        if (mr == null) {
            rl.result = "当前不在矿区,无法操作占领矿脉"
            return -1
        }

        int rtn = -1
        long peer

        def amb = mines.find { ab -> ab.pos == pos }

        if (amb == null) {
            rl.result = "此处没有矿脉,或者已经消失."
            return -1
        }

        if (amb.uid == 0) { // 空矿
            rtn = mr.occupy(amb)
            notifyMineChange(amb)
        } else {
            peer = amb.uid

            // 战斗，打人
            HerosBean hsb = DaoGame.getInstance().findHeros(id)
            HerosBean other = DaoGame.getInstance().findHeros(peer)

            FightResult r = GameServer.instance.getBattleSystem().doBattle(hsb.createHerosStruct(), other.createHerosStruct())

            rl.data = r.data
            if (r.win == 1) {  // 胜利
                rl.win = 1
                // 计算奖励
                List<Integer> awards = harvest(amb, mr.user, rl.awards)
                rtn = mr.occupy(amb)

                RoleMineBean rmb = DaoGame.getInstance().findRoleMap(peer)
                if (rmb != null) {
                    rmb.setOccupy(-1)
                    // 被打败后重置到出生点
                    rmb.reset()

                    MineAwardBean mab = rmb.getMineAward()
                    mab.key = amb.key
                    mab.info = new MineRobInfo()
                    mab.info.name = mr.name
                    mab.info.grade = mr.grade
                    mab.info.ts = (long) (System.currentTimeMillis() / 1000)
                    mab.awards.clear()
                    mab.awards.addAll(awards)
                }

                onMineOver(amb, mr, peer, rmb.getPosition(), awards)
            } else { // 失败
                println("矿区内战斗id(" + id + ") 战败于:id(" + peer + ")")
            }
        }
        return rtn
    }

    /**
     * 玩家请求进入地图
     */
    synchronized String enter(GameUser user, RoleMineBean rmb) {
        if (user.level < Defs.矿区等级) {
            return "领主等级不足,无法进入"
        }

        if (!GameConfig.instance.getMineOpen()) {
            return "矿区维护中,请稍候再进入."
        }

        /***
         * 人数太多的时候做点控制
         *
         */
        if (roles.size() > 1000) {
            return "矿区人数已达到上限"
        }

        MineRole mr = roles.getOrDefault(user.getUid(), null)
        if (mr == null) {
            mr = new MineRole(this, user)
            roles.put(user.getUid(), mr)
        }
        mr.settle()

        // 检查占领的矿脉是否还存在
        if (!checkExistsMine(user.getUid(), rmb.getOccupy())) {
            rmb.setOccupy(-1)
        }

        return ""
    }

    /**
     * 加速
     *
     * @param user
     * @param pos
     * @param level
     * @return
     */
    synchronized void accelerate(GameUser user, int pos, int level, RequestMineAccelerate.Return rl) {
        PackBean pb = DaoGame.getInstance().findPack(user.getUid())

        MineRole mr = roles.getOrDefault(user.getUid(), null)
        if (mr == null) {
            rl.result = "玩家不在矿区"
            return
        }

        def mss = mines.find { mine -> mine.pos == pos && mine.uid == user.getUid() }
        if (mss == null) {
            rl.result = "无法加速不存在的矿脉或者没被占领的矿脉"
            return
        }

        MineStoneConf msc = Readonly.getInstance().getMineStoneConfs().get(level - 1)

        int gem = mss.accelerate() * msc.cost
        if (gem > 0) {
            if (!pb.contains(Defs.钻石, gem)) {
                rl.result = ErrCode.钻石不足.name()
                return
            }
            rl.gem = gem
            user.payItem(Defs.钻石, gem, "加速矿脉")
            List<Integer> awards = harvest(mss)

            RoleMineBean rmb = DaoGame.getInstance().findRoleMap(user.getUid())
            if (rmb != null) {
                rmb.setOccupy(-1)
                MineAwardBean mab = rmb.getMineAward()
                mab.key = mss.key
                mab.awards.clear()
                mab.awards.addAll(awards)
                mab.info = null
                onMineOver(mss, user.getUid(), awards)
            }
        }
    }

    /**
     * 延长矿产时间
     * @param user
     * @param pos
     * @param level
     * @param eh
     * @return
     */
    synchronized String enhance(GameUser user, int pos, int level, int eh) {
        MineRole mr = roles.getOrDefault(user.getUid(), null)
        if (mr == null) {
            return "玩家不在矿区"
        }

        def mine = mines.find { mine -> mine.pos == pos && mine.uid == user.getUid() }
        if (mine == null) {
            return "指定的矿脉未被占领"
        }

        if (mine.enhance != 0) {
            return "矿脉只能延长一次"
        }

        mine.enhance += eh
        notifyMineChange(mine)
        return ""
    }

    /**
     * 玩家离开
     *
     * @param id
     */
    synchronized void leave(long id) {
        MineRole mr = roles.remove(id)
        if (mr != null) {
            for (long _id : mr.seeList) { // 通知可见列表的人物，自己离开
                MineRole _mr = roles.getOrDefault(_id, null)
                if (_mr != null) {
                    _mr.addPsNotify(id, -1, -1)
                    _mr.seeList.remove(id)
//                    println("玩家(" + id + ") 退出矿区，通知:" + _id)
                }
            }
            // 结点中删除自己
            mr.node.removeRole(id)
            mr.lookNode.removeRole(id)
        }
    }

    /**
     * 移动, 玩家每次只能朝指定方向移动1下
     *
     * @param id
     * @param dir
     * @return
     */
    synchronized int move(long id, int dir, int pos) {
        MineRole mr = roles.getOrDefault(id, null)
        if (mr == null) {
            return -1
        }

        // GM飞行模式过去不需要传送点标识，不受影响
        if (dir == 7) { // 瞬移
            return mr.move(pos)
        }

        HexNode hn = neighbour(mr.node, dir)
        if (hn == null) {
            return -2
        }

        // 检查移动过去的点是否是传送点, 强制的.
        int new_pos = transporter.getOrDefault(hn.id, -1);
        if (new_pos < 0) {
            mr.move(hn, dir)
            return hn.id
        } else {
            return mr.move(new_pos)
        }
    }

    /**
     * 查看指定坐标周边的信息, 将身体和眼睛分离开,存储到不同的HexNode中
     *
     * @param id
     * @param pos
     */
    synchronized void look(long id, int pos) {
        MineRole mr = roles.getOrDefault(id, null)
        if (mr == null) {
            return
        }

        HexNode hn = nodes.getOrDefault(pos, null)
        if (hn != null) {
            mr.lookMove(hn)
        }
    }

    /**
     * 结算矿脉
     *
     * @param mb
     * @return
     */
    List<Integer> harvest(MineBean mb) {
        if (mb.uid <= 0) {
            return null
        }

        // 1. 查看当前是否可以收获
        MineStoneConf msc = Readonly.getInstance().getMineStoneConfs().get(mb.key - 1)
        if (msc != null) {
            int need = mb.needToDefende(msc)

            HashMap<Integer, Integer> tmp = new HashMap<>()
            List<Integer> awards = new ArrayList<>()

            int count = need / msc.cd1
            for (int i = 0; i < count; ++i) {
                int idx = ((mb.goods[0] + i) * 2) % msc.good1.length
                int _id = msc.good1[idx]
                int _count = msc.good1[idx + 1]
                tmp.merge(_id, _count, { a, b -> a + b })
            }

            count = need / msc.cd2
            for (int i = 0; i < count; ++i) {
                int idx = ((mb.goods[1] + i) * 2) % msc.good2.length
                int _id = msc.good2[idx]
                int _count = msc.good2[idx + 1]
                tmp.merge(_id, _count, { a, b -> a + b })
            }

            count = need / msc.cd3
            for (int i = 0; i < count; ++i) {
                int idx = ((mb.goods[1] + i) * 2) % msc.good3.length
                int _id = msc.good3[idx]
                int _count = msc.good3[idx + 1]
                tmp.merge(_id, _count, { a, b -> a + b })
            }

            tmp.forEach({ k, v ->
                if (v > 0) {
                    awards.add(k)
                    awards.add(v)
                }
            })

            mb.uid = 0
            mb.name = ""
            mb.level = 0
            mb.skin = 0
            mb.power = 0
            mb.enhance = 0
            mb.used = 0
            mb.occupyTime = 0

            return awards
        }

        return null
    }

    /**
     * 抢劫矿脉，计算到当前时间为止的产出，并分成
     *
     * @param mb
     * @return
     */
    List<Integer> harvest(MineBean mb, GameUser robber, List<AwardBean> rwds) {
        // 1. 查看当前是否可以收获
        MineStoneConf msc = Readonly.getInstance().getMineStoneConfs().get(mb.key - 1)
        if (msc != null) {
            int need = mb.needToDefende(msc)
            int real = System.currentTimeMillis() / 1000 - mb.occupyTime
            if (real > need) {
                real = need
            }

            HashMap<Integer, Integer> tmp = new HashMap<>()
            List<Integer> awards = new ArrayList<>()

            int count = real / msc.cd1
            for (int i = 0; i < count; ++i) {
                int idx = ((mb.goods[0] + i) * 2) % msc.good1.length
                int _id = msc.good1[idx]
                int _count = msc.good1[idx + 1]
                tmp.merge(_id, _count, { a, b -> a + b })
            }

            count = real / msc.cd2
            for (int i = 0; i < count; ++i) {
                int idx = ((mb.goods[1] + i) * 2) % msc.good2.length
                int _id = msc.good2[idx]
                int _count = msc.good2[idx + 1]
                tmp.merge(_id, _count, { a, b -> a + b })
            }

            count = real / msc.cd3
            for (int i = 0; i < count; ++i) {
                int idx = ((mb.goods[1] + i) * 2) % msc.good3.length
                int _id = msc.good3[idx]
                int _count = msc.good3[idx + 1]
                tmp.merge(_id, _count, { a, b -> a + b })
            }

//            println("抢劫矿脉有:" + tmp.size())
            tmp.forEach({ k, v ->
                awards.add(k)
//                println("抢劫矿脉有:" + k + "," + v)
                if (v > 0) {
                    // 1. 抢劫获得34%
                    int vv = v * 0.34f
                    if (vv > 0) {
                        rwds.addAll(robber.addItem(k, vv, true, "抢劫矿"))
                    }

                    // 2. 自己剩余
                    v -= vv
                    awards.add(v)
                }
            })

            return awards
        }

        return null
    }

    /**
     * 消除指定位置的任务物体
     *
     * @param id
     * @param pos
     * @return
     */
    void removeTask(long id, int pos) {
        MineRole mr = roles.getOrDefault(id, null)
        if (mr != null) {
            mr.removeStaticSeeList(pos)
        }
    }

    /**
     * 检测某方向的邻居是否是任务物体, 如果是返回对应的坐标
     *
     * @param id
     * @param dir
     * @return
     */
    int hasTaskInDirection(long id, int dir) {
        MineRole mr = roles.getOrDefault(id, null)
        if (mr == null) {
            return -1
        }

        HexNode hn = neighbour(mr.node, dir)
        if (hn == null) {
            return -2
        }

        return hn.id
    }

    /// ======================================   以下是无锁函数，不需要锁，或者调用者已经带了锁 =============================== ///

    /**
     * 移动身体，必须移动眼睛到身体相同的位置
     * <p>
     * 处理结点内角色索引的问题:
     * 1. 之前的结点中要删除玩家
     * 2. 当前结点添加玩家
     */
    void fixRoleInNode(MineRole mr, HexNode last, HexNode lastLook) {
        long id = mr.user.getUid()

        if (last != null) {
            last.removeRole(id)
        }
        if (null != lastLook && lastLook != last) { // 相同的时候没必要删除两次
            last.removeRole(id)
        }

        // 1. 进入和移动
        mr.node.addRole(id, 3, true); // 回归身体和眼睛都在的状态
    }

    /**
     * 移动摄像机，看别的地方，更新的仅仅只是摄像机
     *
     * @param mr
     * @param last
     */
    void fixRoleInNodeLook(MineRole mr, HexNode last) {
        long id = mr.user.getUid()

        if (last != null) { // 如果之前的眼睛位置不为空

            // 1. 之前的眼睛位置和当前的身体位置不同, 直接将眼睛位置删除
            // 2. 之前的眼睛位置和当前的身体位置相同，修改这个位置为只有身体
            // 3. 修改当前眼睛的位置，增加掩码 眼睛（该情况覆盖了，如果当前眼睛位置和身体位置相同）
            if (last != mr.node) {
                last.removeRole(id)
            } else { // 如果之前的lookNode == node，则更新node的状态为身体在，眼睛不在
                mr.node.addRole(id, 2, true)
            }
        }

        // 眼睛放到指定的结点,可能这个结点也是身体的位置，反正将1接收器或进去即可
        mr.lookNode.addRole(id, 1, false)
    }

    /**
     * 身体，接收部分移动到地图的某个结点，需要通知周围所有玩家, 和眼线
     */
    void onMove(MineRole mr, int _dir) {
        // 1. 计算当前的周围玩家
        List<HexNode> _nodes = neighbours(mr.node, GameConfig.getInstance().getMine_view())
        HashMap<Long, Integer> tmpRoles = new HashMap<>()
        for (HexNode _node : _nodes) {
            // 可能 k已经在tmpRoles中 一个是身体 一个是眼睛，需要合并通知这个人的状态
            _node.getRoles().forEach({ k, v ->
                tmpRoles.merge(k, v, { a, b ->
                    return a | b
                })
            })
        }

        // 我的id
        long id = mr.user.getUid()
        int pos = mr.node.id

        // 2.

        // 2.1 检测老视野玩家处理:
        Long[] sl = new Long[mr.seeList.size()]
        mr.seeList.toArray(sl)
        for (Long _id : sl) {
            MineRole _mr = roles.getOrDefault(_id, null)

            if (_mr != null) {
                // 1. 自己以前看到的人，现在看不到
                if (!tmpRoles.containsKey(_id)) {
                    mr.seeList.remove(_id)
                    mr.addPsNotify(_id, -1, _mr.node.id)

                    // 别人不一定看到你，只有有你的时候，才能通知别人你的离开(这个情况就是他的眼睛已经离开了，只是身体在这里).
                    if (_mr.seeList.remove(id)) {
                        _mr.addPsNotify(id, -1, pos)
                    }
//                    println("ooo 玩家:" + id + " 离开视野,通知:" + _id)
                } else { // 2. 以前在,现在仍旧在
                    int st = tmpRoles.get(_id)
                    if ((st & 1) != 0) { // 并且他能接收
                        // 这里要区分，别人是否能看到你，也许之前你的眼睛看到别人了，现在正式移动进去
                        if (_mr.seeList.contains(id)) {
                            _mr.addPsNotify(id, _dir, pos)
//                            println("ooo 玩家:" + id + " 移动,方向:" + _dir + " 通知:" + _id)
                        } else {
                            _mr.addPsNotify(id, mr.node.id, mr.skin, mr.name, mr.guild, mr.level, mr.power, mr.grade)
                            _mr.seeList.add(id)
//                            println(_id + "的视野中增加玩家:" + id + " grade:" + mr.grade)
                        }
                    }
                }
            }
        }

        // 2.2 检测新视野中的玩家
        for (long key : tmpRoles.keySet()) {
            // 周围看到的自己跳过
            if (key == id) {
                continue
            }

            int status = tmpRoles.get(key)

            if (!mr.seeList.contains(key)) { // 新增视野玩家
                MineRole _mr = roles.getOrDefault(key, null)

                // 周围这个人如果能接收，才会把自己的信息发送给他（注意这里是通知别人）
                if (_mr != null && (status & 1) != 0 && !_mr.seeList.contains(id)) {
                    _mr.addPsNotify(id, mr.node.id, mr.skin, mr.name, mr.guild, mr.level, mr.power, mr.grade)
                    _mr.seeList.add(id)
//                    println(key + "的视野中增加玩家:" + id + " grade:" + mr.grade)
                }

                // 周围的这个人如果能发送，才能通知自己(注意这里是通知自己)
                if ((status & 2) != 0) {
                    mr.addPsNotify(key, _mr.node.id, _mr.skin, _mr.name, _mr.guild, _mr.level, _mr.power, _mr.grade)
                    mr.seeList.add(key)
//                    println(id + "的视野中增加玩家:" + key + " grade:" + _mr.grade)
                }
            }
        }
    }

    /***
     * 眼睛移动后，判断周围的状态
     *
     * @param mr
     */
    void onLookMove(MineRole mr) {
        // 1. 计算当前的周围玩家
        List<HexNode> _nodes = neighbours(mr.lookNode, GameConfig.getInstance().getMine_view())
        HashMap<Long, Integer> tmpRoles = new HashMap<>()
        for (HexNode _node : _nodes) {
            _node.getRoles().forEach({ k, v ->
                tmpRoles.merge(k, v, { a, b ->
                    return a | b
                })
            })
        }

        // 我的id
        long id = mr.user.getUid()

        // 2.
        Long[] sl = new Long[mr.seeList.size()]
        mr.seeList.toArray(sl)
        for (long _id : sl) {
            MineRole _mr = roles.getOrDefault(_id, null)

            if (_mr != null) {
                // 自己以前看到，现在看不到的人(因为眼睛的移动，所以不通知对方)
                if (!tmpRoles.containsKey(_id)) {
                    // 眼睛移动，去掉不再视野的人, 通知自己
                    mr.seeList.remove(_id)
                    mr.addPsNotify(_id, -1, _mr.node.id)

//                    // 对方也要去掉自己？, 只是自己看不到别人，不意味着别人看不到自己，但是这样会造成分裂
                    // 必须在每次自己移动的时候，查看别人是身体还是眼睛
//                    _mr.seeList.remove(id); // 眼睛离开了别人的视野
//                    println("玩家(" + id + ") 眼睛, 离开视野，通知:" + _id)
                }
            }
        }

        for (long key : tmpRoles.keySet()) {
            // 周围看到的自己跳过
            if (key == id) {
                continue
            }

            int status = tmpRoles.get(key)

            // 自己以前没看到的新玩家
            if (!mr.seeList.contains(key)) {
                MineRole _mr = roles.getOrDefault(key, null)

                // 周围的这个人如果能发送，才能通知自己(注意这里是通知自己)
                if ((status & 2) != 0) {
                    mr.addPsNotify(key, _mr.node.id, _mr.skin, _mr.name, _mr.guild, _mr.level, _mr.power, _mr.grade)
                    mr.seeList.add(key)
//                    // 对方视野内增加这个眼睛，但是由于眼睛只能接收，所以不需要向他发送通知
//                    // 对方视野增加眼睛的原因是 只要看得到你，你的所有信息都要上报
//                    if (!_mr.seeList.contains(id)) {
//                        _mr.seeList.add(id)
//                    }
//                    println("[" + id + "] 眼睛移动，新看到目标:" + key + " grade:" + _mr.grade)
                }
            }
        }

//        println("+ 玩家(" + mr.user.getUid() + ") 可以看见的角色:")
//        for (long _id : mr.seeList) {
//            println("+++ " + _id)
//        }
    }

    /**
     * 根据坐标位置获得对应的结点
     *
     * @param position
     * @return
     */
    HexNode getNode(int position) {
        HexNode hn = nodes.getOrDefault(position, null)
        if (hn == null) {
            return null
        }
        return hn
    }

    int distance(int a, int b) {
        HexNode hn = getNode(a)
        if (hn == null) {
            return 10000
        }

        HexNode hm = getNode(b)
        if (hm == null) {
            return 10000
        }

        return hn.hex.distance(hm.hex)
    }

    int distance(HexNode hn, int b) {
        HexNode hm = getNode(b)
        if (hm == null) {
            return 10000
        }

        return hn.hex.distance(hm.hex)
    }

    HexNode neighbour(HexNode node, int dir) {
        int pos = node.hex.neighbour(dir - 1).ID()
        return nodes.getOrDefault(pos, null)
    }

    HexNode neighbour(int position, int dir) {
        HexNode local = nodes.getOrDefault(position, null)
        if (local == null) {
            return null
        }
        return neighbour(local, dir)
    }

    /**
     * 返回距离在distance以内的所有邻居
     *
     * @param distance , 不包含中心的半径
     * @return
     */
    List<HexNode> neighbours(HexNode node, int distance) {
        List<HexNode> rtn = new ArrayList<>()
        for (int i = -distance; i <= distance; ++i) {
            for (int j = -distance; j <= distance; ++j) {
                for (int k = -distance; k <= distance; ++k) {
                    if ((i + j + k) == 0) {
                        int id = (i + node.hex.q) + (j + node.hex.r) * 1000
                        HexNode find = nodes.getOrDefault(id, null)
                        if (find != null) {
                            rtn.add(find)
                        }
                    }
                }
            }
        }

        return rtn
    }

    MapData getMapData() {
        return mapData
    }

    //// ---------------- 协议处理逻辑 ---------------------- ////

    /**
     * 展示当前矿区玩家信息
     */
    @Override
    synchronized String dumpPlayers() {
        String msg = ""
        roles.forEach({ k, v ->
            msg += "id(" + v.user.uid + ") 昵称(" + v.name + ") 身体位置[" + v.node.toString() + "] 眼睛位置[" + v.lookNode.toString() + "]\n"

            msg += v.lookNode.dumpPlayers() + "\n"

        })

        return msg
    }

    @Override
    void enter(GameUser user, RequestMineEnter proto) {
        RequestMineEnter.Return rl = new RequestMineEnter.Return()
        rl.seq = proto.seq

        RoleMineBean rmb = user.getDao().findRoleMap(user.getUid())
        rmb.flush()
        rl.refresh = rmb.getWeekDay()

        rl.result = enter(user, rmb)
        if (!Strings.isNullOrEmpty(rl.result)) {
            user.send(rl)
            return
        }

        rl.pos = rmb.getPosition()
        rl.energy = rmb.getEnergy()
        rl.count = rmb.getBuyEnergyCount()
        rl.attack = rmb.getAttackCount()
        rl.occupy = rmb.getOccupy()

        MineAwardBean mab = rmb.getMineAward()
        rl.key = mab.key
        rl.info = mab.info
        rl.awards.addAll(mab.awards)

        user.send(rl)
    }

    @Override
    void leave(GameUser user, RequestMineLeave proto) {
        RequestMineLeave.Return rl = new RequestMineLeave.Return()
        rl.seq = proto.seq

        leave(user.getUid())

        user.send(rl)
    }

    @Override
    void move(GameUser user, RequestMineMove proto) {
        RequestMineMove.Return rl = new RequestMineMove.Return()
        rl.seq = proto.seq

        RoleMineBean rmb = user.getDao().findRoleMap(user.getUid())
        if (rmb.flush()) {
            rl.result = "NEW"
            rl.energy = -1
        } else {
            if (rmb.getOccupy() >= 0) {
                rl.result = "占领矿脉期间不能移动"
            } else {
                int energy = rmb.getEnergy()
                energy -= GameConfig.getInstance().getMine_consume()
                if (energy < 0) {
                    rl.result = "没有行动力,无法移动"
                } else {
                    if (proto.dir == 7) { // 瞬移
                        int pos = move(user.getUid(), 7, proto.pos)
                        if (pos < 0) {
                            rl.result = "无法瞬移到指定点"
                        } else {
                            rl.pos = pos
                            rmb.setPosition(pos)
                            rmb.setEnergy(energy)
                            rl.energy = energy
                        }
                    } else {
                        int pos = move(user.getUid(), proto.dir, 0)

                        if (pos == -1) {
                            rl.result = "角色已经离开地图，请重新进入"
                        } else if (pos == -1) {
                            rl.result = "无法移动到那个位置"
                        } else if (pos == -3) {
                            rl.result = "占领矿脉中，无法移动"
                        } else {
                            rl.pos = pos
                            rmb.setPosition(pos)
                            rmb.setEnergy(energy)
                            rl.energy = energy
                        }
                    }
                }
            }
        }

        user.send(rl)
    }

    @Override
    void look(GameUser user, RequestMineLook proto) {
        RequestMineLook.Return rl = new RequestMineLook.Return()
        rl.seq = proto.seq

        RoleMineBean rmb = user.getDao().findRoleMap(user.getUid())
        if (rmb.flush()) {
            rl.result = "NEW"
        } else {
            look(user.getUid(), proto.pos)
        }

        user.send(rl)
    }

    /**
     *
     * 延长矿脉的时间，收获更多奖励
     *
     * @param user
     * @param proto
     */
    void enhance(GameUser user, RequestMineEnhance proto) {
        RequestMineEnhance.Return rl = new RequestMineEnhance.Return()
        rl.seq = proto.seq

        RoleMineBean rmb = user.getDao().findRoleMap(user.getUid())
        if (rmb.flush()) {
            rl.result = "NEW"
            user.send(rl)
            return
        }

        MineStoneConf msc = Readonly.getInstance().getMineStoneConfs().get(proto.level - 1)
        int[] eh = msc.add[proto.mode]

        PackBean pb = DaoGame.getInstance().findPack(user.getUid())
        if (!pb.contains(Defs.钻石, eh[1])) {
            rl.result = ErrCode.钻石不足.name()
        } else {
            rl.result = enhance(user, proto.pos, proto.level, eh[0])
            if (Strings.isNullOrEmpty(rl.result)) {
                user.payItem(Defs.钻石, eh[1], "延长矿脉")
            }
        }
        rl.gem = eh[1]

        user.send(rl)
    }

    /**
     * 矿区内购买行动力
     *
     * @param user
     * @param proto
     */
    void buyEnergy(GameUser user, RequestMineBuyEnergy proto) {
        RequestMineBuyEnergy.Return rl = new RequestMineBuyEnergy.Return()
        rl.seq = proto.seq

        RoleMineBean rmb = user.getDao().findRoleMap(user.getUid())
        if (rmb.flush()) {
            rl.result = "NEW"
            user.send(rl)
            return
        }

        int count = rmb.getBuyEnergyCount()
        rl.gem = GameConfig.getInstance().getMine_buyenergy_cost()[count]

//		if (usedCount >= 3) {
//			rl.result = "今日购买行动力已达上限"
//		} else {
        PackBean pb = user.getDao().findPack(user.getUid())
        if (!pb.contains(Defs.钻石, rl.gem)) {
            rl.result = ErrCode.钻石不足.name()
        } else {
            user.payItem(Defs.钻石, rl.gem, "购买矿区行动力")
            rmb.setEnergy(rmb.getEnergy() + GameConfig.getInstance().getMine_buyenergy_count())
            rmb.setBuyEnergyCount(count + 1)
        }
//		}

        user.send(rl)
    }

    /**
     * 矿区收获，非强制的，必须等到了时间才可以
     * @param user
     * @param proto
     */
    void harvest(GameUser user, RequestMineHarvest proto) {
        RequestMineHarvest.Return rl = new RequestMineHarvest.Return()
        rl.seq = proto.seq

        RoleMineBean rmb = user.getDao().findRoleMap(user.getUid())
        if (rmb.flush()) {
            rl.result = "NEW"
            user.send(rl)
            return
        }

        MineAwardBean mab = rmb.getMineAward()

        if (mab.awards.size() > 0) {
            rl.awards.addAll(user.addItems(mab.awards, false, "矿区收获"))
        }

        mab.awards.clear()
        mab.key = 0
        mab.info = null

        user.send(rl)
    }

    /**
     * 矿脉加速, 相当于强制收获，收获按照矿脉全部时间计算，需要花费钻石
     *
     * @param user
     * @param proto
     */
    void accelerate(GameUser user, RequestMineAccelerate proto) {
        RequestMineAccelerate.Return rl = new RequestMineAccelerate.Return()
        rl.seq = proto.seq

        RoleMineBean rmb = user.getDao().findRoleMap(user.getUid())
        if (rmb.flush()) {
            rl.result = "NEW"
        } else {
            accelerate(user, proto.pos, proto.level, rl)
        }

        user.send(rl)
    }

    /**
     * 地图物件交互
     *
     * @param user
     * @param proto
     */
    void action(GameUser user, RequestMineAction proto) {
        RequestMineAction.Return rl = new RequestMineAction.Return()
        rl.seq = proto.seq

        RoleMineBean rmb = user.getDao().findRoleMap(user.getUid())
        if (rmb.flush()) {
            rl.result = "NEW"
        } else {
            if (rmb.getOccupy() >= 0) {
                rl.result = "占领矿脉期间不能进行其他操作"
            } else {
                boolean handled = false
                int pos = 0

                switch (proto.mode) {
                    case 1: // 宝箱
                        pos = hasTaskInDirection(user.getUid(), proto.dir)
                        if (pos < 0) {
                            rl.result = "该方向没有可操作的对象"
                        } else {
                            MineTreasureStruct mts = rmb.getTreasure().getOrDefault(pos, null)
                            if (mts == null) {
                                rl.result = "宝箱不存在"
                            } else {
                                rl.awards.addAll(user.addItem(mts.chest, 1, true, "矿区"))
                                // 节日活动期间可以有新的东西出现
                                FestivalBean fb = DaoGame.getInstance().findFestival(user.uid);
                                if (fb.getMode() > 0 && fb.getFlag() == 0) {
                                    int[] b2 = fb.getB2();
                                    int rr = RandomUtil.getBetween(1, 100);
                                    if (b2.length > 0 && rr <= b2[0]) {
                                        int c = RandomUtil.getBetween(b2[2], b2[3]);
                                        rl.awards.addAll(user.addItem(b2[1], c, false, "矿区打怪节日"));
                                    }
                                }


                                rmb.getTreasure().remove(pos)
                                handled = true
                            }
                        }
                        break
                    case 2: // 怪物
                        pos = hasTaskInDirection(user.getUid(), proto.dir)
                        if (pos < 0) {
                            rl.result = "该方向没有可操作的对象"
                        } else {
                            MineMonsterStruct mts = rmb.getMonster().getOrDefault(pos, null)
                            if (mts == null) {
                                rl.result = "怪物不存在"
                            } else {
                                MineMonsterConf mmc = Readonly.instance.getMineMonsterConfs().get(mts.level - 1)
                                // 战斗
                                SceneBean sb = user.getDao().findScene(user.getUid())
                                int scene = sb.getScene() - mmc.at
                                if (scene < 1) {
                                    scene = 1
                                }
                                RaidConf rc = Readonly.getInstance().findRaid(scene)
                                HerosBean hsb = user.getDao().findHeros(user.getUid())
                                FightResult r = GameServer.getInstance().getBattleSystem().doSceneBattle(hsb.createHerosStruct(), rc.makeMineBattle(mts.hero), 0, false, 0, 0, 0, 0)
                                rl.data = r.data

                                if (r.win == 1) { // 只有胜利才能增加自己的金币与经验
                                    handled = true
                                    rl.win = 1
                                    rmb.getMonster().remove(pos)
                                    int num = RandomUtil.randomWeightIndex(mmc.num) + 1
                                    for (int j = 0; j < num; ++j) {
                                        int ratio = RandomUtil.getRandom() % 100
                                        int total = 0
                                        for (int[] d : mmc.drop) {
                                            total += d[2]
                                            if (ratio <= total) {
                                                rl.awards.addAll(user.addItem(d[0], d[1], true, "矿区"))
                                                break
                                            }
                                        }
                                    }

                                    // 节日活动期间可以有新的东西出现
                                    FestivalBean fb = DaoGame.getInstance().findFestival(user.uid);
                                    if (fb.getMode() > 0 && fb.getFlag() == 0) {
                                        int[] b2 = fb.getB2();
                                        int rr = RandomUtil.getBetween(1, 100);
                                        if (b2.length > 0 && rr <= b2[0]) {
                                            int c = RandomUtil.getBetween(b2[2], b2[3]);
                                            rl.awards.addAll(user.addItem(b2[1], c, false, "矿区打怪节日"));
                                        }
                                    }
                                }
                            }
                        }
                        break
                    case 3:
                        pos = hasTaskInDirection(user.getUid(), proto.dir)
                        if (pos < 0) {
                            rl.result = "该方向没有可操作的对象"
                        } else {
                            MineMerchantStruct mts = rmb.getMerchant().getOrDefault(pos, null)
                            if (mts == null) {
                                rl.result = "商人不存在"
                            } else {
                                MineMerchantConf mmc = Readonly.getInstance().getMineMerchantConfs().get(mts.key - 1)
                                PackBean pb = user.getDao().findPack(user.getUid())
                                if (!pb.contains(Defs.钻石, mmc.price1)) {
                                    rl.result = ErrCode.钻石不足.name()
                                } else {
                                    user.payItem(Defs.钻石, mmc.price1, "矿区商人")
                                    rl.awards.addAll(user.addItems(mmc.good, "矿区商人"))
                                    handled = true
                                    rmb.getMerchant().remove(pos)
                                }
                            }
                        }
                        break
                    case 4:
                        VipConf vc = Readonly.getInstance().findVip(user.getVip())
                        if (rmb.getAttackCount() >= vc.mattnum) {
                            rl.result = "今日矿区攻打次数已经达到上限"
                        } else {
                            int mine = beatMine(user.getUid(), proto.dir, proto.level, rl)
                            if (mine != -1) {
                                rmb.setOccupy(mine)
                            }
                            rmb.setAttackCount(rmb.getAttackCount() + 1)
                        }
                        break
                    default:
                        rl.result = "不认识的mode."
                        break
                }

                if (handled) { // 处理结束的个人信息，需要在地图上删除,下周刷新
                    removeTask(user.getUid(), pos)
                }
            }
        }
        user.send(rl)
    }
}

////////////////////// MineRole 类型 /////////////

class MineRole {
    // 索引矿区
    private GroovyMineSystem service

    public GameUser user

    /**
     * 位置, 当前处于的位置
     */
    public HexNode node

    /**
     * 当前监视的位置
     */
    public HexNode lookNode

    /**
     * 具体的骨骼id,换了皮肤会替换
     */
    public int skin

    /**
     * 昵称
     */
    public String name

    /**
     * 公会
     */
    public String guild

    /**
     * 等级
     */
    public int level

    /**
     * 战斗力
     */
    public long power

    /**
     * 觉醒等级
     */
    public int grade

    /**
     * 待通知的信息
     */
    public final List<MineInfoStruct> infos = new ArrayList<>()

    /**
     * 玩家动作通知:
     * key:是玩家的ID
     * value是行为，定义:
     * 			-1 		离开
     * 			0 		进入
     * 			1-6 	六个方向的移动
     */
    public List<PlayerBehaveStruct> ps = new ArrayList<>()

    /**
     * 地图上的静态物体可见列表
     */
    public final HashSet<Integer> sSeeList = new HashSet<>()

    /**
     * 玩家 会移动的
     */
    public final HashSet<Long> seeList = new HashSet<>()


    MineRole(GroovyMineSystem _service, GameUser _user) {
        service = _service
        user = _user
    }

    /***
     * 玩家进入矿区调用
     *
     * 返回是否刷新了地图
     */
    void settle() {
        long id = user.getUid()

        RoleBean rb = DaoGame.getInstance().findRole(id)
        this.name = rb.getNickname()
        this.skin = rb.getIcon()
        this.level = rb.getLevel()

        HerosBean hsb = DaoGame.getInstance().findHeros(id)
        if (hsb.getFakeHero() != 0) {
            this.skin = hsb.getFakeHero()
        }
        this.grade = rb.getGrade()
        this.power = GameServer.getInstance().getBattleSystem().calc_power(hsb.createHerosStruct())

        reset(0)
    }

    /**
     * 重置玩家的位置
     *
     * @param mode 0 进入 7 瞬移
     */
    void reset(int mode) {
        RoleMineBean rmb = DaoGame.getInstance().findRoleMap(user.getUid())

        HexNode local = service.getNode(rmb.getPosition())

        def last = this.node
        def lastLook = this.lookNode

        this.node = local
        this.lookNode = local

        lookStatic(rmb)

        // 玩家移动，在地图上更新结点
        service.fixRoleInNode(this, last, lastLook)
        service.onMove(this, mode)
    }

    /**
     * 朝_dir方向移动到_node结点
     *
     * @param _node
     * @param _dir
     */
    void move(HexNode _node, int _dir) {
        HexNode last = node
        HexNode lastLook = lookNode

        if (last != lastLook) { // 移动之前判断眼睛是否和身体位置重合
            lookMove(last)
        }

        node = _node
        lookNode = _node

        lookStatic(null)

        // 玩家移动，在地图上更新结点
        service.fixRoleInNode(this, last, lastLook)
        service.onMove(this, _dir)
    }

    /**
     * 直接移动到指定地点
     * @param _pos
     */
    int move(int _pos) {
        HexNode _node = service.getNode(_pos)
        if (_node == null) {
            return -1
        }

        HexNode last = node
        HexNode lastLook = lookNode

        if (last != lastLook) { // 移动之前判断眼睛是否和身体位置重合
            lookMove(last)
        }

        node = _node
        lookNode = _node

        lookStatic(null)

        // 玩家移动，在地图上更新结点
        service.fixRoleInNode(this, last, lastLook)
        service.onMove(this, 7)
        return _node.hex.ID()
    }

    /***
     * 将玩家的视线定位到指定的结点，并查看周围
     *
     * @param _node 眼睛需要移动到的目的地
     */
    void lookMove(HexNode _node) {
        HexNode lastLook = lookNode; // 之前眼睛所在的位置
        lookNode = _node; // 更新新的目的地
        service.fixRoleInNodeLook(this, lastLook); // 更新之前的眼睛位置 和 身体位置的掩码

        lookStatic(null); // 看看周围
        service.onLookMove(this)
    }

    boolean removeStaticSeeList(int pos) {
        sSeeList.remove(pos)
        return true
    }

    /**
     * 查询四周，个人任务相关的静态物体
     *
     */
    private void lookStatic(RoleMineBean rmb) {
        MineInfoStruct ne = new MineInfoStruct()

        if (rmb == null) {
            rmb = DaoGame.getInstance().findRoleMap(user.getUid())
        }

        Map<Integer, MineTreasureStruct> ts = rmb.getTreasure()
        for (int key : ts.keySet()) {
            // 可以看到的
            if (service.distance(lookNode, key) <= GameConfig.getInstance().getMine_view()) {
                synchronized (sSeeList) {
                    if (!sSeeList.contains(key)) {
                        // 添加这个物体到通知中
                        MineTreasureStruct mts = ts.get(key)
                        ne.treasures.add(mts)
                        sSeeList.add(key)
                    }
                }
            } else {
                // 任务可见列表删除，但是不通知客户端，他知道距离原来自己选择是否隐藏或者回收对象
                synchronized (sSeeList) {
                    sSeeList.remove(key)
                }
            }
        }

        Map<Integer, MineMonsterStruct> ms = rmb.getMonster()
        for (int key : ms.keySet()) {
            // 可以看到的
            if (service.distance(lookNode, key) <= GameConfig.getInstance().getMine_view()) {
                synchronized (sSeeList) {
                    if (!sSeeList.contains(key)) {
                        // 添加这个物体到通知中
                        MineMonsterStruct mts = ms.get(key)
                        ne.monsters.add(mts)
                        sSeeList.add(key)
                    }
                }
            } else {
                // 任务可见列表删除，但是不通知客户端，他知道距离原来自己选择是否隐藏或者回收对象
                synchronized (sSeeList) {
                    sSeeList.remove(key)
                }
            }
        }

        def mc = rmb.getMerchant()
        for (int key : mc.keySet()) {
            // 可以看到的
            if (service.distance(lookNode, key) <= GameConfig.getInstance().getMine_view()) {
                synchronized (sSeeList) {
                    if (!sSeeList.contains(key)) {
                        // 添加这个物体到通知中
                        MineMerchantStruct mts = mc.get(key)
                        ne.merchants.add(mts)
                        sSeeList.add(key)
                    }
                }
            } else {
                // 任务可见列表删除，但是不通知客户端，他知道距离原来自己选择是否隐藏或者回收对象
                synchronized (sSeeList) {
                    sSeeList.remove(key)
                }
            }
        }

        service.lookMine(lookNode, { flag, mine ->
            synchronized (sSeeList) {
                if (flag) {
                    if (!sSeeList.contains(mine.pos)) {
                        MineBean mb = new MineBean(mine)
                        mb.used = mine.used
                        ne.mines.add(mb)
                        sSeeList.add(mine.pos)
//                        user.info("看到矿脉:" + mine.pos + " 占领者:" + mine.uid + " 昵称:" + mine.name + " skin:" + mine.skin)
                    }
                } else {
                    sSeeList.remove(mine.pos)
                }
            }
        })

        addNotify(ne)
    }

    /**
     * 消息加入到通知列表中
     * @param ne
     */
    void addNotify(MineInfoStruct ne) {
        try {
            synchronized (infos) {
                infos.add(ne)
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    /**
     * 增加一个新的玩家行为,一定不是进入
     * @param _id
     * @param _behave
     */
    void addPsNotify(long _id, int _behave, int _pos) {
        synchronized (infos) {
            ps.add(new PlayerBehaveStruct(_id, _behave, _pos))
        }
    }

    /**
     * 抢劫通知
     * @param _key
     * @param _robber
     * @param _grade
     * @param _rebirth
     * @param _awards
     */
    void addMineNotify(int _key, String _robber, int _grade, int _rebirth, List<Integer> _awards) {
        NotifyMineFinish nmf = new NotifyMineFinish()
        nmf.key = _key
        nmf.rebirth = _rebirth
        nmf.awards = _awards

        nmf.info = new MineRobInfo()
        nmf.info.name = _robber
        nmf.info.grade = _grade
        nmf.info.ts = System.currentTimeMillis() / 1000

        user.send(nmf)

        reset(7)
    }

    /**
     * 矿脉结束通知
     * @param _key
     * @param _awards
     */
    void addMineNotify(int _key, List<Integer> _awards) {
        NotifyMineFinish nmf = new NotifyMineFinish()
        nmf.key = _key
        nmf.awards = _awards

        user.send(nmf)
    }

    /**
     * 增加一个新的玩家行为,一定是进入
     * @param _id
     */
    void addPsNotify(long _id, int _pos, int _skin, String _name, String _guild, int _level, long _power, int _grade) {
        synchronized (infos) {
            ps.add(new PlayerBehaveStruct(_id, _pos, _skin, _name, _guild, _level, _power, _grade))
        }
    }


    void update() {
        report()
    }

    /**
     * 将infos里积累的消息发送给玩家
     */
    void report() {
        NotifyMineInfo nmi = null
        synchronized (infos) {
            if (infos.size() > 0) {
                nmi = new NotifyMineInfo()
            }

            for (MineInfoStruct info : infos) {
                nmi.protos.add(info)
            }

            infos.clear()

            if (ps.size() > 0) {
                if (nmi == null) {
                    nmi = new NotifyMineInfo()
                }
                nmi.ps.addAll(ps)
                ps.clear()
            }
        }

        if (nmi != null) {
//            print("send notify mine info by user:" + user.name + " uid:" + user.uid + "\n")
            user.send(nmi)
        }
    }

    /**
     * 玩家占领矿
     */
    int occupy(MineBean mss) {
        long now = System.currentTimeMillis() / 1000

        if (mss.uid != 0) {
            mss.used += (int) (now - mss.occupyTime)
        }

        mss.uid = user.getUid()
        mss.skin = skin
        mss.level = level
        mss.power = power
        mss.name = name
        mss.occupyTime = now

        return mss.pos
    }
}

/**
 *  可能出现的问题:
 *  1. 在两个人物的边界，互相看不到，移动眼睛，看到对方，然后走人对方视野, 这个测试的是 眼睛导致seelist增加，移动进入视野后，应该继续使用这个seelist.
 *
 *  2. 让人物的眼睛和身体分别在两个不远的格子内，然后对方操作占领矿脉，或者移动，因为这些都在对方neibough中，要保证这个函数得到的眼睛和身体求|之后的结果，不能身体覆盖了眼睛，那么就不会通知了。
 *
 *
 *
 *
 *
 */
