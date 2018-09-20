package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/10
 */
@Document(collection = "Dailyactlist")
public class DailyactlistConf {
    @Id
    public String objectId;

    public int key;
    /**
     * 活动开始时间
     */
    public long start;
    /**
     * 活动结束时间
     */
    public long last;
    /**
     * 奖励
     */
    public int[][] rewardx;

    public int flag;

    public int num;
    /**
     * 消费限额
     */
    public int[] costx;
}
