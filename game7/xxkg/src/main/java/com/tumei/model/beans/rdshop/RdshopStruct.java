package com.tumei.model.beans.rdshop;

/**
 * Created by Administration on 2018/7/27
 */
public class RdshopStruct {
    /**
     * 配置表的key
     * */
    public long key;
//    public int type;
//    public int limit;
//    public int level;
//    public int[] rewards;
//    public int cost;

    /**
     * 战力值
     * 如果为：0 表示是购买事件
     */
    public long power;
    /***
     * 任务生成时间
     * */
    public long begin;

    /**
     * 激活后的持续结束时间
     * */
    public long complete;
}
