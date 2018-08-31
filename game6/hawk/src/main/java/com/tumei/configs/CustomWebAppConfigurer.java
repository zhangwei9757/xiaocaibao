package com.tumei.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Leon on 2017/8/25 0025.
 */
@Configuration
public class CustomWebAppConfigurer extends WebMvcConfigurerAdapter {

	/**
	 * 增加html 静态目录
	 *
	 * @param registry
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/html/**").addResourceLocations("file:./html/", "classpath:/resources/html/");
		super.addResourceHandlers(registry);
	}

}
