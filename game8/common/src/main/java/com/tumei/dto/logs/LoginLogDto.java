package com.tumei.dto.logs;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by zw on 2018/10/12
 * <p>
 * 登入登出日志
 */
public class LoginLogDto {
    @JsonIgnore
    public String log_type;
    public String log_id;
    public long create_time;
    public int login_type;
    public String ip;

    public AccountInfoDto account_info;
    public DeviceInfoDto device_info;
    public Login_RoleInfosDto role_info;
}
