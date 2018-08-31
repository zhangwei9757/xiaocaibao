package com.tumei.configs;//package com.tumei.simfight.configs;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//
///**
// * Created by leon on 2016/11/5.
// */
//@Configuration
//@EnableWebSecurity // 开启安全校验
////@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
////        http.csrf().disable();
//
//		//http.antMatcher("fasfdsf");
//		// 只对
//		http.authorizeRequests() // 定义哪些url需要被保护，哪些不需要
//			.anyRequest().permitAll();
//	}
//
//	@Autowired
//	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().withUser("leon").password("leon").roles("USER"); // 在内存中的定义一个角色的帐号密码
////        auth.userDetailsService(userDetailsService());
//	}
//}
