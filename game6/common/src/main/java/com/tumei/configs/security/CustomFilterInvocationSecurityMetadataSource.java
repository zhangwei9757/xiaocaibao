//package com.tumei.configs.security;
//
//import com.google.common.base.Strings;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Scope;
//import org.springframework.security.access.ConfigAttribute;
//import org.springframework.security.access.SecurityConfig;
//import org.springframework.security.web.FilterInvocation;
//import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//
//import javax.annotation.PostConstruct;
//import javax.crypto.SecretKey;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//@Scope(value = "singleton")
//public class CustomFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
//    @Autowired
//    private WebSecurityMongo webSecurityMongo;
//
//    @Value("${gameserver.real:1}")
//    private int real;
//
//    private AntPathMatcher antPathMatcher = new AntPathMatcher();
//
//    private String common = "ADMIN";
//
//    private Map<String, String> urlRoleMap = new HashMap<>();
//
//    public void update(String url, String role) {
//        urlRoleMap.put(url, role);
//    }
//
//    public void delete(String url) {
//        urlRoleMap.remove(url);
//    }
//
//    // 服务器首次启动，初始化权限列表
//    private void establishDatabase() {
//        String mode = webSecurityMongo.mode;
//        switch (mode) {
//            case "center": {
//                String[] ls = {
//                        "/register"     , "", "",
//                        "/logon"        , "", "",
//                        "/logon_admin"  , "", "",
//                        "/logon_gm"     , "", "",
//                        "/logonYD"      , "", "",
//                        "/html/**"      , "", "",
//                        "/health"       , "", "",
//                        "/*.html"       , "", "",
//                        "/service/**"   , "", "",
//                        "/abp/**"       , "", "",
//                        "/ad/**"        , "", "",
//                        "*"             , "ADMIN"   , ""
//                };
//                for (int i = 0; i < ls.length; ++i) {
//                    webSecurityMongo.insert(new WebSecurityConf(ls[i], mode, ls[++i], ls[++i]));
//                }
//                break;
//            }
//            case "xxkg": {
//                String[] ls = {
//                        "/health"       , ""   , "",
//                        "/notifyPay"    , ""   , "",
//                        "/group/**"     , ""   , "",
//                        "/ws/**"        , "USER"        , "USER",
//                        "/arena/**"     , "ADMIN"       , "ADMIN",
//                        "/*.html"       , "ADMIN"       , "",
//                        "/cmd/**"       , "ADMIN"       , "",
//                        "/user/**"      , "ADMIN"       , "",
//                        "/helps/**"     , "ADMIN"       , "",
//                        "*"             , "ADMIN"       , ""
//                };
//                for (int i = 0; i < ls.length; ++i) {
//                    webSecurityMongo.insert(new WebSecurityConf(ls[i], mode, ls[++i], ls[++i]));
//                }
//                break;
//            }
//        }
//
//    }
//
//    @PostConstruct
//    public void init() {
//        List<WebSecurityConf> urlsByMode = webSecurityMongo.getUrlsByMode(webSecurityMongo.mode);
//        if (urlsByMode.size() <= 0) {
//            establishDatabase();
//            urlsByMode = webSecurityMongo.getUrlsByMode(webSecurityMongo.mode);
//        }
//        for (WebSecurityConf wsc : urlsByMode) {
//
//            // 生产环境赋值
//            String role = wsc.role;
//            if (real == 0) {
//                role = wsc.devRole;
//            }
//            if (!Strings.isNullOrEmpty(role)) {
//                if (wsc.url.equalsIgnoreCase("*")) {
//                    common = role;
//                } else {
//                    urlRoleMap.put(wsc.url, role);
//                }
//            } else {
//                if (wsc.url.equalsIgnoreCase("*")) {
//                    common = "";
//                } else {
//                    urlRoleMap.put(wsc.url, "");
//                }
//            }
//        }
//
//    }
//
//    @Override
//    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
//        FilterInvocation fi = (FilterInvocation) object;
//        String url = fi.getRequestUrl();
//
//        for (Map.Entry<String, String> entry : urlRoleMap.entrySet()) {
//            if (antPathMatcher.match(entry.getKey(), url)) {
//                if (entry.getValue().isEmpty()) {
//                    return SecurityConfig.createList();
//                }
//                return SecurityConfig.createList(entry.getValue());
//            }
//        }
//
//        //没有匹配到,默认是要登录才能访问
//        if (common.isEmpty()) {
//            return SecurityConfig.createList();
//        }
//        return SecurityConfig.createList(common);
//    }
//
//    @Override
//    public Collection<ConfigAttribute> getAllConfigAttributes() {
//        return null;
//    }
//
//    @Override
//    public boolean supports(Class<?> clazz) {
//        return FilterInvocation.class.isAssignableFrom(clazz);
//    }
//}