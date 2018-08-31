package com.tumei;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

@EnableDiscoveryClient
@SpringBootApplication
@EnableMongoHttpSession(maxInactiveIntervalInSeconds = 1800) // 这里定义的时间，决定session的真实超时，使用@Primary定义的数据库,并保存在名字为sessions的表中
@EnableScheduling
@ImportResource(locations = {"classpath:${groovy.bean}.xml"})
public class Hawk {
	@Autowired
	private RunnerBean runnerBean;

	/**
	 * mongo sessionc save converter, neccessary!!!
	 *
	 * @return
	 */
	@Bean
	public JdkMongoSessionConverter jdkMongoSessionConverter() {
		return new JdkMongoSessionConverter();
	}

	public static void main(String[] args) {
		SpringApplication.run(Hawk.class, args);
	}
}
