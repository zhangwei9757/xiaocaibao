package com.tumei.dto.guild;

/**
 * Created by zw on 2018/08/22
 */
public class GuildbagBasicDto {
    public GuildbagBasicDto() {
    }

    public GuildbagBasicDto(String bagId, int status, int key, int mode, long gid, String openName, int count, long openLast, long existLast, long money, int reward,int[] sources) {
        this.bagId = bagId;
        this.status = status;
        this.key = key;
        this.mode = mode;
        this.gid = gid;
        this.openName = openName;
        this.count = count;
        this.openLast = openLast;
        this.existLast = existLast;
        this.money = money;
        this.reward = reward;
        this.sources = sources;
    }

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
     * 激活红包玩家的呢称
     **/
    public String openName;

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

    public boolean flag;
    /**
     * 红包资源信息左id ，右数量
     */
    public int[] sources = new int[2];
}
