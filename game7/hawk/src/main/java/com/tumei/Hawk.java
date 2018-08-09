package com.tumei;

import com.tumei.common.utils.TimeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
public class Hawk {
	private static final Log log = LogFactory.getLog(Hawk.class);

	@Autowired
	private RunnerBean runnerBean;

	public static void main(String[] args) {
		EmbeddedWebApplicationContext webApplicationContext = (EmbeddedWebApplicationContext)SpringApplication.run(Hawk.class, args);

		EmbeddedServletContainer ec = webApplicationContext.getEmbeddedServletContainer();
		if (ec != null) {
			log.warn("中心服务监听端口:" + ec.getPort() + " day:" + TimeUtil.getNextWeekDay());
		}
	}


}
