package com.tumei.dto.logs;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Created by zw on 2018/10/12
 * <p>
 * 资源物品日志
 */
public class ResourceLogDto implements Serializable {
    @JsonIgnore
    public String log_type;
    public String log_id;

    public AccountInfoDto account_info;
    public Res_ResInfoDto res_info;
    public Res_RoleInfoDto role_info;
    public Res_ActionInfoDto action_info;
}
