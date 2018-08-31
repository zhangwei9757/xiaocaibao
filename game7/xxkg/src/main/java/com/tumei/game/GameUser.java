package com.tumei.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.centermodel.ReceiptBean;
import com.tumei.centermodel.ReceiptBeanRepository;
import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.common.RemoteService;
import com.tumei.common.group.GroupRoleMessage;
import com.tumei.common.utils.*;
import com.tumei.dto.arena.ArenaRoleDto;
import com.tumei.game.protos.notifys.NotifyCharge;
import com.tumei.game.protos.notifys.NotifySceneEvent;
import com.tumei.game.protos.notifys.NotifyUserInfo;
import com.tumei.game.protos.structs.RaidRankStruct;
import com.tumei.game.protos.structs.RankStruct;
import com.tumei.game.protos.structs.RobStruct;
import com.tumei.game.services.*;
import com.tumei.model.*;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.model.festival.FestivalBean;
import com.tumei.modelconf.*;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;

import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by leon on 2016/12/31.
 * <p>
 * 虚拟玩家
 */
@Component("GameUser")
@Scope("prototype")
public class GameUser extends WebSocketUser {
    private static final Log log = LogFactory.getLog(GameUser.class);

    @Autowired
    private DaoGame dao;
    /**
     * 服务器句柄
     */
    @Autowired
    private GameServer server;

    @Autowired
    private ReloadableResourceBundleMessageSource rrbm;

    @Autowired
    private ReceiptBeanRepository receiptBeanRepository;

    @Autowired
    private RankService rankService;

    /**
     * 燃烧远征的排名系统
     */
    @Autowired
    private FireRaidRankService raidRanks;

    @Autowired
    @Qualifier(value = "simple")
    protected RestTemplate simpleTemplate;


    /**
     * 当前的昵称
     */
    private String name;

    private int level;

    /**
     * 当前vip等级
     */
    private int vip;

    /**
     * 当前的vip经验
     */
    private int vipexp;

    /**
     * 是否今日创建的玩家
     */
    private boolean old;

    /**
     * 最近计算的战斗力
     */
    private long power;
    /**
     * key为协议， value是时间
     */
    private HashMap<String, Long> protoTime = new HashMap<>();

    /**
     * 临时登录的变量 上次临时记录的抢劫信息
     */
    public List<RobStruct> tmpRobs;
    /**
     * 临时记录抢劫的物品
     */
    public int tmpRobItem;
    /**
     * 临时保存的上次竞技场对手
     */
    public List<RankStruct> tmpPeers;
    /**
     * 缓存公会等级
     */
    public int guildLevel;
    /**
     * 上次访问的公会副本进度，临时的
     */
    public int guildScene;
    /**
     * 上次访问的公会副本进度
     */
    public int[] guildProgress;

    /**
     * 公会红包捐献指定范围
     **/
    private final List<Integer> rechares = Arrays.asList(19800, 32800, 64800);

    public int getZone() {
        return server.getZone();
    }

    public GameUser() {
    }

    @Override
    public void authenticate(WebSocketSession session) throws Exception {
        super.authenticate(session);

        /**
         * 帐号id * 1000 + 服务器id == 角色id
         */
        uid = (Long) socketSession.getAttributes().get("uid") * 1000 + server.getZone();
//		log.warn("***** establish session user:" + getName() + " uid:" + uid);
    }


    /**
     * 判断协议，与协议之间的间隔
     *
     * @param proto
     * @param interval
     * @return
     */
    public boolean judegeProtocolInterval(BaseProtocol proto, int interval) {
        long now = System.currentTimeMillis() / 1000;
        if (interval > 0) {
            long then = protoTime.getOrDefault(proto.getProtoType(), 0L);
            if (now - then < interval) { // 两次发送协议的间隔太短
                return false;
            }
        }
        protoTime.put(proto.getProtoType(), now);
        return true;
    }

    @Override
    public void onAdd() {
    }

    @Override
    public synchronized void onDelete() {
        log.info("玩家(" + this.uid + ")退出.");
        server.getMineSystem().leave(uid);

        RoleBean rb = dao.findRole(uid);
        if (rb != null) {
            Date d = new Date();
            int diff = (int) (d.getTime() - rb.getLogtime().getTime());
            rb.setTotaltime(rb.getTotaltime() + (diff / 1000));
            rb.setLogouttime(d);
            // 更新今日游戏时间
            updateTodayPlayTime();

            FriendsBean fsb = dao.findFriends(uid);
            fsb.notifyAllFriends(power, rb);
        }
    }

