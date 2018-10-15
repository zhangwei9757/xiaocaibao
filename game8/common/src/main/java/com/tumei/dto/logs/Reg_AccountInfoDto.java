package com.tumei.dto.logs;

import java.io.Serializable;

/**
 * Created by zw on 2018/10/15
 */
public class Reg_AccountInfoDto implements Serializable {
    /**
     * 用户信息，如果没有则为0
     */
    public int uid;
    /**
     * 安装包的身份证，包括了项目，渠道，支付等信息
     */
    public String gamekey;
    /**
     * 帐号id
     */
    public String aid;
    /**
     * 服id
     */
    public int sid;
}

