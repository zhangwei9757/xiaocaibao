package com.tumei.game;

import com.tumei.GameConfig;
import com.tumei.common.DaoService;
import com.tumei.common.RemoteService;
import com.tumei.common.utils.JsonUtil;
import com.tumei.game.protos.notifys.NotifyRedPoint;
import com.tumei.game.protos.notifys.NotifyServerMessage;
import com.tumei.game.services.RobService;
import com.tumei.groovy.contract.IMineSystem;
import com.tumei.groovy.contract.IProtocolDispatcher;
import com.tumei.model.MailsBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.ISessionServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by leon on 2016/12/31.
 * <p>
 * 游戏服务器代表
 */
@Service
public class GameServer implements ISessionServer {
	public static final Log log = LogFactory.getLog(GameServer.class);

	private static GameServer instance;

	public static GameServer getInstance() {
		return instance;
	}

	@Autowired
	private GameConfig config;

	@Autowired
	private ApplicationContext sysContext;

	@Autowired
	private DaoService dao;

	@Autowired
	private RemoteService remoteService;

	@Autowired
	private RobService robService;

	@Autowired
	private IProtocolDispatcher protocolDispatcher;

	@Autowired
	private IMineSystem mineSystem;

	public DaoService getDao() {
		return dao;
	}

	public int getZone() {
		return config.getZone();
	}

	// 暂存所有消息
	private List<String> msgs = new LinkedList<>();

	public void pushServerMsg(String msg) {
		synchronized (msgs) {
			if (msgs.size() >= 30) {
				msgs.remove(0);
			}
			msgs.add(msg);
		}
	}


	/***
	 *  业务相关:
	 *  手动注册各种协议，而不是使用注解来自动进入
	 */
	@PostConstruct
	public void init() {
		instance = this;
		log.warn("[GameServer] 启动,当前所属区:[" + getZone() + "].");

//		String[] ans = sysContext.getBeanNamesForAnnotation(ProtocolAnnotation.class);
//		for (String m : ans) {
//			log.info("带有协议注解:" + m);
//			BaseProtocol nb = (BaseProtocol) sysContext.getBean(m);
//			if (nb != null) {
//				log.info("找到一个协议:" + nb.toString());
//			}
//			ProtocolAnnotation annotation = nb.getClass().getAnnotation(ProtocolAnnotation.class);
//			if (annotation == null) {
//				continue;
//			}
//		}
//		debugAllProtocols();
	}

	@PreDestroy
	public void dispose() {
		mineSystem.save();
	}

	public void debugAllProtocols() {
		Map<String, BaseProtocol> pts = sysContext.getBeansOfType(BaseProtocol.class);
		pts.values().stream().forEach(pt -> {
			Class cls = pt.getClass();
			if (cls.getName().indexOf("ReturnLogin") != -1) {
				debugClass(cls, 1);
			}
		});
	}

	public void debugClass(Class cls, int deep) {
		log.info("+++ 类(" + cls.getName() + ") 属性:");
		Field[] fields = cls.getDeclaredFields();
		for (Field f : fields) {
			String typeName = f.getGenericType().getTypeName();
			int pos = typeName.lastIndexOf(".");
			if (pos > 0) {
				typeName = typeName.substring(pos + 1);
			}

			if (f.isSynthetic()) { // 复合类型
				continue;
			}

			log.info("\t>>> " + f.getGenericType().getTypeName() + "\t" + f.getName() + " ");

			switch (typeName) {
				case "int":
				case "Integer":
				case "Long":
				case "long":
				case "String":
				case "float":
				case "Float":
					break;
				case "Log":
					break;
				default: {
				}
				break;
			}
		}
	}


	/**
	 * 根据传入的名字查询协议的具体类型，这个函数的作用是得到json反序列化需要的类型信息
	 *
	 * @param name
	 * @return
	 */
	public Class<? extends BaseProtocol> getProtoClass(String name) {
		byte[] items = name.getBytes();

		// springboot 默认注册的实体协议第一个字母是小写, 烦，转成groovy的可以随意定义。
		int i = (int) items[0];
		if (i < 97) {
			i = i - 65 + 97;
			items[0] = (byte) i;
			name = new String(items);
		}

		try {
			return (Class<? extends BaseProtocol>) sysContext.getType(name);
		} catch (NoSuchBeanDefinitionException ex) {
			log.error("--- 没有找到协议:" + name + " 失败:" + ex.getMessage());
		}
		return null;
	}

	/**
	 * 当前所有会话集合
	 */
	private ConcurrentHashMap<Long, GameUser> sessions = new ConcurrentHashMap<>();

