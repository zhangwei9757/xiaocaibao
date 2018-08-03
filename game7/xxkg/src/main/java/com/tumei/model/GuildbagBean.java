package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameUser;
import com.tumei.model.beans.GuildbagStruct;
import com.tumei.modelconf.GuildbagConf;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018-07-31
 * <p>
 * 公会红包
 */
@Data
@Document(collection = "Role.GuildBag")
public class GuildbagBean {
    public GuildbagBean() {
    }

    public GuildbagBean(long _id) {
        id = _id;
    }

    @JsonIgnore
    @Id
    private String objectId;

    @Field("id")
    private Long id;

    /**
     * 未开启的所有红包
     **/
    private volatile List<GuildbagStruct> waitOpen;
    /**
     * 开启后可领取的所有红包
     **/
    private volatile List<GuildbagStruct> waitReceive;

    /**
     * 根据用户捐献类型，随机生成红包
     * 捐献类型：mode
     * 1: 普通捐献
     * 2: 中极捐献
     * 3: 高级捐献
     **/
    public synchronized void flush(GameUser user, int mode) {

        waitOpen = waitOpen.stream().filter(s -> s.openLast > System.currentTimeMillis() / 1000).collect(Collectors.toList());
        waitReceive = waitReceive.stream().filter(s -> s.existLast > System.currentTimeMillis() / 1000).collect(Collectors.toList());

        List<Integer> modes = Readonly.getInstance().getGuildbagConfs().stream().distinct().map(s -> s.mode).collect(Collectors.toList());
        if (!modes.contains(mode)) {
            return;
        }
        if (waitOpen.size() == modes.size()) {
            return;
        }

        int[] probability = Readonly.getInstance().getGuildbagConfs().get(0).condition;
        int random = RandomUtil.getBetween(1, 100);

        if (random <= probability[mode - 1]) {
            // 已生成过的未开启红包类型
            // List<Integer> existKey = waitOpen.stream().map(w -> w.key).collect(Collectors.toList());
            List<Integer> existMode = waitOpen.stream().map(w -> w.mode).distinct().collect(Collectors.toList());
            // 所有可生成的未开启红包类型
            List<GuildbagConf> gbcs = Readonly.getInstance().getGuildbagConfs();
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
            GroupBean gb = user.getDao().findGroup(user.getUid());
            gbs.gid = gb.getGid();
        }
    }

    /**
     * 完成对应金额充值,开启红包
     **/
    public synchronized void open(GameUser user, int key) {
        flush(user, -1);
        GuildbagBean gbb = DaoGame.getInstance().findGuildbagBean(user.getUid());
        List<GuildbagConf> gbcs = Readonly.getInstance().getGuildbagConfs();

        List<Integer> keys = gbcs.stream().map(s -> s.key).collect(Collectors.toList());
        if (!keys.contains(key)) {
            return;
        }
        // 存在未开启红包
        if (gbb.waitOpen.size() > 0) {
            // 充值对应金额开启对应的红包
            GuildbagStruct open = waitOpen.stream().filter(o -> o.key == key).collect(Collectors.toList()).get(0);
            if (open != null) {
                open.openLast = 0;
                GuildbagConf gbc = gbcs.stream().filter(o -> o.key == open.key).collect(Collectors.toList()).get(0);
                open.existLast = System.currentTimeMillis() / 1000 + gbc.time[1];
                waitReceive.add(open);
            }
        }
    }

    /**
     * 根据指定红包key,领取该红包
     **/
    public synchronized boolean receive(GameUser user, int key) {
        flush(user, -1);
        GuildbagStruct gbs = waitReceive.stream().filter(r -> r.key == key).collect(Collectors.toList()).get(0);

        if (gbs == null) {
            return false;
        }
        if (gbs.ids.contains(user.getUid())) {
            return false;
        }
        GroupBean gb = user.getDao().findGroup(user.getUid());
        if (gb.getGid() != gbs.gid) {
            return false;
        }
        // 待领取的红包未达上限，且还可以领取
        if (gbs.count >= 1) {
            // 红包超时删除
            if (gbs.existLast < System.currentTimeMillis() / 1000) {
                waitReceive.remove(gbs);
                return false;
            }
            List<GuildbagConf> gbcs = Readonly.getInstance().getGuildbagConfs();
            GuildbagConf gbc = gbcs.stream().filter(s -> s.key == key).collect(Collectors.toList()).get(0);
            // 非第10个红包
            if (gbs.count > 1) {
                // 玩家领取的奖励
                int receive = RandomUtil.getBetween(1, (gbc.reward[1] - gbs.reward - gbs.count));
                user.addItem(gbc.reward[0], receive, true, "领取公会红包");
                gbs.reward += receive;
                gbs.ids.add(user.getUid());
            } else {
                // 第10个红包
                user.addItem(gbc.reward[0], gbc.reward[1] - gbs.reward, true, "领取公会红包");
            }
            --gbs.count;
            // 红包领取完删除
            if (gbs.count == 0) {
                waitReceive.remove(gbs);
            }
        } else {
            waitReceive.remove(gbs);
        }
        return true;
    }
}
