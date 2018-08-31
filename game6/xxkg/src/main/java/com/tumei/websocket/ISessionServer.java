package com.tumei.websocket;

import com.tumei.groovy.contract.IProtocolDispatcher;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by leon on 2016/12/31.
 */
public interface ISessionServer {
	void onAddSession(WebSocketSession session) throws Exception;

	void onDelSession(WebSocketSession session);

	void onMessage(WebSocketSession session, String cmd, String data);

	Class<? extends BaseProtocol> getProtoClass(String name);

	IProtocolDispatcher getDispatcher();
}
