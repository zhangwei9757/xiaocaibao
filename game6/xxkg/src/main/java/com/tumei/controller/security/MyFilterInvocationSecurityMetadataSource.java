package com.tumei.controller.security;

import com.google.common.base.Strings;
import com.tumei.GameConfig;
import com.tumei.configs.security.WebSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MyFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    @Autowired
    private GameConfig gameConfig;
    @Autowired
    private WebSecurityConfig webSecurityConfig;

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    private String common = "ROLE_ADMIN";

    private Map<String, String> urlRoleMap = new HashMap<>();

    public void update(String url, String role) {
        urlRoleMap.put(url, "ROLE_" + role);
    }

    public void delete(String url) {
        urlRoleMap.remove(url);
    }

    // 服务器首次启动，初始化权限列表
    private void establishDatabase() {
        String mode = webSecurityConfig.mode;
        String[] ls = {
                "/health", "ANONYMOUS", "ANONYMOUS",
                "/notifyPay", "ANONYMOUS", "ANONYMOUS",
                "/group/**", "ANONYMOUS", "ANONYMOUS",
                "/ws/**", "USER", "USER",
                "/arena/**", "ADMIN", "ADMIN",
                "/*.html", "ADMIN", "ANONYMOUS",
                "/cmd/**", "ADMIN", "ANONYMOUS",
                "/user/**", "ADMIN", "ANONYMOUS",
                "/helps/**", "ADMIN", "ANONYMOUS",
                "*", "ADMIN", "ANONYMOUS"
                };
        for (int i = 0; i < ls.length; ++i) {
            webSecurityConfig.confTemplate.save(new WebSecurityConf(ls[i], mode, ls[++i],ls[++i]));
        }
    }

    @PostConstruct
    public void init() {
        List<WebSecurityConf> urlsByMode = webSecurityConfig.getUrlsByMode(webSecurityConfig.mode);
        if (urlsByMode.size() <= 0) {
            establishDatabase();
            urlsByMode = webSecurityConfig.getUrlsByMode(webSecurityConfig.mode);
        }
        for (WebSecurityConf wsc : urlsByMode) {
            // 0：表示开发 1：表示生产
            if (gameConfig.getReal() == 0) {
                // 开发环境赋值
                if (!Strings.isNullOrEmpty(wsc.devRole)) {

                    if (wsc.url.equalsIgnoreCase("*")) {
                        common = "ROLE_" + wsc.devRole;
                    } else {
                        urlRoleMap.put(wsc.url, "ROLE_" + wsc.devRole);
                    }
                } else {
                    if (wsc.url.equalsIgnoreCase("*")) {
                        common = "ROLE_ANONYMOUS";
                    } else {
                        urlRoleMap.put(wsc.url, "ROLE_ANONYMOUS");
                    }
                }
            } else {
                // 生产环境赋值
                if (!Strings.isNullOrEmpty(wsc.role)) {

                    if (wsc.url.equalsIgnoreCase("*")) {
                        common = "ROLE_" + wsc.role;
                    } else {
                        urlRoleMap.put(wsc.url, "ROLE_" + wsc.role);
                    }
                } else {
                    if (wsc.url.equalsIgnoreCase("*")) {
                        common = "ROLE_ANONYMOUS";
                    } else {
                        urlRoleMap.put(wsc.url, "ROLE_ANONYMOUS");
                    }
                }
            }
        }
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        FilterInvocation fi = (FilterInvocation) object;
        String url = fi.getRequestUrl();

        for (Map.Entry<String, String> entry : urlRoleMap.entrySet()) {
            if (antPathMatcher.match(entry.getKey(), url)) {
                return SecurityConfig.createList(entry.getValue());
            }
        }

//        String httpMethod = fi.getRequest().getMethod();

        //没有匹配到,默认是要登录才能访问
        return SecurityConfig.createList(common);
//        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}