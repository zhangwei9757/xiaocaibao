package com.tumei.configs.security;

import com.google.common.base.Strings;
import com.mongodb.WriteResult;
import com.tumei.GameConfig;
import com.tumei.controller.security.MyAccessDecisionManager;
import com.tumei.controller.security.MyFilterInvocationSecurityMetadataSource;
import com.tumei.controller.security.WebSecurityConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/11/5.
 */
@Configuration
@EnableWebSecurity // 开启安全校验
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private GameConfig gameConfig;

    @Autowired
    @Qualifier("confTemplate")
    public MongoTemplate confTemplate;

    @Value("xxkg")
    public String mode;

    @Autowired
    private MyFilterInvocationSecurityMetadataSource myFilterInvocationSecurityMetadataSource;
    @Autowired
    private MyAccessDecisionManager myAccessDecisionManager;

    public int insert(WebSecurityConf wsc) {
        try {
            wsc.mode = wsc.mode.toLowerCase();
            Query query = Query.query(Criteria.where("url").is(wsc.url))
                    .addCriteria(Criteria.where("mode").is(wsc.mode));
            WebSecurityConf one = confTemplate.findOne(query, WebSecurityConf.class);
            // 指定url && mode 存在就直接修改，不存在则插入
            if (one == null) {
                if (!Strings.isNullOrEmpty(wsc.role)) {
                    wsc.role = wsc.role.toUpperCase();
                } else {
                    wsc.role = "";
                }
                confTemplate.save(wsc);
            } else {
                return update(wsc);
            }
        } catch (Exception ex) {
            return -1;
        }
        // 新增成功或者修改成功，直接把内存中权限同步操作
        myFilterInvocationSecurityMetadataSource.update(wsc.url, wsc.role);
        return 1;
    }

    public int delete(WebSecurityConf wsc) {
        Query query = Query.query(Criteria.where("url").is(wsc.url)
                .andOperator(Criteria.where("mode").is(wsc.mode)));

        if (!Strings.isNullOrEmpty(wsc.role)) {
            query.addCriteria(Criteria.where("role").is(wsc.role));

        }
        WriteResult remove = confTemplate.remove(query, WebSecurityConf.class);
        int index = remove.getN();
        if (index == 1) {
            // 删除成功，直接把内存中权限同步操作
            myFilterInvocationSecurityMetadataSource.delete(wsc.url);
        }
        return index;
    }

    public int update(WebSecurityConf wsc) {
        String role = "";
        if (!Strings.isNullOrEmpty(wsc.role)) {
            role = wsc.role.toUpperCase();
        }
        wsc.mode = wsc.mode.toLowerCase();
        Query query = Query.query(Criteria.where("url").is(wsc.url))
                .addCriteria(Criteria.where("mode").is(wsc.mode));

        Update update = Update.update("role", role);

        WriteResult writeResult = confTemplate.updateFirst(query, update, WebSecurityConf.class);
        int index = writeResult.getN();
        if (index == 1) {
            // 修改成功，直接把内存中权限同步操作
            myFilterInvocationSecurityMetadataSource.update(wsc.url, wsc.role);
        }
        return index;
    }

    public List<WebSecurityConf> findByfuzzy(WebSecurityConf wsc) {
        List<WebSecurityConf> list = new ArrayList<>();
        Query query = null;

        if (!Strings.isNullOrEmpty(wsc.url)) {
            query = Query.query(Criteria.where("url").is(wsc.url));
        }
        if (!Strings.isNullOrEmpty(wsc.mode)) {
            query = query.addCriteria(Criteria.where("mode").is(wsc.mode));
        }
        if (!Strings.isNullOrEmpty(wsc.role)) {
            query = query.addCriteria(Criteria.where("role").is(wsc.role));
        }
        if (query != null) {
            // 条件为空直接返回全部列表
            list = confTemplate.find(query, WebSecurityConf.class);
        } else {
            list = confTemplate.findAll(WebSecurityConf.class);
        }
        return list;
    }

    public List<WebSecurityConf> getUrlsByMode(String mode) {
        Query query = Query.query(Criteria.where("mode").is(mode));
        List<WebSecurityConf> wscs = confTemplate.find(query, WebSecurityConf.class);
        return wscs;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        // 只对
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry rg = http.authorizeRequests(); // 定义哪些url需要被保护，哪些不需要
//                .antMatchers("/health", "/ydPay", "/notifyPay", "/group/**")
//                .permitAll()
//                .antMatchers("/ws/**")
//                .authenticated();
//
//        rg.antMatchers("/arena/**").hasAnyAuthority("ADMIN");
//
//        // 开发的时候需要打开以下，正式环境关闭
//        if (gameConfig.getReal() == 0) {
//            rg = rg.antMatchers("/*.html", "/cmd/**", "/user/**", "/helps/**").permitAll();
//        } else {
//            rg = rg.anyRequest().hasAnyAuthority("ADMIN");
//        }
        rg.anyRequest().permitAll();
        // 循环完以后启动FilterSecurityInterceptor配置
        // SecurityMetadataSource用来动态获取url权限配置
        // AccessDecisionManager来进行权限判断
        rg.withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
            public <O extends FilterSecurityInterceptor> O postProcess(
                    O fsi) {
                fsi.setSecurityMetadataSource(myFilterInvocationSecurityMetadataSource);
                fsi.setAccessDecisionManager(myAccessDecisionManager);
                return fsi;
            }
        });
        // 增加jwt验证功能
        rg.and().addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("leon").password("fuckyou").roles("USER"); // 在内存中的定义一个角色的帐号密码
//        auth.userDetailsService(userDetailsService());
    }
}
