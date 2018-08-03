package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.controller.GroupService;
import com.tumei.model.beans.GuildbagStruct;
import com.tumei.modelconf.GuildbagConf;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018-07-31
 * <p>
 * 公会红包
 */
@Data
@Document(collection = "GuildBags")
public class GuildbagBean {
    public GuildbagBean() {
    }

    public GuildbagBean(long _id) {
        id = _id;
    }

    @JsonIgnore
    @Id
    private String objectId;

    /**
     * 公会id
     */
    @Field("id")
    private Long id;

    /**
     * 本公会对应的所有红包
     */
    public List<GuildbagStruct> guildBags = new ArrayList<>();

    /**
     * 刷新本公会所有红包状态
     * 0：未生成红包
     * 1：已生成红包，未开启
     * 2：已开启红包，待领取
     * 3：已领取红包，只可读
     **/
    public synchronized void flushBag(int mode) {
        long currentTime = System.currentTimeMillis() / 1000;
        // 获取指定公会的红包信息
        guildBags = GroupService.getInstance().findGuildbags(1).guildBags;
        // 超时红包，清掉
        guildBags.stream().forEach(s -> {
            if (s.openLast > currentTime) {
                s.status = 0;
            }
            if (s.existLast > currentTime) {
                s.status = 3;
            }
        });
        // 所在可生成类型总个数
        List<Integer> modes = Readonly.getInstance().findGuildbagConfs().stream().distinct().map(s -> s.mode).collect(Collectors.toList());
        if (!modes.contains(mode)) {
            return;
        }
        // 捐献时对应的可生成类型，所生成的红包个数
        long count = guildBags.stream().filter(f -> (f.mode == mode && f.status == 1)).count();
        if (count > 0) {
            return;
        }
        // 本公会所有未开启的红包个数
        long max = guildBags.stream().filter(f -> f.status == 1).count();
        if (max == modes.size()) {
            return;
        }
        // 配置表中的对应概率
        int[] probability = Readonly.getInstance().findGuildbagConfs().get(0).condition;
        int random = RandomUtil.getBetween(1, 100);

        if (random <= probability[mode - 1]) {
            // 已生成过的未开启红包类型
            List<Integer> existMode = guildBags.stream().map(m -> m.mode).distinct().collect(Collectors.toList());
            // 所有可生成的未开启红包类型
            List<GuildbagConf> gbcs = Readonly.getInstance().findGuildbagConfs();
            // 去除已生成类型后，可生成的未开启 红包类型
            List<GuildbagConf> newList = gbcs.stream().filter(g -> (!existMode.contains(g.mode) && g.mode == 4 - mode))
                    .collect(Collectors.toList());

            random = RandomUtil.getBetween(0, newList.size() - 1);
            GuildbagConf gbc = newList.get(random);

            GuildbagStruct gbs = new GuildbagStruct();
            gbs.key = gbc.key;
            gbs.mode = gbc.mode;
            gbs.count = 10;
            gbs.openLast = System.currentTimeMillis() / 1000 + gbc.time[0];
            gbs.money = gbc.open;
            gbs.bagId = String.format("%s-%d", System.currentTimeMillis() / 1000, mode);
            gbs.status = 1;
            //GroupBean gb = GroupService.getInstance(). user.getDao().findGroup(user.getUid());
            //gbs.gid = gb.getGid();
        }
    }

    /**
     * 完成对应金额充值,开启红包
     **/
    public synchronized void open(int mode) {
        flushBag(-1);
        guildBags = GroupService.getInstance().findGuildbags(1).guildBags;
        List<GuildbagConf> gbcs = Readonly.getInstance().findGuildbagConfs();

        List<Integer> modes = gbcs.stream().map(s -> 4 - s.mode).collect(Collectors.toList());
        if (!modes.contains(mode)) {
            return;
        }
        long count = guildBags.stream().filter(f -> f.status == 1).count();
        // 存在未开启红包
        if (count > 0) {
            // 充值对应金额开启对应的红包
            GuildbagStruct open = guildBags.stream().filter(o -> o.mode == (4 - o.mode)).collect(Collectors.toList()).get(0);
            if (open != null) {
                open.openLast = 0;
                GuildbagConf gbc = gbcs.stream().filter(o -> o.key == open.key).collect(Collectors.toList()).get(0);
                open.existLast = System.currentTimeMillis() / 1000 + gbc.time[1];
                open.status = 2;
            }
        }
    }

    /**
     * 领取该红包
     **/
    public synchronized boolean receive(String bagid) {
        flushBag(-1);
        // 对应的可领取红包个数
        List<GuildbagStruct> gbsList = guildBags.stream().filter(f -> f.status == 2 && f.bagId.equalsIgnoreCase(bagid)).collect(Collectors.toList());
        if (gbsList.size() < 1) {
            return false;
        }
//        if (gbs.ids.contains(user.getUid())) {
//            return false;
//        }
//        GroupBean gb = user.getDao().findGroup(user.getUid());
//        if (gb.getGid() != gbs.gid) {
//            return false;
//        }
        // 待领取的红包未达上限，且还可以领取
        if (gbsList.size() >= 1) {
            GuildbagStruct gs = gbsList.get(0);
            // 红包超时删除
            if (gs.existLast < System.currentTimeMillis() / 1000) {
                gs.status = 3;
                return false;
            }
            List<GuildbagConf> gbcs = Readonly.getInstance().findGuildbagConfs();
            GuildbagConf gbc = gbcs.stream().filter(s -> s.key == gs.key).collect(Collectors.toList()).get(0);
            // 非第10个红包
            if (gs.count > 1) {
                // 玩家领取的奖励
                int receive = RandomUtil.getBetween(1, (gbc.reward[1] - gs.reward - gs.count));
                //user.addItem(gbc.reward[0], receive, true, "领取公会红包");
                gs.reward += receive;
                //gs.ids.add(user.getUid());
            } else {
                // 第10个红包
//                user.addItem(gbc.reward[0], gbc.reward[1] - gbs.reward, true, "领取公会红包");
            }
            --gs.count;
            // 红包领取完删除
            if (gs.count == 0) {
                gs.status = 3;
            }
        }
        return true;
    }
}
