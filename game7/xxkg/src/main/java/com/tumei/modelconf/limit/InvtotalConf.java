package com.tumei.modelconf.limit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/28
 * <p>
 * 怪兽入侵活动期间累计充值奖励配置表
 */
@Document(collection = "Invtotal")
public class InvtotalConf {
    @Id
    public String objectId;

    public int key;

    public int cost;

    public int[] reward;
}
