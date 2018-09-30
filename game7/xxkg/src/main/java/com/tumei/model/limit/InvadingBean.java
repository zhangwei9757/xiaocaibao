package com.tumei.model.limit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.common.webio.AwardStruct;
import com.tumei.dto.limit.InvadingLoginDto;
import com.tumei.game.GameServer;
import com.tumei.game.services.InvadingRankService;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.limit.InvadingConf;
import com.tumei.modelconf.limit.InvrankConf;
import com.tumei.modelconf.limit.InvtotalConf;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zw on 2018/09/28
 * <p>
 * 怪兽入侵
 */
@Data
@Document(collection = "Role.Invading")
public class InvadingBean {
    @JsonIgnore
    @Id
    private String objectId;

    @JsonIgnore
    @Field("id")
    @Indexed(unique = true, name = "i_id")
    private Long id;

    /**
     * 活动期间累计充值金额,单位分
     */
    private int ChargeTotal;
    /**
     * 活动期间累计充值已领取档位列表,单位分
     * -1：表示充值金额未达标
     * 0：表示充值金额已达标未领取
     * 1：表示充值金额已达标已领取
     */
    private HashMap<Integer, Integer> awd = new HashMap<>();
    /**
     * 活动期间登陆奖励领取结果
     * -1：表示已过期需要补签
     * 0：表示未领取
     * 1：表示已领取
     * 2：表示已购买
     * 3：未到领取日期状态
     */
    private List<Integer> loginAwardsStatus = new ArrayList<>();
    /**
     * 次元碎片攻击历史所有奖励
     */
    private List<AwardBean> debrisList = new ArrayList<>();
    /**
     * 次元碎片击杀怪兽历史所有奖励
     */
    private List<List<AwardBean>> killList = new ArrayList<>();
    /**
     * 怪兽血量
     */
    private int blood;
    /**
     * 怪兽击杀次数
     */
    private int kill;
    /**
     * 怪兽复活最终时间
     */
    private long resurgence;
    /**
     * 购买次元碎片单价
     */
    private int price;
    /**
     * 最后刷新次元碎片时间
     */
    private long lastFlushDebris;
    /**
     * 活动期间第一次登陆刷新标识
     */
    private boolean firstLogin;
    /**
     * 刷新次元碎片购买价格的标识
     */
    private int lastDay;
    /**
     * 购买总次数，新的一天重置次数
     */
    private int buyTotal;
    /**
     * 最后一次参加的活动日期，下次新活动刷新标识
     */
    private long begin;
    /**
     * 记录当前活动特定的物品ID
     */
    private int actgood;

    public InvadingBean() {
    }

    public InvadingBean(long _id) {
        id = _id;
    }

    /**
     * 构建登陆奖励列表并填充领取状态、当天补签所需钻石
     *
     * @return
     */
    public List<InvadingLoginDto> createInvadingLoginDto() {
        List<InvadingLoginDto> rtn = null;
        // 登陆奖励列表
        int key = InvadingRankService.getInstance().key;
        InvadingConf ics = Readonly.getInstance().findInvadingConf(key);

        if (ics != null) {
            rtn = new ArrayList<>();
            int[][] receives = ics.logindd;
            for (int i = 0; i < receives.length; ++i) {

                InvadingLoginDto ild = new InvadingLoginDto();
                ild.status = loginAwardsStatus.get(i);
                List<AwardStruct> list = new ArrayList<>();
                for (int j = 0; j < receives[i].length; ++j) {
                    AwardStruct as = new AwardStruct(receives[i][j], receives[i][++j]);
                    list.add(as);
                }
                ild.awards = list;
                // 登陆奖励，再次购买，补签，当次活动期间，数组长度同步，共享下标取值
                ild.resign = ics.addcost[i];
                ild.ddagain = ics.ddagain[i];
                rtn.add(ild);
            }
        }
        return rtn;
    }

    /**
     * 新的活动时会清掉缓存旧的记录，保证数据实时正确性
     */
    public void clear() {
        // 当前是新的活动，上次参加的记录清掉
        ChargeTotal = 0;
        awd.clear();
        loginAwardsStatus.clear();
        debrisList.clear();
        killList.clear();
        blood = 0;
        kill = 0;
        resurgence = 0;
        price = 0;
        lastFlushDebris = 0;
        firstLogin = false;
        lastDay = 0;
        buyTotal = 0;
        // 清掉背包
        PackBean pb = DaoGame.getInstance().findPack(id);
        // 不论特定活动物品id是否一样，新的活动期间时，上次的背包此物品要清掉
        pb.getItems().remove(actgood);
    }

