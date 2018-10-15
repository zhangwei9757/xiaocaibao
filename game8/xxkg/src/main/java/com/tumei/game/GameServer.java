package com.tumei.game;

import com.tumei.GameConfig;
import com.tumei.common.DaoGame;
import com.tumei.common.RemoteService;
import com.tumei.game.protos.notifys.NotifyRedPoint;
import com.tumei.game.protos.notifys.NotifyServerMessage;
import com.tumei.game.services.RobService;
import com.tumei.groovy.contract.IBattle;
import com.tumei.groovy.contract.IMineSystem;
import com.tumei.model.MailsBean;
import com.tumei.websocket.WebSocketServer;
import com.tumei.websocket.WebSocketUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * <p>
 * 游戏服务器代表
 */
@Service
public class GameServer extends WebSocketServer {
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
	private DaoGame dao;

	@Autowired
	private RemoteService remoteService;

	@Autowired
	private RobService robService;

	@Autowired
	private IMineSystem mineSystem;

	@Autowired
	private ApplicationContext ctx;

	public DaoGame getDao() {
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
	}

	@PreDestroy
	public void dispose() {
		mineSystem.save();
	}

	@Override
	protected void update() {
		super.update();
		try {
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

	public GameUser find(long uid) {
		return (GameUser)super.find(uid);
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

		WebSocketUser user = users.get(uid);
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

		WebSocketUser user = users.get(uid);
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
	 * commands 矿区系统
	 *
	 * @return
	 */
	public IMineSystem getMineSystem() {
		return mineSystem;
	}

	public IBattle getBattleSystem() {
		return ctx.getBean(IBattle.class);
	}

	public void info(String msg) {
		log.info(msg);
	}

	public void pushSta(long id, String action) {
		//log.info(id + "|" + action);// 取消旧的日志格式输出
	}

	/**
	 *
	 * @param uid 玩家id
	 * @param key 物品id
	 * @param left 剩余数量
	 * @param change 变更数量
	 * @param reason 行为原因
	 */
	public void pushStaResource_log(long uid, int key, long left, long change, String reason){
		GameUser user = (GameUser) users.get(uid);
		if (user == null) {
			return;
		}
		// 打印物品资源日志
		user.pushStaResource_log(key, left, change, reason);
	}
}
