package com.tumei.configs.security;

import com.tumei.RunnerBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Created by leon on 2016/11/5.
 */
@Configuration
@EnableWebSecurity // 开启安全校验
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private RunnerBean runnerBean;

	@Override
	public @Bean
	UserDetailsService userDetailsService() {
		return new MongoUserDetailService();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		if (runnerBean.getReal() == 0) {
			http.authorizeRequests().anyRequest().permitAll();
		} else {
			http.authorizeRequests()
				.antMatchers("/register", "/logon", "/logonYD", "/html/**", "/health").permitAll()
				.antMatchers("/service/**", "/*.html", "/abp/**", "/ad/**").permitAll()
				.anyRequest().hasAnyAuthority("ADMIN");
		}
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().withUser("leon").password("leon").roles("USER"); // 在内存中的定义一个角色的帐号密码
		auth.userDetailsService(userDetailsService());
	}
}