    /**
     * 定时任务
     * <p>
     * 刷新副本的能量和玩家的精力
     */
    public synchronized void update() {
        SceneBean sb = dao.findScene(uid);
        sb.updateEnergy(0);

        if (canHarvest && sb.getScene() > 0) {
            sb.harvest(this);
        }

        if (++cum >= 5) {
            cum = 0;

            NotifyUserInfo nui = new NotifyUserInfo();
            PackBean pb = dao.findPack(uid);
            nui.spirit = pb.flushSpirit(0);
            nui.energy = sb.getEnergy();

            nui.gem = pb.getGem();
            nui.gold = pb.getCoin();

            RoleBean rb = dao.findRole(uid);
            nui.level = rb.getLevel();
            nui.exp = rb.getExp();

            nui.ts = System.currentTimeMillis();

            send(nui);
        }
    }


    /**
     * 角色生成后，根据客户端选择的性别进行初始化
     */
    public HeroBean initRole(int sex) {
        RoleBean rb = dao.findRole(uid);
        // 已经初始化过后的角色不能再次初始化
        if (rb.getIcon() != 0) {
            return null;
        }

        rb.setIcon(sex * 100 + 90010);
        rb.setSex(sex);

        HerosBean hsb = dao.findHeros(uid);
        return hsb.addFirstHero(rb.getIcon());
    }

    /**
     * 获取今天游戏时间
     *
     * @return
     */
    public void updateTodayPlayTime() {
        RoleBean rb = dao.findRole(uid);
        long logTime = rb.getLogtime().getTime();
        // 登录时间
        LocalDateTime dt = LocalDateTime.ofEpochSecond(logTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime now = LocalDateTime.now();

        // 跨年日期相等，基本不考虑
        if (dt.getDayOfYear() != now.getDayOfYear()) {
            // 这里表示已经跨天了，所以时间要从今天开始算起
            // 获取今天的milliseconds 今天凌晨的时间
            long secs = now.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toEpochSecond(ZoneOffset.UTC);
            rb.setTodaytime((int) secs);
        } else {
            long secs = now.toEpochSecond(ZoneOffset.UTC) - logTime / 1000;
            rb.setTodaytime(rb.getTodaytime() + (int) secs);
        }

//		return rb.getTodaytime();
    }

    public DaoGame getDao() {
        return dao;
    }

    public void setDao(DaoGame dao) {
        this.dao = dao;
    }

    /**
     * 触发小事件
     */
    public void triggerSceneEvent(RaidConf rc, long gold, long exp) {
        NotifySceneEvent nse = new NotifySceneEvent();
        nse.gold = gold;
        nse.exp = exp;
        if (exp > 0) {
            addLordExp(exp);
        }
        if (gold > 0) {
            addItem(金币, gold, false, "小事件:" + rc.key + "关");
        }

        // 节日活动期间可以有新的东西出现
        FestivalBean fb = DaoGame.getInstance().findFestival(uid);
        if (fb.getMode() > 0) {
            int[] b1 = fb.getB1();
            if (b1 != null) {
                int rr = RandomUtil.getBetween(1, 100);
                if (b1.length > 0 && rr <= b1[0]) {
                    int c = RandomUtil.getBetween(b1[2], b1[3]);
                    nse.rewards.addAll(addItem(b1[1], c, false, "挂机节日"));
                }
            }
        }
        send(nse);
    }

    /**
     * 触发大事件
     */
    public void triggerSceneBigEvent(RaidConf rc, int treasure, long gold, long exp) {
        NotifySceneEvent nse = new NotifySceneEvent();
        nse.event = 1;
        nse.gold = gold;
        nse.exp = exp;
        if (exp > 0) {
            addLordExp(exp);
        }
        if (gold > 0) {
            addItem(金币, gold, false, "大事件:" + rc.key);
        }
        if (treasure > 0) {
            nse.rewards.addAll(addItem(treasure, 1, true, "挂机:" + rc.key));
        }
        send(nse);
    }

    private int cum = 0;

    @JsonIgnore
    private volatile boolean canHarvest = false;

    public void setCanHarvest(boolean flag) {
        canHarvest = flag;
    }

    /**
     * 挂机增加领主经验
     *
     * @param exp
     */
    public synchronized void addLordExp(long exp) {
        RoleBean rb = dao.findRole(uid);
//		warn("当前领主等级(" + rb.getLevel() + ") 经验(" + exp + "),增加领主经验:" + rb.getExp() + " 增加经验:" + exp);
        if (rb.addExp(exp)) {
            rankService.putLevel(uid, rb.getLevel());
            setLevel(rb.getLevel());
        }
//		warn("之后领主等级(" + rb.getLevel() + ") 经验(" + exp + "),增加领主经验:" + rb.getExp());
    }

    public List<AwardBean> addItems(int[] awds, boolean _open, String reason) {
        return addItems(awds, 1, _open, reason);
    }

    public List<AwardBean> addItems(int[] awds, String reason) {
        return addItems(awds, 1, false, reason);
    }

    public List<AwardBean> addItems(int[] awds, int ratio, boolean _open, String reason) {
        List<AwardBean> awards = new ArrayList<>();
        for (int i = 0; i < awds.length; i += 2) {
            awards.addAll(addItem(awds[i], awds[i + 1] * ratio, _open, reason));
        }
        return awards;
    }

    public List<AwardBean> addItems(List<Integer> awds, boolean _open, String reason) {
        return addItems(awds, 1, _open, reason);
    }

    public List<AwardBean> addItems(List<Integer> awds, int ratio, boolean _open, String reason) {
        List<AwardBean> awards = new ArrayList<>();
        for (int i = 0; i < awds.size(); i += 2) {
            awards.addAll(addItem(awds.get(i), awds.get(i + 1) * ratio, _open, reason));
        }
        return awards;
    }

    /**
     * 增加物品，3个数字一组，第一个是id,第二个和第三个是下限和上限
     *
     * @param awds
     * @param _open
     * @param reason
     * @return
     */
    public List<AwardBean> addRangeItems(int[] awds, boolean _open, String reason) {
        List<AwardBean> awards = new ArrayList<>();
        for (int i = 0; i < awds.length; i += 3) {
            int c = RandomUtil.getBetween(awds[i + 1], awds[i + 2]);
            awards.addAll(addItem(awds[i], c, _open, reason));
        }
        return awards;
    }

    public List<AwardBean> addItems(String awds, String reason) {
        List<AwardBean> awards = new ArrayList<>();

        String[] fields = awds.split(",");

        for (int i = 0; i < fields.length; i += 2) {
            int id = Integer.parseInt(fields[i]);
            long count = Long.parseLong(fields[i + 1]);
            awards.addAll(addItem(id, count, true, reason));
        }
        return awards;
    }

    public List<AwardBean> addHero(int id, int count, String reason) {
        List<AwardBean> rtn = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            rtn.addAll(addItem(id, 1, false, reason));
        }
        return rtn;
    }

