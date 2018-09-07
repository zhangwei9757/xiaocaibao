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

//	@Autowired
//	private CustomFilterInvocationSecurityMetadataSource customFilterInvocationSecurityMetadataSource;
//
//	@Autowired
//	private CustomAccessDecisionManager customAccessDecisionManager;

	@Override
	public @Bean
	UserDetailsService userDetailsService() {
		return new MongoUserDetailService();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		// 有问题 暂时关闭
//		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry rg = http.authorizeRequests(); // 定义哪些url需要被保护，哪些不需要
//		rg.anyRequest().permitAll();
//		rg.withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
//			public <O extends FilterSecurityInterceptor> O postProcess(
//					O fsi) {
//				fsi.setSecurityMetadataSource(customFilterInvocationSecurityMetadataSource);
//				fsi.setAccessDecisionManager(customAccessDecisionManager);
//				return fsi;
//			}
//		});

		if (runnerBean.getReal() == 0) {
			http.authorizeRequests().anyRequest().permitAll();
		} else {
			http.authorizeRequests()
				.antMatchers("/register", "/logon_gm", "/logon_admin", "/logon", "/logonQ", "/html/**", "/health").permitAll()
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
