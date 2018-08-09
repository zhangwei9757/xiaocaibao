package com.tumei.configs.security;

import com.tumei.RunnerBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry rg = http.authorizeRequests().antMatchers("/register", "/register_auto", "/logon", "/logon_gm",  "/logonQ", "/logon_admin", "/html/**", "/service/**", "/abp/**", "/*.html", "/health").permitAll();


		if (runnerBean.getReal() == 0) {
			rg.anyRequest().permitAll();
		}
		else {
			// 部分需要更强的OWNER权限才能访问
			rg.antMatchers(
			"/cmd/modifyPassword", "/cmd/modifyAdmin", "/cmd/getAdminLogs", "/cmd/bindAccount", "/cmd/saveServer", "/cmd/delServer",
					"/cmd/recentInfos", "/cmd/getIncome", "/cmd/getTopAccount"
			).hasAnyAuthority("OWNER");
			rg.anyRequest().hasAnyAuthority("ADMIN");
		}

		rg.and().addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().withUser("leon").password("leon").roles("USER"); // 在内存中的定义一个角色的帐号密码
		auth.userDetailsService(userDetailsService());
	}
}
