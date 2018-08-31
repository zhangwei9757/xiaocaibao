package com.tumei.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tumei.common.utils.JsonUtil;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.game.protos.mine.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 *
 * 会话表示一个tcp连接相关的内容
 *
 */
public class SessionUser {
    protected Log log = LogFactory.getLog(SessionUser.class);

    /**
     * 会话名
     */
    protected Long uid;

    /**
     * 对应的处理器上下文
     */
    protected WebSocketSession socketSession;

    /**
     * 服务器
     */
    protected ISessionServer baseServer;

    protected Object sendMonitor = new Object();

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public SessionUser() {}


    /**
     *
     * 服务器主动关闭这个Session
     */
    public void close() {
        if (socketSession != null) {
            try {
                if (socketSession.isOpen()) {
                    socketSession.close();
                }
            } catch (Exception ex){

            }
        }
    }

    /**
     * ChannelHandler回调 会话生成时，迅速返回一个16bit的随机数用于会话通信等.
     */
    public void onAdd() {

    }

    /**
     * ChannelHandler回调 会话结束时，如果会话认证通过，则有玩家信息，需要将玩家信息也退出
     *
     * 这个函数只会由网络层回调，所以他是线程安全的
     */
    public void onDelete() {
    }

    /**
     * ChannelHandler回调，新的协议到来
     */
    public void onReceive(String cmd, String data) {
		try {
//            log.warn("on receive thread:" + Thread.currentThread().getId());
			// 协议收到后进行处理，一定在GameUser的锁下面，对于访问公共资源的, 千万不能以对方的GameUser为锁，否则会死锁.
            // 建议线上的环境使用单线程跑逻辑, 一个服1000人同时在线很多了。
			synchronized (this) {
                baseServer.getDispatcher().process(cmd, data, this);
            }
		} catch (Exception e) {
            e.printStackTrace();
			error("协议[%s]处理失败[%s], 原因:%s %s", cmd, data, e.getMessage(), e.getStackTrace());
		}
    }

    /**
     * 发送协议
     * @param proto
     */
    public boolean send(BaseProtocol proto) {
        try {
            String data = JsonUtil.Marshal(proto);
            if (socketSession == null) {
                return false;
            }
//          log.info("序列号协议:[" + data + "]");
//			log.info("长度：" + data.length());
            synchronized (sendMonitor) {
                socketSession.sendMessage(new TextMessage(proto.getProtoType() + "|" + data));
            }
        } catch (JsonProcessingException e) {
            error("JsonProcessingException:", e);
            return false;
        } catch (Exception ex) {
            error("服务器返回协议到客户端失败:" + ex.getMessage());
            return false;
        }
        return true;
    }

    public boolean send(String type, String data) {
        try {
            if (socketSession == null) {
                return false;
            }

			synchronized (sendMonitor) {
                socketSession.sendMessage(new TextMessage(type + "|" + data));
            }
        } catch (Exception ex) {
            error("协议发送失败:", ex);
            return false;
        }
        return true;
    }

    /** 一些Session下的日志处理 **/
    private String arrange(String format, Object...args) {
        return "[ID:" + uid + "]" + String.format(format, args);
    }

    public void debug(String format, Object...args) {
        log.debug(arrange(format, args));
    }
    public void info(String format, Object...args) {
        log.info(arrange(format, args));
    }
    public void warn(String format, Object...args) {
        log.warn(arrange(format, args));
    }
    public void error(String format, Object...args) {
        log.error(arrange(format, args));
    }
}
