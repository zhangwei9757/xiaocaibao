package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.controller.GroupService;
import com.tumei.dto.guild.GuildbagDetailDto;
import com.tumei.modelconf.GuildbagConf;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by zw on 2018-07-31
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
    public synchronized Integer flushBag(int mode, long gid) {
        long currentTime = System.currentTimeMillis() / 1000;
        // 超时红包，直接删除
        for (int i = 0; i < guildBags.size(); ) {
            GuildbagStruct gbd = guildBags.get(i);
            if (gbd.openLast < currentTime && gbd.status == 1) {
                guildBags.remove(i);
            } else if (gbd.existLast < currentTime && gbd.status >= 2) {
                guildBags.remove(i);
            } else {
                ++i;
            }
        }
        if (mode <= 0) {
            return -1;
        }
        // 所有可生成的类型
        List<Integer> modes = Readonly.getInstance().getGuildbagConfs().stream().map(s -> s.mode).distinct().collect(Collectors.toList());
        if (!modes.contains(mode)) {
            return -1;
        }
        // 本公会所有未开启的红包个数
        long max = guildBags.stream().filter(f -> f.status == 1).count();
        if (max == modes.size()) {
            return -1;
        }
        // 已生成过的红包个数
//        long count = guildBags.stream().filter(f -> (f.mode == mode && f.status == 1)).count();
//        if (count == 1) {
//            return -1;
//        }
        // 配置表中的对应概率
        int[] probability = Readonly.getInstance().getGuildbagConfs().get(0).condition;
        int random = RandomUtil.getBetween(1, 100);
        if (random <= probability[Math.abs(mode - 3)]) {
            // 已生成过的未开启红包类型
            List<Integer> existMode = guildBags.stream().filter(m -> m.status == 1).map(s -> s.mode).distinct().collect(Collectors.toList());
            // 所有可生成的未开启红包类型
            List<GuildbagConf> gbcs = Readonly.getInstance().getGuildbagConfs();
            // 去除已生成类型后，可生成的未开启 红包类型
            List<GuildbagConf> newList = gbcs.stream().filter(g -> (!existMode.contains(g.mode)))
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
            gbs.gid = gid;
            gbs.sources = gbc.reward;
            guildBags.add(gbs);
            return gbs.key;
        }
        return -1;
    }

    /**
     * 完成对应金额充值,开启红包
     * 0：未生成红包
     * 1：已生成红包，未开启
     * 2：已开启红包，待领取
     * 3：已领取红包，只可读
     **/
    public synchronized void open(int mode, String userName, long gid) {
        flushBag(-1, gid);

        List<GuildbagConf> gbcs = Readonly.getInstance().getGuildbagConfs();
        List<Integer> modes = gbcs.stream().map(s -> 4 - s.mode).distinct().collect(Collectors.toList());
        if (!modes.contains(mode)) {
            return;
        }
        // 充值类型对应可开启红包
        List<GuildbagStruct> gbsList = guildBags.stream().filter(f -> (f.status == 1 && f.mode == mode)).collect(Collectors.toList());
        // 存在未开启红包
        if (gbsList.size() > 0) {
            // 开启对应的红包
            GuildbagStruct open = gbsList.get(0);
            if (open != null) {
                open.openLast = 0;
                GuildbagConf gbc = gbcs.stream().filter(o -> o.key == open.key).collect(Collectors.toList()).get(0);
                open.existLast = System.currentTimeMillis() / 1000 + gbc.time[1];
                open.status = 2;
                open.openName = userName;
            }
        }
    }

    /**
     * 领取红包
     * 0：未生成红包
     * 1：已生成红包，未开启
     * 2：已开启红包，待领取
     * 3：已领取红包，只可读
     **/
    public synchronized GuildbagDetailDto receive(long uid, String name, String bagid, long gid) {
        flushBag(-1, gid);
        // 对应的可领取红包
        Optional<GuildbagStruct> opt = guildBags.stream().filter(f -> f.bagId.equalsIgnoreCase(bagid)).findFirst();
        if (!opt.isPresent()) {
            return null;
        }
        GuildbagDetailDto detail = null;
        GuildbagStruct gs = opt.get();
        // 领取上限，不可领取
        if (gs.status != 2 || gs.count == 0) {
            Optional<GuildbagStruct> first = guildBags.stream().filter(s -> s.bagId == bagid).findFirst();
            if (first.isPresent()) {
                GuildbagStruct guildbagStruct = first.get();
                detail = guildbagStruct.createDetail();
                detail.remaining = guildbagStruct.count;
                return detail;
            }
        }

        // 领取过红包不可领取
        if (gs.ids.containsKey(uid)) {
            Optional<GuildbagStruct> first = guildBags.stream().filter(s -> s.bagId.equalsIgnoreCase(bagid)).findFirst();
            if (first.isPresent()) {
                GuildbagStruct guildbagStruct = first.get();
                detail = guildbagStruct.createDetail();
                detail.remaining = guildbagStruct.count;
                return detail;
            }
        }
        List<GuildbagConf> gbcs = Readonly.getInstance().getGuildbagConfs();
        GuildbagConf gbc = gbcs.stream().filter(s -> s.key == gs.key).collect(Collectors.toList()).get(0);
        double receive = 0;
        if (gs.count > 1) {
            // 非第10个红包
            // receive = RandomUtil.getBetween(1, (gbc.reward[1] - gs.reward - gs.count));
            receive = Math.floor(gs.resouce[10 - gs.count] * gbc.reward[1]);
            if (receive < 1) {
                receive = 1;
            }
            gs.reward += receive;
        } else {
            // 第10个红包
            receive = gbc.reward[1] - gs.reward;
            if (receive <= 0) {
                receive = 1;
            }
        }
        --gs.count;
        gs.addIDS(uid, name, (int) receive, gbc.reward[0]);
        // 红包领取完删除
        if (gs.count == 0) {
            gs.status = 3;
        }
        Optional<GuildbagStruct> optional = guildBags.stream().filter(s -> s.bagId.equalsIgnoreCase(bagid)).findFirst();
        if (optional.isPresent() && receive > 0) {
            GuildbagStruct guildbagStruct = optional.get();
            detail = guildbagStruct.createDetail();
            detail.count = (int) receive;
            detail.id = gbc.reward[0];
            detail.remaining = guildbagStruct.count;
        }
        return detail;
    }
}
