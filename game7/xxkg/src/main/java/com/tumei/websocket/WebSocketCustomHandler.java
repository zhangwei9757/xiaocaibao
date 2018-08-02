package com.tumei.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Created by Administrator on 2016/12/29 0029.
 *
 * WebSocket 自定义事件处理逻辑
 *
 */
@ConditionalOnProperty(value = "xcb.websocket", havingValue = "true")
@Component
public class WebSocketCustomHandler extends TextWebSocketHandler {
    private final static Log log = LogFactory.getLog(WebSocketCustomHandler.class);

    /**
     * 游戏服务器中心
     */
    @Autowired
    private WebSocketServer server;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//		log.info(">>> incoming connection thread(" + Thread.currentThread().getId() + ".");
        server.onAddSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        log.info("****** session:" + session.getId() + " 链接关闭,状态:" + status.toString());
        server.onDelSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            int pos = payload.indexOf("|");


            if (pos == -1 || pos == payload.length() - 1) {
                return;
            }

            String cmd = payload.substring(0, pos);
            String data = payload.substring(pos + 1);
            server.onMessage(session, cmd, data);
        } catch (Exception ex) {
            log.error("handleTextMessage error:", ex);
            server.onError(session, ex);
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("handlerPongMessage");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("handleTransportError:" + exception.getMessage());
        server.onError(session, exception);
    }
}