	@Scheduled(fixedDelay = 1000)
	void update() {
//		log.info(" game baseServer loop: " + System.currentTimeMillis() / 1000);
//		log.info(" thread:" + Thread.currentThread().getId());
		try {
			sessions.forEach((Long uid, GameUser user) -> {
				user.update();
			});

			NotifyServerMessage nsm = null;
			synchronized (msgs) {
				if (msgs.size() > 0) {
					nsm = new NotifyServerMessage();
					nsm.msg.addAll(msgs);
					msgs.clear();
				}
			}
			if (nsm != null) {
				broadcast(nsm);
			}
		} catch (Exception ex) {
//			log.error("服务器 玩家逻辑更新遭遇错误.");
		}
	}

	/***
	 * 发送给指定的玩家
	 * @param proto
	 * @return
	 */
	public boolean send(long id, BaseProtocol proto) {
		GameUser gu = sessions.getOrDefault(id, null);
		if (gu != null) {
			gu.send(proto);
			return true;
		}
		return false;
	}

	/**
	 * 广播消息
	 *
	 * @param proto
	 */
	public void broadcast(BaseProtocol proto) {
		sessions.forEach((Long id, GameUser user) -> {
			user.send(proto);
		});
	}

	/**
	 * 通知指定的玩家, 相同的内容
	 *
	 * @param proto
	 * @param users
	 */
	public boolean broadcast(BaseProtocol proto, List<Long> users) {
		try {
			final String data = JsonUtil.Marshal(proto);

			if (data != null) {
				users.forEach((u) -> {
					GameUser su = sessions.getOrDefault(u, null);
					if (su != null) {
						su.send(proto.getProtoType(), data);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 强制关闭指定的会话
	 *
	 * @param id
	 */
	public void close(Long id) {
		GameUser s = sessions.get(id);
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
		GameUser s = (GameUser) sysContext.getBean("GameUser");
//        log.debug("on add ~~~session:" + s.hashCode());
		s.authenticate(_session);

		_session.getAttributes().put("USER", s);
		GameUser old = sessions.put(s.getUid(), s);
		if (old != null) {
//            log.debug("老的帐号存在 踢;");
			old.close();
		}
		s.onAdd();
	}

	/**
	 * 关闭会话
	 *
	 * @param _session
	 */
	public void onDelSession(WebSocketSession _session) {
		try {
			GameUser user = (GameUser) _session.getAttributes().get("USER");
//			GameUser user = (GameUser) sysContext.getBean("GameUser");
			if (user != null) {
//                log.debug("on del ~~~session:" + user.hashCode());
				if (sessions.remove(user.getUid(), user)) {
					user.onDelete();
				}
			}
		} catch (Exception e) {
//			log.error("onDelSession error: " + e.getMessage());
		}
	}

	public void onMessage(WebSocketSession _session, String cmd, String data) {
		try {
			GameUser user = (GameUser) _session.getAttributes().get("USER");
//			GameUser user = (GameUser) sysContext.getBean("GameUser");
			if (user != null) {
				user.onReceive(cmd, data);
			}
			else {
//                log.debug("没有找到玩家:" + _session.toString());
			}
		} catch (Exception e) {
//			log.error("onMessage error: " + e.getMessage());
		}
	}

	public GameUser find(long id) {
		return sessions.get(id);
	}

	public boolean exists(long id) {
		return sessions.containsKey(id);
	}

	/**
	 * 返回当前会话个数
	 *
	 * @return
	 */
	public int size() {
		return sessions.size();
	}

	/**
	 * 发送系统邮件
	 *
	 * @param title
	 * @param content
	 * @param awards
	 */
	public boolean sendAwardMail(long uid, String title, String content, String awards) {
		MailsBean msb = dao.findMails(uid);
		msb.addAwardMail(title, content, awards);

		GameUser user = sessions.get(uid);
		if (user != null) {
			NotifyRedPoint nrp = new NotifyRedPoint();
			nrp.infos.put(1000, 1);
			return user.send(nrp);
		}
		return false;
	}

	/**
	 * 发送公会邮件
	 *
	 * @param title
	 * @param content
	 */
	public boolean sendInfoMail(long uid, String title, String content) {
		MailsBean msb = dao.findMails(uid);
		msb.addInfoMail(title, content);

		GameUser user = sessions.get(uid);
		if (user != null) {
			NotifyRedPoint nrp = new NotifyRedPoint();
			nrp.infos.put(1000, 1);
			return user.send(nrp);
		}
		return false;
	}

	public RemoteService getRemoteService() {
		return remoteService;
	}

	/**
	 * commands 协议分发器
	 *
	 * @return
	 */
	public IProtocolDispatcher getDispatcher() {
		return protocolDispatcher;
	}

	/**
	 * commands 矿区系统
	 *
	 * @return
	 */
	public IMineSystem getMineSystem() {
		return mineSystem;
	}

	public void info(String msg) {
		log.info(msg);
	}
}