    /**
     * 自动开宝箱,加强版，不论id是什么
     *
     * @param id    物品id
     * @param count 物品数量
     * @param auto  是否自动开箱
     * @return 返回列表给客户端
     */
    public List<AwardBean> addItem(int id, long count, boolean auto, String reason) {
        List<AwardBean> rl = new ArrayList<>();
        if (count < 0) {
            throw new RuntimeException("addItem 数量不能小于等于0, 原因:" + reason + " count:" + count);
        } else if (count == 0) {
            return rl;
        }

        if (id == 经验) {
            addLordExp(count);
            rl.add(new AwardBean(id, count, 0));
            return rl;
        }

        PackBean pb = dao.findPack(uid);

        if (Defs.isClothID(id)) {                    // 时装
            HerosBean hsb = dao.findHeros(uid);

            if (!hsb.hasSkin(id)) {
                hsb.addSkin(id, reason);
                rl.add(new AwardBean(id, 1, 0));
                count = count - 1;
            }

            MaskConf mc = Readonly.getInstance().findMask(id);
            if (count > 0) {
                if (mc.pdd.length > 0) {
                    pb.addItem(mc.pdd[0], mc.pdd[1] * count, reason);
                    rl.add(new AwardBean(mc.pdd[0], mc.pdd[1] * count, 0));
                }
            }
            return rl;
        } else if (Defs.isEquipID(id) || Defs.isTreasureID(id)) { // 装备与宝物
            for (int nn = 0; nn < count; ++nn) {
                EquipBean eb = pb.addEquip(id, reason);
                if (eb != null) {
                    rl.add(new AwardBean(id, 1, eb.getEid()));
                }
            }
            return rl;
        } else if (Defs.isHeroID(id)) {        // 英雄
            for (int nn = 0; nn < count; ++nn) {
                HeroBean hb = pb.addHero(id, reason);
                if (hb != null) {
                    rl.add(new AwardBean(id, 1, hb.getHid()));
                }
            }
            return rl;
        } else if (Defs.isRelic(id)) { // 圣物整的
            HerosBean hb = dao.findHeros(uid);
            if (hb.addRelic(id) == null) {
                // 证明已经有该圣物了，所以分成25个碎片加进去
                pb.addItem(id + 1, 25, reason);
            }
        } else if (auto) {
            //查找宝箱记录，然后拆开宝箱
            ChestConf cc = Readonly.getInstance().findChest(id);
            if (cc != null) {
                if (cc.mode == 0) {
                    for (int nn = 0; nn < count; ++nn) {
                        int ratio = RandomUtil.getRandom() % 100;
                        int total = 0;
                        int i = 0;
                        for (; i < cc.rate.length; ++i) {
                            total += cc.rate[i];
                            if (ratio < total) {
                                break;
                            }
                        }
                        int[][] awards = cc.box1;
                        switch (i) {
                            case 1:
                                awards = cc.box2;
                                break;
                            case 2:
                                awards = cc.box3;
                                break;
                        }

                        int idx = RandomUtil.getRandom() % awards.length;
                        id = awards[idx][0];
                        int mod = (awards[idx][2] - awards[idx][1]);
                        if (mod <= 0) {
                            mod = 1;
                        }
                        int coc = (RandomUtil.getRandom() % mod) + awards[idx][1];
                        rl.addAll(addItem(id, coc, false, reason));
                    }
                }
                return rl;
            }
        }

        pb.addItem(id, count, reason);
        rl.add(new AwardBean(id, count, 0));
        return rl;
    }

