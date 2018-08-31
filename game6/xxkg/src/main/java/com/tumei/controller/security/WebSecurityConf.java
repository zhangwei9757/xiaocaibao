package com.tumei.controller.security;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/08/29
 * <p>
 * 权限配置
 */
@Document(collection = "WebSecurityScript")
public class WebSecurityConf{
    @Id
    public String objectId;
    @Indexed(unique = true)
    public String url;// 对应路径
    public String mode;// 功能模块
    public String role;// 生产角色权限
    public String devRole = "";// 开发角色权限

    public WebSecurityConf(String url, String mode, String role) {
        this.url = url;
        this.mode = mode;
        this.role = role;
    }

    public WebSecurityConf(String mode) {
        this.mode = mode;
    }

    public WebSecurityConf() {
    }
}