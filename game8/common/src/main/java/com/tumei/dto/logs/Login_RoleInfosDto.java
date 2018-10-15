package com.tumei.dto.logs;

import java.io.Serializable;

/**
 * Created by zw on 2018/10/12
 */
public class Login_RoleInfosDto implements Serializable {
    /**
     * 关卡信息，举例：“5-3”或主线节点如“1-2”
     */
    public String checkpoint;
    /**
     * 金币数量
     */
    public long gold;
    /**
     * 玩家钻石
     */
    public int diamond;
    /**
     * 登出时候记录本次在线时长，单位秒，举例：“1000”
     */
    public int online_time;
    /**
     * 角色id
     */
    public int role_id;
    /**
     * vip经验，举例：“8000”
     */
    public int vip_exp;
    /**
     * 昵称
     */
    public String nickname;
    /**
     * 等级
     */
    public int lv;
    /**
     * vip等级，举例：“5”
     */
    public int vip_lv;
    /**
     * int64，玩家战力
     */
    public long power;
    /**
     * int64，角色创建时间，时间戳，13位，精确到毫秒,角色注册时间
     */
    public long role_create_time;
}
