//package com.tumei.configs;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//
///**
// * Created by Leon on 2017/9/13 0013.
// */
//@Configuration
//public class GameThreadConfiguration {
//
//	@Bean(name = "GameThread")
//	public Executor getThreadPoolExecutor() {
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(1);
//		executor.setMaxPoolSize(1);
//		executor.setQueueCapacity(100);
//		executor.setThreadNamePrefix("gt-");
//		executor.initialize();
//		return executor;
//	}
//}
