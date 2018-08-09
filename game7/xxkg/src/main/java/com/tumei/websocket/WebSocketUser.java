package com.tumei.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tumei.common.utils.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.Collection;

/**
 *
 * 带有会话的一个玩家
 *
 */
public abstract class WebSocketUser {
    protected final static Log log = LogFactory.getLog(WebSocketUser.class);

    /**
     * 会话名
     */
    protected Long uid;

    private int gmlevel;

    /**
     * 对应的处理器上下文
     */
    protected WebSocketSession socketSession;

    private InetSocketAddress address;

    private final Object sendMonitor = new Object();

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public WebSocketUser() { }

    /**
     * authenticate session
     *
     * @param session
     * @throws Exception
     */
    public void authenticate(WebSocketSession session) throws Exception {
        this.socketSession = session;
        this.setAddress(session.getRemoteAddress());

        Principal p = socketSession.getPrincipal();
        if (p != null) {
//			log.info("principal:" + p.toString());
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) p;
            Collection<GrantedAuthority> cs = token.getAuthorities();
            for (GrantedAuthority ga : cs) {
                if (ga.getAuthority().equals("ADMIN")) {
                    if (this.gmlevel < 1) {
                        this.gmlevel = 1;
                    }
                    log.warn("++++ 管理员(" + p.getName() + ") 上线." + ga.getAuthority());
                }
                if (ga.getAuthority().equals("OWNER")) {
                    if (this.gmlevel < 2) {
                        this.gmlevel = 2;
                    }
                    log.warn("++++ 游戏主宰(" + p.getName() + ") 上线." + ga.getAuthority());
                }
            }
        }
        else {
            throw new Exception("authentication fail");
        }
    }

    /**
     *
     * 服务器主动关闭这个Session
     */
    public void close() {
        synchronized (sendMonitor) {
            if (socketSession != null) {
                try {
                    if (socketSession.isOpen()) {
                        socketSession.close();
                    }
                } catch (Exception ex) {
                    log.error("close session error:" + ex.getMessage());
                } finally {
                    socketSession = null;
                }
            }
        }
    }

    /**
     * ChannelHandler回调 会话生成时，迅速返回一个16bit的随机数用于会话通信等.
     */
    public abstract void onAdd();

    /**
     * ChannelHandler回调 会话结束时，如果会话认证通过，则有玩家信息，需要将玩家信息也退出
     *
     * 这个函数只会由网络层回调，所以他是线程安全的
     */
    public abstract void onDelete();

    public abstract void update();

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
            log.error("send protocol JsonProcessingException:" + e.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("send protocol error:" + ex.getMessage());
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
            log.error("send protocol error:", ex);
            return false;
        }
        return true;
    }

    /**
     * gm level
     */
    public int getGmlevel() {
        return gmlevel;
    }

    public void setGmlevel(int gmlevel) {
        this.gmlevel = gmlevel;
    }

    /**
     * remote ip address
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

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
