package com.tumei.dto.logs;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by zw on 2018/10/15
 */
public class ActionLogDto {
    @JsonIgnore
    public String log_type;

    public String log_id;

    public AccountInfoDto account_info;
    public Res_RoleInfoDto role_info;
    public Act_actionInfoDto action_info;
}
