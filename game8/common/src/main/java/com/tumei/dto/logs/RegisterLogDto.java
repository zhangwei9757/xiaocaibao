package com.tumei.dto.logs;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Created by zw on 2018/10/15
 * <p>
 * 注册日志
 */
public class RegisterLogDto implements Serializable {
    @JsonIgnore
    public String log_type;
    public String log_id;
    public long create_time;
    public String ip;

    public Reg_AccountInfoDto account_info;
    public DeviceInfoDto device_Info;
    public Reg_RoleInfoDto role_info;
}
