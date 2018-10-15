package com.tumei;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class Arena {
	private static final Log log = LogFactory.getLog(Arena.class);

	@Autowired
	private RunnerBean runnerBean;

	public static void main(String[] args) {
		EmbeddedWebApplicationContext webApplicationContext = (EmbeddedWebApplicationContext)SpringApplication.run(Arena.class, args);
		EmbeddedServletContainer ec = webApplicationContext.getEmbeddedServletContainer();
		if (ec != null) {
			log.warn("跨服竞技场服务监听端口:" + ec.getPort());
		}
	}
}