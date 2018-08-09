package com.tumei.websocket;

import com.tumei.common.utils.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by leon on 2016/12/31.
 * <p>
 * 游戏服务器代表
 */
public class WebSocketServer {
	private final static Log log = LogFactory.getLog(WebSocketServer.class);

	@Autowired
	private ApplicationContext sysContext;

	private final static String USER_ATTR =  "xUser";

	/**
	 * 是否踢老玩家下线
	 */
	private static boolean kick_old = true;

	/**
	 * 当前所有会话集合
	 */
	protected HashMap<Long, WebSocketUser> users = new HashMap<>();

	protected final Object users_monitor = new Object();

	/**
	 * 根据传入的名字查询协议的具体类型，这个函数的作用是得到json反序列化需要的类型信息
	 *
	 * @param name
	 * @return
	 */
	public Class<? extends BaseProtocol> getProtoClass(String name) {
		try {
			byte[] items = name.getBytes();

			// springboot 默认注册的实体协议第一个字母是小写
			int i = (int) items[0];
			if (i < 97) {
				i = i - 65 + 97;
				items[0] = (byte) i;
				name = new String(items);
			}
			return (Class<? extends BaseProtocol>) sysContext.getType(name);
		} catch (NoSuchBeanDefinitionException ex) {
			log.error("--- 没有找到协议:" + name + " 失败:" + ex.getMessage());
		}
		return null;
	}

	protected List<WebSocketUser> all = new ArrayList<>();

	protected void update() {
		try {
			all.clear();
			synchronized (users_monitor) {
			    all.addAll(users.values());
			}

			all.forEach(WebSocketUser::update);
		} catch (Exception ex) {
			log.error("WebSocketServer update error:", ex);
		}
	}

	@Scheduled(fixedDelay = 1000)
	void _update() {
		update();
	}

	/***
	 * 发送给指定的玩家
	 * @param proto
	 * @return
	 */
	public boolean send(long id, BaseProtocol proto) {
		WebSocketUser user = find(id);

		if (user != null) {
			user.send(proto);
			return true;
		}
		return false;
	}

	public WebSocketUser find(long id) {
		WebSocketUser user;

		synchronized (users_monitor) {
			user = users.getOrDefault(id, null);
		}

		return user;
	}

	/**
	 * 广播消息
	 *
	 * @param proto
	 */
	public void broadcast(BaseProtocol proto) {

	    List<WebSocketUser> tmp = new ArrayList<>();
		synchronized (users_monitor) {
			tmp.addAll(users.values());
		}

		tmp.forEach((WebSocketUser WebSocketUser) -> {
			WebSocketUser.send(proto);
		});
	}

	/**
	 * 通知指定的玩家, 相同的内容
	 *
	 * @param _proto
	 * @param _users
	 */
	public void broadcast(BaseProtocol _proto, List<Long> _users) {
		try {
			final String data = JsonUtil.Marshal(_proto);

			if (data != null) {
				_users.forEach((u) -> {
					WebSocketUser su = find(u);
					if (su != null) {
						su.send(_proto.getProtoType(), data);
					}
				});
			}
		} catch (Exception e) {
			log.error("WebSocketServer broadcast error:", e);
		}
	}

	/**
	 * 强制关闭指定的会话
	 *
	 * @param id
	 */
	public void close(Long id) {
		WebSocketUser s = find(id);
		if (s != null) {
			s.close();
		}
	}

	/**
	 * 加入一个玩家
	 *
	 * @param _session
	 */
	public void onAddSession(WebSocketSession _session) throws Exception {
		WebSocketUser user = sysContext.getBean("GameUser", WebSocketUser.class);
		user.authenticate(_session);
		_session.getAttributes().put(USER_ATTR, user);
//		log.info("onAddSession:" + user.uid);

        if (!kick_old) {
			// 只能允许一个连接登录一个账号. 多登被禁止,同时关闭这个链接
			if (!addSession(user)) {
				user.send(new NotifyError("账号正在<color=red>使用中</color>,请稍后再登录..."));
				user.close();
			} else {
				user.onAdd();
			}
		} else {
        	synchronized (users_monitor) {
				WebSocketUser old = users.put(user.uid, user);
				if (old != null) {
					old.close();
				}
			}

			user.onAdd();
		}
	}

