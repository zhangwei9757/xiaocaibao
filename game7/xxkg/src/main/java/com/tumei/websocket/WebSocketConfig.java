package com.tumei.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.logging.Logger;

/**
 * Created by Administrator on 2016/12/29 0029.
 */
@ConditionalOnProperty(value = "xcb.websocket", havingValue = "true")
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Log log  = LogFactory.getLog(WebSocketConfig.class);

    @Autowired
    private WebSocketCustomHandler webSocketCustomHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(webSocketCustomHandler, "/ws").addInterceptors(parameterInterceptor());
    }

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        log.info("----------- 设置 servlet server container...");
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        container.setAsyncSendTimeout(3000);
        container.setMaxSessionIdleTimeout(1800000);
//        container.setMaxTextMessageBufferSize(1024 * 1024);
//        container.setMaxBinaryMessageBufferSize(1024 * 1024);
        return container;
    }

    @Bean
    public HandshakeInterceptor parameterInterceptor() {
        return new HandshakeParameterInterceptor();
    }
}
