package com.tumei.configs.security;

import com.tumei.GameConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
//        http.cors().disable();

        // 只对
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry rg = http.authorizeRequests() // 定义哪些url需要被保护，哪些不需要
			    .antMatchers("/health", "/notifyPay", "/group/**").permitAll().antMatchers("/ws/**").authenticated();

//        rg.antMatchers("/arena/**").hasAnyAuthority("ADMIN");

        // 开发的时候需要打开以下，正式环境关闭
		if (gameConfig.getReal() == 0) {
            rg = rg.antMatchers("/*.html", "/cmd/**", "/user/**", "/helps/**").permitAll();
        } else {

            rg = rg.antMatchers(
                    "/cmd/getInfo"
            ).hasAnyAuthority("OWNER");

            rg = rg.anyRequest().hasAnyAuthority("ADMIN");
        }

        // 增加jwt验证功能
        rg.and().addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("leon").password("Xcb@018213-434&^#$").roles("USER"); // 在内存中的定义一个角色的帐号密码
//        auth.userDetailsService(userDetailsService());
    }
}
