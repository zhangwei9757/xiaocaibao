package com.tumei.common.service;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Administrator on 2016/12/29 0029.
 */
@Configuration
public class RestTemplateConfig {

    @Bean("balance")
	@LoadBalanced
    RestTemplate restTemplate() {
        RestTemplate template = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        template.setRequestFactory(factory);

//        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
//        interceptors.add(new LoggingRequestInterceptor());
//		template.setInterceptors(interceptors);
        return template;
    }

    @Bean(value = "simple")
    RestTemplate simpleTemplate() {
        RestTemplate template = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        template.setRequestFactory(factory);

        return template;
    }

}
