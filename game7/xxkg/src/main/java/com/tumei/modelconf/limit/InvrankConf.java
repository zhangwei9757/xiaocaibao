package com.tumei.modelconf.limit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/28
 *  <p>
 *  怪兽入侵击杀排行配置表
 */
@Document(collection = "Invrank")
public class InvrankConf {
    @Id
    public String objectId;

    public int key;
    /**
     * 上榜达标次数
     */
    public int limit;
    /**
     * 排行奖励
     */
    public int[] reward;
}