    /**
     * 初始化加载玩家信息
     */
    public void init() {
        int today = TimeUtil.getToday();
        long now = System.currentTimeMillis() / 1000;

        InvadingRankService lrs = InvadingRankService.getInstance();
        InvadingConf ic = Readonly.getInstance().findInvadingConf(lrs.key);
        begin = lrs.begin;
        actgood = ic.actgood;
        lastFlushDebris = now;
        lastDay = today;
        blood = Defs.怪兽入侵血量上限;
        price = Defs.怪兽入侵次元碎片单价;
        // 活动时长
        int ends = ic.end - ic.start;
        // 填充登陆奖励领取状态列表
        for (int i = 0; i <= ends; ++i) {
            loginAwardsStatus.add(0);
        }
        // 今天与开活动的差值,小于这个值的下标全部为-1，表示奖励过期，待补签
        int diff = today - ic.start;
        if (diff > 0) {
            for (int i = 0; i < diff; ++i) {
                // 过期日期全部标识为 -1
                loginAwardsStatus.set(i, -1);
            }
        }
        for (int i = diff + 1; i <= ends; ++i) {
            // 活动未开日期全部标识为 3
            loginAwardsStatus.set(i, 3);
        }
        // 首次加载把碎片击杀可得所有奖励填充,下标对应次数的奖励
        for (int i = 0; i < ic.reward.length; ++i) {
            List<AwardBean> one = new ArrayList<>();
            for (int j = 0; j < ic.reward[i].length; j += 3) {
                one.add(new AwardBean(ic.reward[i][j], 0));
            }
            killList.add(one);
        }
        // 默认所有档位累计充值奖励均未达标,要把角转化为分
        Readonly.getInstance().getInvtotalConfs().forEach(f -> awd.put(f.cost * 100, -1));
        firstLogin = true;
    }

    /**
     * 刷新次元碎片
     */
    public void flushDebris() {
        InvadingRankService lrs = InvadingRankService.getInstance();
        if (!lrs.isActive()) {
            // 如果不在活动期间，或者上次活动已过期，直接不刷新，防止配置表读取不到，因为key不存在了
            // 如果下次有活动，在刷新前会自动刷新过期数据，初始化活动信息
            return;
        }
        if (lrs.begin != begin) {
            // 新的活动清掉上次记录
            clear();
        }
        int today = TimeUtil.getToday();
        long now = System.currentTimeMillis() / 1000;
        InvadingConf ic = Readonly.getInstance().findInvadingConf(lrs.key);

        // 活动期间首次登陆
        if (!firstLogin) {
            // 首次登陆初始化玩家信息
            init();
        }

        long diff = (now - lastFlushDebris) / Defs.怪兽入侵碎片生成时间;
        if (diff > 0) {
            // 如果本身碎片未达上限，可以累加生成，已达上限，不生成
            int debris = getDebris();
            if (debris < Defs.怪兽入侵碎片上限) {
                debris += diff;
                lastFlushDebris = now - (diff % Defs.怪兽入侵碎片生成时间);
                if (debris >= Defs.怪兽入侵碎片上限) {
                    // 碎片上限50个
                    diff = Defs.怪兽入侵碎片上限 - getDebris();
                    lastFlushDebris = 0;
                }
                // 生成碎片后，添加进背包
                updateDebris((int) diff);
            } else {
                lastFlushDebris = 0;
            }
        }
        // 怪兽死亡状态下，尝试刷新复活
        if (blood <= 0) {
            flushResurgence();
        }
        // 新的一天，开始价格重置，重置总购买次数
        if (lastDay < today) {
            lastDay = today;
            price = Defs.怪兽入侵次元碎片单价;
            buyTotal = 0;
            // 要动态修改真实当天状态
            int position = today - ic.start;
            loginAwardsStatus.set(position, 0);
        }
    }

