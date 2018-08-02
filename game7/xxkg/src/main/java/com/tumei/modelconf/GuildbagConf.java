package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2018-07-31
 */
@Document(collection = "Guildbag")
public class GuildbagConf {
    @Id
    public String ObjectId;

    public int key;
    /**
     * 红包类型
     */
    public int mode;
    public int[] condition;
    /**
     * 下标 0：红包未激活存在时间 1：红包打开后存在时间
     */
    public long[] time;
    /**
     * 开启红包所需金额
     */
    public int open;

    public int[] reward;


}