    public boolean payItem(int[] goods, int count, String reason) {
        boolean rtn = true;
        for (int i = 0; i < goods.length; i += 2) {
            if (this.payItem(goods[i], goods[i + 1] * count, reason) < goods[i + 1]) {
                rtn = false;
            }
        }
        return rtn;
    }

    public long payItem(int key, long val, String reason) {
        if (val < 0) {
            throw new RuntimeException("PackBean::payItem 不能小于0");
        }

        PackBean pb = dao.findPack(uid);
        long consum = pb.payItem(key, val, reason);
        if (consum < 0) {
            return -1;
        }

        int v = (int) consum;

        switch (key) {
            case 初级精炼石:
                pushDailyTask(3, v);
                break;
            case 中级精炼石:
                pushDailyTask(3, v);
                break;
            case 高级精炼石:
                pushDailyTask(3, v);
                break;
            case 极品精炼石:
                pushDailyTask(3, v);
                break;
        }
        return consum;
    }


    /**
     * 判断是否刷新日常任务
     */
    public DailyTaskBean flushDailyTask() {
        DailyTaskBean dtb = dao.findDailyTask(uid);
        int now = TimeUtil.getToday();
        if (dtb.getLastFlushDay() != now) { // 是否更新当前任务
            dtb.flush(vip);
            dtb.setLastFlushDay(now);
        }
        return dtb;
    }

    /**
     * 推送完成的任务指标
     *
     * @param task 日常任务id
     * @param val  数值
     */
    public void pushDailyTask(int task, int val) {
        DailyTaskBean dtb = flushDailyTask();
        dtb.step(task, val);
    }

    /**
     * 当前排名
     *
     * @return
     */
    public RankBean getRank() {
        return LocalArenaService.getInstance().getRank(uid);
    }

    public RankBean getRankPeer(long id) {
        return LocalArenaService.getInstance().getRank(id);
    }

    /**
     * 获取远征排名
     *
     * @return
     */
    public List<RaidRankStruct> getRaidRanks() {
        RoleBean rb = dao.findRole(uid);
        return raidRanks.getRanks(uid, rb.getNickname());
    }

    /**
     * 更新远征排名
     *
     * @param star
     */
    public void fixFireRaidRank(int star) {
        raidRanks.fixRank(this.uid, this.name, star);
        rankService.putStar(uid, star);
    }

    public Map<Integer, Integer> getArenaStoreLimit() {
        RankBean rb = getRank();
        if (rb.flush()) {
            LocalArenaService.getInstance().dirty(uid);
        }
        return rb.getStoreLimits();
    }