    /**
     * 获取击杀奖励
     *
     * @return -1 表示怪兽未被击杀
     */
    public int[] getKillAward(long uid, String name) {
        flushDebris();
        if (blood <= 0) {
            ++kill;
            // 击杀成功添加修改排行
            List<InvrankConf> invrankConfs = Readonly.getInstance().getInvrankConfs();
            // 最低上榜配置要求
            InvrankConf rank = invrankConfs.get(invrankConfs.size() - 1);

            // 击杀后就直接上榜
            InvadingRankService.getInstance().put(uid, name, 1L, InvadingRankService.getInstance().key);

            long now = System.currentTimeMillis() / 1000;
            resurgence = now + Defs.怪兽入侵复活间隔;

            int key = InvadingRankService.getInstance().key;
            InvadingConf ic = Readonly.getInstance().findInvadingConf(key);

            // 左id--中下限--右上限,是一多个奖励待随机
            int[] receive = null;
            if (kill <= ic.reward.length) {
                receive = ic.reward[kill - 1];
            } else {
                receive = ic.reward[ic.reward.length - 1];
            }

            // 击杀待随机的所有奖励,从对应次数奖励中随便机1个
            int max = receive.length / 3 - 1;
            int index = RandomUtil.getBetween(0, max);
            int[] realReceive = new int[3];
            // 物品id
            realReceive[0] = receive[index * 3];
            // 数量下限
            realReceive[1] = receive[index * 3 + 1];
            // 数量上限
            realReceive[2] = receive[index * 3 + 2];
            // 随机数量
            int count = RandomUtil.getBetween(realReceive[1], realReceive[2]);

            return new int[]{realReceive[0], count};
        }
        return new int[]{-1};
    }

    /**
     * 使用次元碎片攻击
     *
     * @param count
     * @return < 0 表示碎片不足  -999 重生中
     */
    public int[] getAttackAward(int count) {
        flushDebris();
        if (blood <= 0) {
            // 怪兽已死亡待复活状态
            return new int[]{-999, 0};
        }

        // 碎片数量足够
        int debris = getDebris();
        if (debris >= count) {
            // 血量 > 使用数量
            if (blood >= count) {
                debris -= count;
                blood -= count;
            } else {
                debris -= blood;
                count = blood;
                blood = 0;
            }
            // 碎片使用，修改背包
            updateDebris(-count);
        } else {
            // 碎片数量不足时，所需要购买碎片数量
            int diff = debris - count;
            return new int[]{0, diff};
        }

        int key = InvadingRankService.getInstance().key;
        InvadingConf ic = Readonly.getInstance().findInvadingConf(key);
        int[] awards = ic.usecrystal;

        int[] award = new int[count * 2];
        int max = (awards.length / 2) - 1;

        for (int i = 0; i < award.length; ++i) {
            int index = RandomUtil.getBetween(0, max);
            // 获取随机领取的奖励
            award[i] = awards[index * 2];
            award[++i] = awards[index * 2 + 1];
        }

        if (debris < Defs.怪兽入侵碎片上限 && lastFlushDebris == 0) {
            lastFlushDebris = System.currentTimeMillis() / 1000;
        }
        return award;
    }

