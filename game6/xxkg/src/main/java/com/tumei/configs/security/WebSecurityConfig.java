package com.tumei.configs.security;

import com.tumei.GameConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    private CustomFilterInvocationSecurityMetadataSource customFilterInvocationSecurityMetadataSource;
    @Autowired
    private CustomAccessDecisionManager customAccessDecisionManager;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        // 只对
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry rg = http.authorizeRequests(); // 定义哪些url需要被保护，哪些不需要

        rg.anyRequest().permitAll();

        rg.withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
            public <O extends FilterSecurityInterceptor> O postProcess(
                    O fsi) {
                fsi.setSecurityMetadataSource(customFilterInvocationSecurityMetadataSource);
                fsi.setAccessDecisionManager(customAccessDecisionManager);
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
