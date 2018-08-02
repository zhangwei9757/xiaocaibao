package com.tumei.model.beans.guildbag;

import java.util.List;

public class GuildbagStruct {
    /**
     * 配置表key
     * */
    public int key;
    /**
     * 红包类型
     * **/
    public int mode;
    /**
     * 红包生成所属公会
     * */
    public long gid;
    /**
     * 领取过奖励的玩家id
     * */
    public List<Long> ids;
    /**
     * 剩余可领取红包个数
     **/
    public int count;
    /**
     * 未开启红包，最后可开启时间
     **/
    public long openLast;
    /**
     * 已开启红包，最后领取时间
     */
    public long existLast;
    /**
     * 未开启，开启所需要充值的金额
     **/
    public long money;
    /**
     * 待领取红包，已领取奖励总和
     */
    public int reward;
}
