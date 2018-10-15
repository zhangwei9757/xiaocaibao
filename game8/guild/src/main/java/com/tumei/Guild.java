package com.tumei;

import com.tumei.common.utils.TimeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableScheduling
@SpringBootApplication
public class Guild {
	static final Log log = LogFactory.getLog(Guild.class);

	public static void main(String[] args) {
		EmbeddedWebApplicationContext webApplicationContext = (EmbeddedWebApplicationContext)SpringApplication.run(Guild.class, args);

		EmbeddedServletContainer ec = webApplicationContext.getEmbeddedServletContainer();
		if (ec != null) {
			log.warn("Guild 服务监听端口:" + ec.getPort() + " day:" + TimeUtil.nowString());
		}
	}
}
