package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.Readonly;
import com.tumei.dto.guildbag.GuildbagDto;
import com.tumei.common.utils.RandomUtil;
import com.tumei.controller.GroupService;
import com.tumei.modelconf.GuildbagConf;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;
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
    public List<GuildbagDto> guildBags = new ArrayList<>();

    /**
     * 刷新本公会所有红包状态
     * 0：未生成红包
     * 1：已生成红包，未开启
     * 2：已开启红包，待领取
     * 3：已领取红包，只可读
     **/
    public synchronized void flushBag(int mode) {
        long currentTime = System.currentTimeMillis() / 1000;
        // 获取指定公会的所有红包信息
        guildBags = GroupService.getInstance().findGuildbag(id).guildBags;
        // 超时红包，修改状态
        guildBags.stream().forEach(s -> {
            if (s.openLast < currentTime) {
                s.status = 0;
            }
            if (s.existLast < currentTime) {
                s.status = 3;
            }
        });
        // 所有可生成的类型
        List<Integer> modes = Readonly.getInstance().getGuildbagConfs().stream().map(s -> s.mode).distinct().collect(Collectors.toList());
        if (!modes.contains(mode)) {
            return;
        }
        // 本公会所有未开启的红包个数
        long max = guildBags.stream().filter(f -> f.status == 1).count();
        if (max == modes.size()) {
            return;
        }
        // 捐献类型，已生成过的红包个数
        long count = guildBags.stream().filter(f -> (f.mode == mode && f.status == 1)).count();
        if (count == 1) {
            return;
        }
        // 配置表中的对应概率
        int[] probability = Readonly.getInstance().getGuildbagConfs().get(0).condition;
        int random = RandomUtil.getBetween(1, 100);
        if (random <= probability[mode - 1]) {
            // 已生成过的未开启红包类型
            List<Integer> existMode = guildBags.stream().filter(m -> m.status == 1).map(s -> s.mode).collect(Collectors.toList());
            // 所有可生成的未开启红包类型
            List<GuildbagConf> gbcs = Readonly.getInstance().getGuildbagConfs();
            // 去除已生成类型后，可生成的未开启 红包类型
            List<GuildbagConf> newList = gbcs.stream().filter(g -> (!existMode.contains(g.mode) && g.mode == 4 - mode))
                    .collect(Collectors.toList());
            random = RandomUtil.getBetween(0, newList.size() - 1);
            GuildbagConf gbc = newList.get(random);
            GuildbagDto gbs = new GuildbagDto();
            gbs.key = gbc.key;
            gbs.mode = gbc.mode;
            gbs.count = 10;
            gbs.openLast = System.currentTimeMillis() / 1000 + gbc.time[0];
            gbs.money = gbc.open;
            gbs.bagId = String.format("%s-%d", System.currentTimeMillis() / 1000, mode);
            gbs.status = 1;
            gbs.gid = id;
        }
    }

    /**
     * 完成对应金额充值,开启红包
     * 0：未生成红包
     * 1：已生成红包，未开启
     * 2：已开启红包，待领取
     * 3：已领取红包，只可读
     **/
    public synchronized void open(int mode) {
        flushBag(-1);
        guildBags = GroupService.getInstance().findGuildbag(id).guildBags;

        List<GuildbagConf> gbcs = Readonly.getInstance().getGuildbagConfs();
        List<Integer> modes = gbcs.stream().map(s -> 4 - s.mode).distinct().collect(Collectors.toList());
        if (!modes.contains(mode)) {
            return;
        }
        // 充值类型对应可开启红包
        List<GuildbagDto> gbsList = guildBags.stream().filter(f -> (f.status == 1 && f.mode == 4 - mode)).collect(Collectors.toList());
        // 存在未开启红包
        if (gbsList.size() > 0) {
            // 开启对应的红包
            GuildbagDto open = gbsList.get(0);
            if (open != null) {
                open.openLast = 0;
                GuildbagConf gbc = gbcs.stream().filter(o -> o.key == open.key).collect(Collectors.toList()).get(0);
                open.existLast = System.currentTimeMillis() / 1000 + gbc.time[1];
                open.status = 2;
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
    public synchronized int[] receive(long uid, String name, String bagid) {
        flushBag(-1);
        // 对应的可领取红包个数
        List<GuildbagDto> gbsList = guildBags.stream().filter(f -> f.status == 2 && f.bagId.equalsIgnoreCase(bagid)).collect(Collectors.toList());
        if (gbsList.size() < 1) {
            return null;
        }

        GuildbagDto gs = gbsList.get(0);
        // 红包超时,只可读取信息
        if (gs.existLast < System.currentTimeMillis() / 1000) {
            gs.status = 3;
            return null;
        }
        List<GuildbagConf> gbcs = Readonly.getInstance().getGuildbagConfs();
        GuildbagConf gbc = gbcs.stream().filter(s -> s.key == gs.key).collect(Collectors.toList()).get(0);
        int receive = 0;
        if (gs.count > 1) {
            // 非第10个红包
            receive = RandomUtil.getBetween(1, (gbc.reward[1] - gs.reward - gs.count));
        } else {
            // 第10个红包
            receive = gbc.reward[1] - gs.reward;
        }
        gs.reward += receive;
        --gs.count;
//      user.addItem(gbc.reward[0], receive, true, "领取公会红包");
        gs.ids.add(gs.getUid(uid, name, receive, gbc.reward[0]));

        // 红包领取完删除
        if (gs.count == 0) {
            gs.status = 3;
        }
        return new int[]{gbc.reward[0], receive};
    }
}
