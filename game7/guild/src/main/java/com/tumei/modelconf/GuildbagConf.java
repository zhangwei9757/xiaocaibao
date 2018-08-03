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
    /**
     * 配置表 key
     */
    public int key;
    /**
     * 红包类型
     * 1：高级
     * 2：中级
     * 3：初级
     */
    public int mode;
    /**
     * 红包对应类型,生成机率
     */
    public int[] condition;
    /**
     * 下标 0：红包未激活存在时间
     * 下标 1：红包打开后存在时间
     */
    public long[] time;
    /**
     * 开启红包所需金额
     */
    public long open;
    /**
     * 对应总奖励数量
     */
    public int[] reward;


}