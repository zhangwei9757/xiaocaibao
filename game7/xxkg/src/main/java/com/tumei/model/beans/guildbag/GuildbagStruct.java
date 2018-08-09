package com.tumei.model.beans.guildbag;

import java.util.List;

public class GuildbagStruct {
    /**
     * 红包id：当前时间 - 红包类型
     **/
    public String bagId;

    /**
     * 0：未生成红包
     * 1：已生成红包，未开启
     * 2：已开启红包，待领取
     * 3：已领取红包，只可读信息
     **/
    public int status;

    /**
     * 配置表key
     */
    public int key;
    /**
     * 红包类型
     **/
    public int mode;
    /**
     * 红包生成所属公会
     */
    public long gid;
    /**
     * 领取过奖励的玩家资源信息 uid-name-count-source
     */
    public List<String> ids;
    /**
     * 剩余可领取红包个数
     **/
    public int count;
    /**
     * 未开启红包，最后可开启时间
     * 0 :表示非生成状态
     **/
    public long openLast;
    /**
     * 已开启红包，最后领取时间
     * 0：表示非开启状态
     */
    public long existLast;
    /**
     * 开启所需充值金额
     **/
    public long money;
    /**
     * 已被领取奖励总和
     */
    public int reward;

    /**
     * 获取指定玩家已领取红包的资源信息
     */
    public String getUid(long uid,String name,int count,int source) {
        return String.format("%l-%s-%d-%d",uid,name,count,source);
    }
}
