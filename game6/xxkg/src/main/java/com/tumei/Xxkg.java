package com.tumei;

import com.tumei.common.service.RouterService;
import com.tumei.common.utils.TimeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;


//@EnableCircuitBreaker
//@EnableRedisHttpSession
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableMongoHttpSession(maxInactiveIntervalInSeconds = 1800) // 这里定义的时间，决定session的真实超时，使用@Primary定义的数据库,并保存在名字为sessions的表中
@ImportResource(locations = {"classpath:${groovy.bean}.xml"})
public class Xxkg {
	@Autowired
	private RunnerBean runnerBean;

	@Bean
	public JdkMongoSessionConverter jdkMongoSessionConverter() {
		return new JdkMongoSessionConverter();
	}

	@Bean
	RouterService routerService() {
		return new RouterService();
	}

	public static Log log = LogFactory.getLog(Xxkg.class);

	public static void main(String[] args) {
		log.info("+++ start to run xxkg.");
		System.setProperty("org.apache.tomcat.websocket.DEFAULT_BUFFER_SIZE", "102400");

		EmbeddedWebApplicationContext webApplicationContext = (EmbeddedWebApplicationContext)SpringApplication.run(Xxkg.class, args);

		EmbeddedServletContainer ec = webApplicationContext.getEmbeddedServletContainer();
		if (ec != null) {
			log.warn("Xxkg 服务监听端口:" + ec.getPort() + " day:" + TimeUtil.getNextWeekDay());
		}

	}
}
