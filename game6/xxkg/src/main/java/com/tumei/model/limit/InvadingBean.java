package com.tumei.model.limit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.common.webio.AwardStruct;
import com.tumei.dto.limit.InvadingLoginDto;
import com.tumei.game.GameServer;
import com.tumei.game.services.LimitRankService;
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
 * Created by zw on 2018/09/12
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
     * 次元碎片数量
     */
    private int debris;
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
     * 上榜标识
     */
    private boolean ranking;
    /**
     * 购买总次数，新的一天重置次数
     */
    private int buyTotal;

    public InvadingBean() {}

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
        int key = LimitRankService.getInstance().key;
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
                ild.resign = ics.addcost[i];
                rtn.add(ild);
            }
        }

        return rtn;
    }

    /**
     * 刷新次元碎片
     */
    public void flushDebris() {
        int today = TimeUtil.getToday();
        long now = System.currentTimeMillis() / 1000;
        int key = LimitRankService.getInstance().key;
        InvadingConf ic = Readonly.getInstance().findInvadingConf(key);
        // 活动期间首次登陆
        if (!firstLogin) {
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
            // 首次登陆，记录自己uid
            LimitRankService.getInstance().put(id, key);
            firstLogin = true;
        }

        long diff = (now - lastFlushDebris) / Defs.怪兽入侵碎片生成时间;
        if (diff > 0) {
            // 如果本身碎片未达上限，可以累加生成，已达上限，不生成
            if (debris < Defs.怪兽入侵碎片上限) {
                debris += diff;
                lastFlushDebris = now - (diff % Defs.怪兽入侵碎片生成时间);

                if (debris >= Defs.怪兽入侵碎片上限) {
                    // 碎片上限50个
                    debris = Defs.怪兽入侵碎片上限;
                    lastFlushDebris = 0;
                }
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
            if (kill >= rank.limit) {
                if (!ranking) {
                    // 刚好达标上榜，添加记录，用真实次数创建记录
                    LimitRankService.getInstance().put(uid, name, kill, LimitRankService.getInstance().key);
                    // 上榜后标识
                    ranking = true;
                } else {
                    // 已经添加过记录，追加击杀次数1次即可
                    LimitRankService.getInstance().put(uid, name, 1L, LimitRankService.getInstance().key);
                }

            }
            long now = System.currentTimeMillis() / 1000;
            resurgence = now + Defs.怪兽入侵复活间隔;
            int key = LimitRankService.getInstance().key;
            InvadingConf ic = Readonly.getInstance().findInvadingConf(key);
            // 左id--中下限--右上限
            int[] receive = new int[3];
            if (kill == 1) {
                // 第一次击杀待随机的所有奖励
                int[] ints = ic.reward[0];
                int max = ints.length / 3 - 1;
                int index = RandomUtil.getBetween(0, max);
                // 物品id
                receive[0] = ints[index * 3];
                // 数量下限
                receive[1] = ints[index * 3 + 1];
                // 数量上限
                receive[2] = ints[index * 3 + 2];
            } else if (kill <= ic.reward.length) {
                receive = ic.reward[kill - 1];
            } else {
                receive = ic.reward[ic.reward.length - 1];
            }
            // 随机数量
            int count = RandomUtil.getBetween(receive[1], receive[2]);

            return new int[]{receive[0], count};
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
        int[] award = new int[2];
        // 碎片数量足够
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
        } else {
            // 碎片数量不足时，所需要购买碎片数量
            int diff = debris - count;
            award[1] = diff;
            return award;
        }
        int key = LimitRankService.getInstance().key;
        InvadingConf ic = Readonly.getInstance().findInvadingConf(key);
        int[] awards = ic.usecrystal;

        int max = (awards.length / 2) - 1;
        int index = RandomUtil.getBetween(0, max);

        // 获取随机领取的奖励
        award[0] = awards[index * 2];
        award[1] = awards[index * 2 + 1] * count;

        if (debris < Defs.怪兽入侵碎片上限 && lastFlushDebris == 0) {
            lastFlushDebris = System.currentTimeMillis() / 1000;
        }
        return award;
    }

    /**
     * 为指定奖励累计次数
     * @param debriss
     */
    public void addList(List<AwardBean> target, List<AwardBean> debriss) {
        if (debriss != null) {
            boolean flag = false;
            for (AwardBean ass : target) {
                flag = false;
                for (AwardBean ab : debriss) {
                    if (ab.id == ass.id) {
                        // 历史追加过此奖励，直接累加奖励次数
                        ab.count += ass.count;
                        flag = true;
                        break;
                    }
                }

                if (!flag) {
                    // 确实未有添加过这条记录，新追加一条
                    debriss.add(ass);
                }
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

        PackBean pb = DaoService.getInstance().findPack(uid);
        if (!pb.contains(Defs.钻石, gem)) {
            // 钻石不足
            gem = -1;
            return gem;
        }

        pb.payItem(Defs.钻石, gem, "购买次元碎片");
        debris += count;
        buyTotal += count;
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
        if (LimitRankService.getInstance().isActive()) {
            // 活动进行中添加充值累计
            ChargeTotal += rmb;
            // 充值累计后，修改对应档位领取状态,单位分
            awd.keySet().forEach(f -> {
                if (ChargeTotal >= f && awd.get(f) == -1) {
                    awd.put(f,awd.get(f) + 1);
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
        InvtotalConf ic = Readonly.getInstance().findInvtotalConfs(rmb / 100);// 单位为分，配置表是元。要转化为分
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
     *  null： 表示不合法购买
     *  -1：   表示钻石不足
     */
    public int[][] getLoginddAward(long uid, int position) {
        int[][] rtn = new int[2][2];
        int gem = 0;
        flushDebris();
        InvadingConf ic = Readonly.getInstance().findInvadingConf(LimitRankService.getInstance().key);
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
            PackBean pb = DaoService.getInstance().findPack(uid);
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
            return rtn;
        } else if (index == 0) { // 表示待领取
            // 领取后标识
            loginAwardsStatus.set(position, 1);
            int[] receive = ic.logindd[position];
            rtn[0] = receive;
            return rtn;
        } else if (index == 1) { // 表示已领取，待购买
            gem = ic.ddagain[position];
            PackBean pb = DaoService.getInstance().findPack(uid);
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
            return rtn;
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
            PackBean pb = DaoService.getInstance().findPack(uid);
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
        int key = LimitRankService.getInstance().key;
        InvadingConf ic = Readonly.getInstance().findInvadingConf(key);
        int today = TimeUtil.getToday();
        // 今天所在活动周期中第？天,对应是下标值 0-6
        int day = today - ic.start + 1;
        return day;
    }
}
