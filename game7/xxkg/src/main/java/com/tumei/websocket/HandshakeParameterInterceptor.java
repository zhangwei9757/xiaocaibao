package com.tumei.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/12/29 0029.
 * 不使用stomp模式处理websocket
 * websocket 握手前后
 */
public class HandshakeParameterInterceptor implements HandshakeInterceptor {
	private static final Log log = LogFactory.getLog(HandshakeParameterInterceptor.class);

	private static final String HEADER_STRING = "uid";

	@Override
	public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, org.springframework.web.socket.WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
		if (serverHttpRequest instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest request = (ServletServerHttpRequest) serverHttpRequest;
			Map<String, String[]> paramters = request.getServletRequest().getParameterMap();
			Map<String, String> httpParams = paramters.entrySet().stream().filter(entry -> entry.getValue().length > 0).collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));

			Object uidHeader = request.getServletRequest().getAttribute(HEADER_STRING);
			if (uidHeader == null) { // cookie + session 模式进行认证
				log.error("客户端提交的数据中没有玩家uid,无法查找玩家信息.");
				return false;
			}
			else { // Jwt模式认证
//				log.info("jwt mode ws hand shake, uid:" + uidHeader);
				map.put(HEADER_STRING, uidHeader);
			}

			map.putAll(httpParams);
			return true;
		}

		return false;
	}

	@Override
	public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, org.springframework.web.socket.WebSocketHandler webSocketHandler, Exception e) {
	}
}