    /**
     * 为指定奖励累计次数
     *
     * @param target
     */
    public void addList(List<AwardBean> target) {
        boolean flag = false;
        for (AwardBean t : target) {
            flag = false;
            for (AwardBean ab : debrisList) {
                if (ab.id == t.id) {
                    // 历史追加过此奖励，直接累加奖励次数
                    ab.count += t.count;
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                // 确实未有添加过这条记录，新追加一条
                debrisList.add(new AwardBean(t.id, t.count, t.hid));
            }
        }
    }

    /**
     * 购买次元碎片
     *
     * @param uid
     * @param count -1 表示钻石不足
     */
    public int buyDebris(long uid, int count) {
        flushDebris();
        int debris = getDebris();
        if (debris >= Defs.怪兽入侵碎片上限) {
            lastFlushDebris = 0;
        }
        int gem = 0;
        if (price >= Defs.怪兽入侵次元碎片价格上限) {
            gem = count * Defs.怪兽入侵次元碎片价格上限;
        } else {
            for (int i = 0; i < count; ++i) {
                gem += price;
                price += Defs.怪兽入侵次元碎片价格累加;
                if (price > Defs.怪兽入侵次元碎片价格上限) {
                    price = Defs.怪兽入侵次元碎片价格上限;
                }
            }
        }

        PackBean pb = DaoGame.getInstance().findPack(uid);
        if (!pb.contains(Defs.钻石, gem)) {
            // 钻石不足
            gem = -1;
            return gem;
        }

        pb.payItem(Defs.钻石, gem, "购买次元碎片");
        // 购买成功，添加背包数量
        updateDebris(count);
        debris += count;
        buyTotal += count;

        // 碎片达到上限值或者超过时，停止生成碎片
        if (debris >= Defs.怪兽入侵碎片上限) {
            lastFlushDebris = 0;
        }
        return gem;
    }

    /**
     * 当前活动进行中，充值金额累计
     *
     * @param rmb
     */
    public void doChargeAdd(int rmb) {
        flushDebris();
        if (InvadingRankService.getInstance().isActive()) {
            // 活动进行中添加充值累计
            ChargeTotal += rmb;
            // 充值累计后，修改对应档位领取状态,单位分
            awd.keySet().forEach(f -> {
                if (ChargeTotal >= f && awd.get(f) == -1) {
                    awd.put(f, awd.get(f) + 1);
                }
            });
        }
    }

    /**
     * 获取充值累计对应档位的奖励
     *
     * @return -1 表示配置读取失败 -2 表示不符合领取条件
     */
    public int[] getChargeAward(int rmb) {
        flushDebris();
        InvtotalConf ic = Readonly.getInstance().findInvtotalConfs(rmb / 100);// 单位为分，配置表是元。要转化为元
        if (ic == null) {
            // 获取奖励配置失败
            return new int[]{-1};
        }
        // 对应档位奖励未领取，且可以领取，发送奖励，配置单位元，要转化为分
        if (awd.get(rmb) == 0 && ChargeTotal >= ic.cost * 100) {
            int[] award = ic.reward;
            awd.put(rmb, awd.get(rmb) + 1);
            return award;
        } else {
            return new int[]{-2};
        }
    }

    /**
     * 领取活动期间登陆奖励,购买登陆奖励，补签
     *
     * @return 下标0：奖励 下标1：花费钻石
     * null： 表示不合法购买
     * -1：   表示钻石不足
     */
    public int[][] getLoginddAward(long uid, int position) {
        int[][] rtn = new int[2][2];
        int gem = 0;
        flushDebris();
        InvadingConf ic = Readonly.getInstance().findInvadingConf(InvadingRankService.getInstance().key);
        int today = TimeUtil.getToday();
        // 今天所在活动周期中第？天,对应是下标值 0-6
        int diff = today - ic.start;
        if (position > diff) {
            // 活动周期中，只能领取今天及今天以前的奖励，补签后一样规则
            return null;
        }
        int index = loginAwardsStatus.get(position);
        if (index == -1) { // 表示待补签
            gem = ic.addcost[position];
            PackBean pb = DaoGame.getInstance().findPack(uid);
            if (!pb.contains(Defs.钻石, gem)) {
                // 钻石不足
                gem -= pb.getGem();
                return new int[][]{{-gem}, {-gem}};
            }
            pb.payItem(Defs.钻石, gem, "怪兽入侵补签");
            // 补签后标识
            loginAwardsStatus.set(position, 1);
            int[] receive = ic.logindd[position];
            rtn[0] = receive;
            rtn[1] = new int[]{gem};
        } else if (index == 0) { // 表示待领取
            // 领取后标识
            loginAwardsStatus.set(position, 1);
            int[] receive = ic.logindd[position];
            rtn[0] = receive;
        } else if (index == 1) { // 表示已领取，待购买
            gem = ic.ddagain[position];
            PackBean pb = DaoGame.getInstance().findPack(uid);
            if (!pb.contains(Defs.钻石, gem)) {
                // 钻石不足
                gem -= pb.getGem();
                return new int[][]{{-gem}, {-gem}};
            }
            pb.payItem(Defs.钻石, gem, "怪兽入侵购买登陆奖励");
            // 购买后标识
            loginAwardsStatus.set(position, 2);
            int[] receive = ic.logindd[position];
            rtn[0] = receive;
            rtn[1] = new int[]{gem};
        } else if (index == 2) { // 已购买，无法有任何操作
            return null;
        } else if (index == 3) { // 在活动期间，但未到日期，不可任何操作
            return null;
        }
        return rtn;
    }

    /**
     * 刷新怪兽复活
     */
    public void flushResurgence() {
        long now = System.currentTimeMillis() / 1000;
        // 在待复活状态，且达到复活时限，复活
        if (now >= resurgence && resurgence > 0) {
            resurgence = 0;
            blood = Defs.怪兽入侵血量上限;
        }
    }

    /**
     * 购买怪兽复活
     *
     * @return -1 表示存活期间不能复活 -2 表示钻石不足
     */
    public int buyResurgence(long uid) {
        flushDebris();
        int gem = 0;
        // 怪兽存活期间不能复活
        if (resurgence == 0) {
            gem = -1;
        } else {
            PackBean pb = DaoGame.getInstance().findPack(uid);
            gem = Defs.怪兽入侵复活费用;
            // 钻石不足
            if (!pb.contains(Defs.钻石, gem)) {
                gem = -2;
                return gem;
            }

            pb.payItem(Defs.钻石, gem, "怪兽入侵复活费用");
            resurgence = 0;
            blood = Defs.怪兽入侵血量上限;
        }
        return gem;
    }

    /**
     * 发送累计充值档位奖励
     * 可领取，且未领取的奖励
     */
    public void sendMailAward() {
        List<InvtotalConf> ics = Readonly.getInstance().getInvtotalConfs();
        for (InvtotalConf ic : ics) {
            // 可领取，且未领取的，发送邮件奖励，配置表单位元，转化成分
            int cost = ic.cost * 100;
            if (ChargeTotal >= cost && awd.get(cost) == 0) {
                StringBuilder sb = new StringBuilder();
                int index = 0;
                for (int i : ic.reward) {
                    if (index > 0) {
                        sb.append(",");
                    }
                    sb.append(i);
                    ++index;
                }
                // 邮件发送后，标识已领取状态
                awd.put(cost, awd.get(cost) + 1);
                GameServer.getInstance().sendAwardMail(id, "异界入侵累充奖励", String.format("充值<color=red>%d</color>元奖励", ic.cost), sb.toString());
            }
        }
    }

    /**
     * 发送自己所在排行，可领取的奖励
     *
     * @param rank
     */
    public void sendMailAwardRank(int rank) {
        InvrankConf ic = Readonly.getInstance().findInvrankConf(rank);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ic.reward.length; ++i) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(ic.reward[i]);
        }
        GameServer.getInstance().sendAwardMail(id, "异界入侵击杀排行奖励", String.format("排名<color=red>%d</color>名奖励", rank), sb.toString());
    }

    /**
     * 指定档位是否已领取奖励
     *
     * @param rmb
     * @return ture 表示已领取 false 表示未领取
     */
    public int getReceiveStatu(int rmb) {
        flushDebris();
        return awd.get(rmb);
    }

    /**
     * 通知客户端今天是活动的第几天
     *
     * @return
     */
    public int getCurrentDay() {
        int key = InvadingRankService.getInstance().key;
        InvadingConf ic = Readonly.getInstance().findInvadingConf(key);
        int today = TimeUtil.getToday();
        // 今天所在活动周期中第？天,对应是下标值 0-6
        int day = today - ic.start + 1;
        return day;
    }

    /**
     * 新增，使用，次元碎片
     *
     * @param debris
     */
    public void updateDebris(int debris) {
        PackBean pb = DaoGame.getInstance().findPack(id);
        int key = InvadingRankService.getInstance().key;
        InvadingConf ic = Readonly.getInstance().findInvadingConf(key);
        if (debris > 0) {
            // 背包添加碎片
            pb.addItem(ic.actgood, debris, "新增怪兽入侵活动次元碎片");
        } else {
            pb.payItem(ic.actgood, -debris, "使用怪兽入侵活动次元碎片");
        }
    }

    /**
     * 获取背包碎片数量
     *
     * @return
     */
    public int getDebris() {
        int key = InvadingRankService.getInstance().key;
        InvadingConf ic = Readonly.getInstance().findInvadingConf(key);
        PackBean pb = DaoGame.getInstance().findPack(id);
        return pb.getItemCount(ic.actgood);
    }
}
