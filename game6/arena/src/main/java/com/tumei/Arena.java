package com.tumei;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ImportResource(locations = {"classpath:${groovy.bean}.xml"})
public class Arena {
	@Autowired
	private RunnerBean runnerBean;

	public static void main(String[] args) {
		SpringApplication.run(Arena.class, args);
	}
}
