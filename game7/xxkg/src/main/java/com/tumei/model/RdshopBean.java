package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameUser;
import com.tumei.model.beans.rdshop.RdshopStruct;
import com.tumei.modelconf.RdshopConf;
import com.tumei.modelconf.happy.SoulConf;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.sql.SQLOutput;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/7/27
 * <p>
 * 神秘商店：
 */
@Data
@Document(collection = "Role.Rdshop")
public class RdshopBean {
    public RdshopBean() {
    }

    public RdshopBean(long _id) {
        id = _id;
    }

    @JsonIgnore
    @Id
    private String objectId;
    @Field("id")
    private Long id;

    /**
     * 最后生成事件时间
     */
    private long last;

    /**
     * 可以生成的事件数
     */
    private int count;

    private RdshopStruct rs;

    /**
     * @desc: 根据等级刷新任务
     * @returns: 返回旧的事件或者新生成的一个事件
     */
    public RdshopStruct flush(GameUser user) {
        long current = System.currentTimeMillis() / 1000;
        if (last > current) {
            last = current;
        }
        if (count < 0) {
            count = 0;
        }
        if (last == 0) {
            last = current;
        }
        long diff = System.currentTimeMillis() / 1000 - last;
        // 应生成事件数量
        long need = diff / 3600;
        count += need;
        if (count > 12) {
            count = 12;
        }

        if (rs == null && count > 0) {
            // 随机一个事件类型
            int type = user.getLevel() >= 20 ? RandomUtil.getBetween(1, 2) : 1;
            // 配置表匹配的信息
            List<RdshopConf> newlist = Readonly.getInstance().getRdshop().stream().filter(f -> (f.type == type && user.getLevel() >= f.level)).collect(Collectors.toList());
            // 生成的一个事件
            RdshopConf rc = newlist.get(RandomUtil.getBetween(0, newlist.size() - 1));
            rs = new RdshopStruct();
            // rs.rewards = rc.rewards;
            rs.key = rc.key;
            // rs.cost = rc.cost;

            if (rc.type == 1) {
                HerosBean hsb = DaoGame.getInstance().findHeros(user.getUid());
                rs.power = (long) (1.0 + rc.limit / 10000.0) * user.calcPower(hsb);
            }

            --count;
            last = System.currentTimeMillis() / 1000;
            rs.begin = System.currentTimeMillis() / 1000;
        }
        return rs;
    }

    public RdshopStruct complete(GameUser user) {
        rs = null;
        return flush(user);
    }

    public RdshopStruct cancel(GameUser user) {
        rs = null;
        return flush(user);
    }
}


