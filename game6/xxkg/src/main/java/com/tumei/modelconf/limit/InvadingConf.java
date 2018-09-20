package com.tumei.modelconf.limit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/11
 */
@Document(collection = "Invading")
public class InvadingConf {

    @Id
    public String objectId;

    public int key;
    /**
     * 活动开始时间
     */
    public int start;
    /**
     * 活动结束时间
     */
    public int end;
    /**
     * 累计登陆奖励
     */
    public int[][] logindd;
    /**
     * 累计登陆再领取所需钻石（仅可再购买一次）
     */
    public int[] ddagain;
    /**
     * 补签所需钻石
     */
    public int[] addcost;
    /**
     * 使用活动材料奖励（每次均随机获取）
     */
    public int[] usecrystal;
    /**
     * 击杀一次领取奖励（首次奖励随机，每次奖励数量均随机）
     */
    public int[][] reward;
}