	/**
	 * 添加新的会话 返回对应uid 老的会话
	 *
	 * @param user
	 * @return
	 */
	protected boolean addSession(WebSocketUser user) {
		synchronized (users_monitor) {
			return (users.putIfAbsent(user.getUid(), user) == null);
		}
	}

	/**
	 * 实际的业务子类继承 确定删除的session是当前的会话
	 *
	 * @param user
	 * @return
	 */
	protected void removeSession(WebSocketUser user) {
	    boolean flag = false;
	    synchronized (users_monitor) {
			WebSocketUser now = users.getOrDefault(user.getUid(), null);
			if (now == user) {
				users.remove(user.getUid());
				flag = true;
			}
		}

		if (flag) {
			user.onDelete();
		}
	}

	/**
	 * 关闭会话
	 *
	 * @param _session
	 */
	public void onDelSession(WebSocketSession _session) {
		try {
			WebSocketUser user = (WebSocketUser) _session.getAttributes().get(USER_ATTR);
			if (user != null) {
//				log.info("onDelSession:" + user.uid);
				removeSession(user);
			}
		} catch (Exception e) {
			log.error("onDelSession error:", e);
		}
	}

	public void onMessage(WebSocketSession _session, String cmd, String data) {
		WebSocketUser user = (WebSocketUser) _session.getAttributes().get(USER_ATTR);
		if (user != null) {
			try {
//            log.warn("on receive thread:" + Thread.currentThread().getId());
				process(cmd, data, user);
			} catch (Exception e) {
				log.error("receive protocol [" + cmd + "] data(" + data + ") handler error:", e);
			}
		}
	}

	public void onError(WebSocketSession _session, Throwable t) {
		WebSocketUser WebSocketUser = (WebSocketUser) _session.getAttributes().get(USER_ATTR);
		if (WebSocketUser != null) {
			WebSocketUser.close();
		}
	}

	public boolean exists(long id) {
		synchronized (users_monitor) {
			return users.containsKey(id);
		}
	}

	/**
	 * 返回当前会话个数
	 *
	 * @return
	 */
	public int size() {
	    synchronized (users_monitor) {
			return users.size();
		}
	}

	/**
	 * 处理消息
	 *
	 * @param cmd
	 * @param data
	 * @param WebSocketUser
	 * @throws Exception
	 */
	public void process(String cmd, String data, WebSocketUser WebSocketUser) throws Exception {
//		log.warn("process cmd:" + cmd + " data:" + data);
		Class<? extends BaseProtocol> bp = getProtoClass(cmd);
		if (bp == null) {
			log.error("协议名(" + cmd + ") 对应的处理组件不存在.");

			// 尽量提取发送来的数据中的seq字段，返回给客户端
			NotifyError errorInfo = new NotifyError();
			errorInfo.seq = JsonUtil.forceGetSeq(data);
			errorInfo.result = "命令(" + cmd + ")没有对应的处理逻辑";
			WebSocketUser.send(errorInfo);
			return;
		}

//		log.info("bp:" + bp.getName());
		BaseProtocol protocol = JsonUtil.Unmarshal(data, bp);
		if (protocol != null) {
			protocol.process(WebSocketUser);
		}
	}

	public void test(String cmd, String data) {
		try {
//			log.warn("process cmd:" + cmd + " data:" + data);
			Class<? extends BaseProtocol> bp = getProtoClass(cmd);
			if (bp == null) {
				log.error("协议名(" + cmd + ") 对应的处理组件不存在.");
				return;
			}

//			log.info("bp:" + bp.getName());
			BaseProtocol protocol = JsonUtil.Unmarshal(data, bp);
			if (protocol != null) {
				log.info("序列化成功");
			}

		} catch (Exception ex) {
			log.error("序列化错误:" + ex);
		}
	}

}
