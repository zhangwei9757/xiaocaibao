package com.tumei.dto.logs;

import java.io.Serializable;

/**
 * Created by zw on 2018/10/15
 */
public class Reg_RoleInfoDto implements Serializable {
    /**
     * 角色id
     */
    public int role_id;
    /**
     * 性别，如0，表示-女 1, 表示-男
     */
    public String sex;
    /**
     * 昵称
     */
    public String nickname;
    /**
     * 职业，如1，表示剑士
     */
    public String job;
}