    public void addArenaStoreCount(int key, int val) {
        RankBean rb = getRank();
        rb.addStoreLimit(key, val);
        LocalArenaService.getInstance().dirty(uid);
    }

    /**
     * 获取自身最高排名
     *
     * @return
     */
    public int getPeekRank() {
        RankBean rb = getRank();
        return rb.getPeek();
    }

    public List<RankStruct> getTopRanks(int count) {
        return LocalArenaService.getInstance().getTopRanks(count);
    }

    /**
     * 根据当前排名，获取对应的对手
     *
     * @return
     */
    public List<RankStruct> getArenaPeers(int rank) {
        tmpPeers = LocalArenaService.getInstance().getPeers(rank);
        return tmpPeers;
    }

    /**
     * 判断uid对应的rank < id对应的rank,并且id对应的rank符合
     *
     * @param id
     * @param rank
     * @return
     */
    public boolean judgeRank(long id, int rank) {
        return LocalArenaService.getInstance().judge(uid, id, rank);
    }

    public RobBean findRob() {
        return RobService.getInstance().findRob(uid);
    }

    /**
     * 创建公会需要的成员属性
     *
     * @return
     */
    public GroupRoleMessage createGroupRole() {
        GroupRoleMessage grm = new GroupRoleMessage();
        grm.id = uid;
        RoleBean rb = dao.findRole(uid);
        grm.name = rb.getNickname();
        grm.icon = rb.getIcon();
        grm.level = rb.getLevel();
        grm.power = getPower();
        grm.vip = rb.getVip();

        HerosBean hsb = dao.findHeros(uid);
        Arrays.stream(hsb.getHeros()).forEach(hb -> {
            if (hb != null) {
                grm.heros.add(hb.getId());
            }
        });
        return grm;
    }

    /**
     * 更新最近登录的服务器
     */
    public void updateLastesLogonServer(RoleBean rb) {
        RemoteService.getInstance().updateLatestServer(uid, server.getZone(), rb.getLevel(), rb.getIcon(), rb.getVip(), rb.getNickname());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        rankService.flushInfo(uid, name);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip, int exp) {
        this.vip = vip;
        this.vipexp = exp;
    }

    /**
     * 计算战斗力
     *
     * @param hsb
     * @return
     */
    public long calcPower(HerosBean hsb) {
        if (hsb == null) {
            hsb = DaoGame.getInstance().findHeros(this.uid);
        }

        long tmp = server.getBattleSystem().calc_power(hsb.createHerosStruct());
        if (tmp > 0) {
            power = tmp;
        }

        rankService.putPowers(this.uid, power);
        return power;
    }

    public long getPower() {
        return power;
    }

    public boolean isOldUser() {
        return old;
    }

    public void SetOldUser(boolean isOld) {
        old = isOld;
    }

    /**
     * 等级玩家的登录
     */
    public void StaUserLog() {
        DataStaBean dsb = dao.findDataSta(TimeUtil.getToday());
        if (dsb != null) {
            if (old) {
                dsb.addOldUser(this.uid);
            } else {
                dsb.addNewUser(this.uid);
            }
        }
    }

    /**
     * 登记玩家的充值
     *
     * @param rmb
     */
    public void StaUserCharge(int rmb) {
        DataStaBean dsb = dao.findDataSta(TimeUtil.getToday());
        if (dsb != null) {
            if (old) {
                dsb.addOldCharge(rmb);
            } else {
                dsb.addNewCharge(rmb);
            }
        }
    }

