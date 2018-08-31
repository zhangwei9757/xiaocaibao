package com.tumei.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import static org.springframework.util.StringUtils.hasText;

/**
 * Created by Administrator on 2016/12/29 0029.
 */
@Component("WebSocketHandler")
public class WebSocketHandler extends TextWebSocketHandler {
    private Log log = LogFactory.getLog(WebSocketHandler.class);

    /**
     * 游戏服务器中心
     */
    @Autowired
    private ISessionServer server;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//		log.info(">>> incoming connection thread(" + Thread.currentThread().getId() + ".");
        server.onAddSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("****** session:" + session.getId() + " 链接关闭,状态:" + status.toString());
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
//            log.info("收到消息: " + data + " 协议头:" + cmd);

            server.onMessage(session, cmd, data);
        } catch (Exception ex) {
            log.error("ooooo 接受消息处理中，遇到未知的错误，原因:" + ex.getMessage());
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("ooooo handlerPongMessage");
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        log.info("session text size:" + session.getTextMessageSizeLimit() +
//        " session binary size:" + session.getBinaryMessageSizeLimit());
//        log.error("连接通道发生错误:" + exception.getMessage() + " ex:" + exception.getStackTrace());
        if (session.isOpen()) {
//            log.info("错误中连接仍旧开着，准备关闭");
            session.close();
//            log.info("错误中连接仍旧开着，已经关闭");
        }
    }
}