    /**
     * 登录的时候刷新一下是否有未完成的充值
     */
    public void flushCharge() {
        List<ReceiptBean> tmp = new ArrayList<>();
        // 查询所有自己id下的状态为0的单据，将其充值处理
        List<ReceiptBean> rbs = receiptBeanRepository.findByUidAndStatusAndZone(this.uid, 0, server.getZone());
        for (ReceiptBean rb : rbs) {
            rb.status = 1;
            charge(rb);
            receiptBeanRepository.save(rb);

            if (rb.source.equals("quick") && !rb.bundle.equals("com.qjzj.jiahe.yunding")) {
                tmp.add(rb);
            }
        }

        int ts = (int) (System.currentTimeMillis() / 1000);
        for (ReceiptBean _rb : tmp) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("sign=" + MD5Util.encode("NXSbyUDnv7TnP844lyo1tP96vVmD41sC" + ts));
                sb.append("&timestamp=" + ts);
                sb.append("&app_id=5124");
                sb.append("&server_id=" + server.getZone());
                sb.append("&account_id=" + (uid / 1000));
                sb.append("&role_id=" + uid);
                sb.append("&role_name=" + URLEncoder.encode(name, "UTF-8"));
                sb.append("&role_level=" + level);
                sb.append("&vip_level=" + vip);
                sb.append("&vip_exp=" + vipexp);
                sb.append("&order_id=" + _rb.tid);
                sb.append("&iap_id=" + _rb.rmb);
                sb.append("&iap_desc=" + _rb.rmb);
                sb.append("&currency_amount=" + ((double) _rb.rmb) / 100.0);
                sb.append("&virtual_currency_amount=" + _rb.gem);
                sb.append("&virtual_currency_amount_ex=" + _rb.exgem);

//				log.info("data:[" + sb.toString() + "].");

                String result = HttpUtils.sentPost("http://td.go.cc/pay_server", sb.toString(), "UTF-8"); // 请求验证服务端
                log.info("云顶支付通知:" + result);
            } catch (Exception ex) {
                log.error("通知云顶服务器，支付成功信息失败:" + ex.getMessage());
            }
        }
    }

    /**
     * 充值
     *
     * @return
     */
    public void charge(ReceiptBean receiptBean) {
        try {
            long uid = receiptBean.uid;
            int rmb = receiptBean.rmb;

            log.info("+++ 准备给玩家(" + uid + ")[" + name + "]充值(" + rmb + ")");
            RoleBean rb = DaoGame.getInstance().findRole(uid);

            NotifyCharge rci = new NotifyCharge();
            rci.product = receiptBean.good;
            rci.tid = receiptBean.tid;
            rci.rmb = rmb;
            rci.vip = rb.getVip();
            rci.vipexp = rb.getVipexp();

            ChargeBean cb = DaoGame.getInstance().findCharge(uid);
            rci.gem = cb.doCharge(rmb, receiptBean);
            cb.checkSendCards();

            ActivityBean ab = dao.findActivity(uid);
            // 调整vip等级与经验
            rci.vipexp += rmb / 10; // 继续累计经验，永远是充值的价格除以10
            receiptBean.exp = rmb / 10;
            VipConf vc = Readonly.getInstance().findVip(rb.getVip() + 1);
            while (vc != null && rci.vipexp >= vc.num) {
                rci.vipexp -= vc.num;
                rci.vip += 1;
                ab.setVipDailyBag(0);
                vc = Readonly.getInstance().findVip(rci.vip + 1);
            }
            rb.setVip(rci.vip);
            rb.setVipexp(rci.vipexp);

            setVip(rb.getVip(), rb.getVipexp());

            // 测试充值不要统计进去
            if (receiptBean.sandbox == 0) {
                StaUserCharge(rmb);
            }

            send(rci);
            log.info("+++ 成功给玩家(" + uid + ")[" + name + "]充值(" + rmb + ")");
            rankService.putCharge(this.uid, rmb);

            // 不符指定金额，无法生成
            int index = rechares.indexOf(rmb);
            if (index == -1) {
                return;
            }
            // 玩家充值完成,触发公会红包开启
            GroupBean gb = dao.findGroup(uid);
            // 支付验证
            if (gb.getGid() != 0) {
                RemoteService.getInstance().askPaymentValidation(gb.getGid(), uid, 3 - index, name);
            }

        } catch (Exception ex) {
            log.error("本地充值异常:" + ex.getMessage());
            ex.printStackTrace();
        }
    }


    /**
     * 提交跨服竞技场信息
     */
    public void submitArenaInfo() {
        ArenaRoleDto ard = new ArenaRoleDto();
        RoleBean rb = DaoGame.getInstance().findRole(this.uid);
        ard.uid = this.uid;
        ard.name = rb.getNickname();
        ard.level = rb.getLevel();
        ard.icon = rb.getIcon();
        ard.grade = rb.getGrade();

        HerosBean hsb = DaoGame.getInstance().findHeros(this.uid);
        ard.info = hsb.createHerosStruct();
        ard.power = calcPower(hsb);

        RemoteService.getInstance().arenaSubmitInfo(ard);
    }

    /**
     * i18n 进行语言切换
     *
     * @param msg
     * @param params
     * @return
     */
    public String getMessage(String msg, Object[] params) {
        return rrbm.getMessage(msg, params, Locale.getDefault());
    }
}
