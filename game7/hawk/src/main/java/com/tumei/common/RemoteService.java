package com.tumei.common;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.tumei.common.service.BaseRemoteService;
import com.tumei.common.service.ServiceRouter;
import com.tumei.common.utils.JwtUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by Administrator on 2016/12/28 0028.
 */
@Service
public class RemoteService extends BaseRemoteService {
	private static final Log log = LogFactory.getLog(RemoteService.class);

	private static RemoteService _instance = null;

	public static RemoteService getInstance() {
		return _instance;
	}

	private static final String xxkg_prefix = "http://xxkg";

	private static final String guild_prefix = "http://Guild";

	private HttpHeaders headers;

	@Autowired
	private ServiceRouter serviceRouter;

	@PostConstruct
	void init() {
		_instance = this;
		String token = JwtUtil.generate365Days(999L, "gm", "USER,ADMIN");
		log.warn("jwtToken:[" + token + "].");
		headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
	}

	/**
	 * 通知指定的服务器,玩家充值,服务器上的玩家自己读取数据库进行检测是否有真实充值发生
	 * @param zone
	 * @param id
	 * @return
	 */
	public boolean notifyPay(int zone, Long id) {
		String url = xxkg_prefix + "{zone}/notifyPay?id={id}";
		try {
			String rtn = restTemplate.getForEntity(url, String.class, zone, id).getBody();
			if (rtn.equalsIgnoreCase("ok")) {
				return true;
			}
		} catch (Exception ex) {
			log.error("支付通知错误:" + ex.getMessage());
		}
		return false;
	}

	/**
	 * 发送全局通知消息
	 * @param zone 如果为负数,则要通知所有服务器
	 * @return
	 */
	public void sendNotifyMsg(int zone, String msg) {
		HttpEntity he = new HttpEntity(headers);
		String url = xxkg_prefix + "{zone}/cmd/broadcastSysInfo?data={msg}";
		if (zone < 0) {
			// 全局发送
			for (int i = 1; i <= -zone; ++i) {
				try {
					restTemplate.exchange(url, HttpMethod.GET, he, String.class, i, msg);
				} catch (Exception ex) {
					log.error("发送全局通知消息失败:" + ex.getMessage());
				}
			}
		} else {
			try {
			restTemplate.exchange(url, HttpMethod.GET, he, String.class, zone, msg);
			} catch (Exception ex) {
				log.error("发送全局通知消息失败:" + ex.getMessage());
			}
		}
	}

	/**
	 * 发送奖励邮件
	 * @return
	 */
	public void sendAwardMail(long uid, String title, String content, String awards) {
		try {
			int zone = serviceRouter.chooseZone(uid);
			String url = xxkg_prefix + "{zone}/cmd/addawardmail?id={uid}&title={title}&content={content}&awards={awards}";
			restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class, zone, uid, title, content, awards);
		} catch (Exception ex) {
			log.error("发送邮件奖励失败:" + ex.getMessage());
		}
	}


	public boolean groupReloadable(int mode) {
		String url = guild_prefix + "/isReloadable?mode={mode}";
		try {
			return restTemplate.getForEntity(url, boolean.class, mode).getBody();
		} catch (Exception ex) {
			log.error("发送全局通知消息失败:" + ex.getMessage());
		}

		return false;
	}

	public String flushGroup() {
		String url = guild_prefix + "/boss/flush";
		try {
			return restTemplate.getForEntity(url, String.class).getBody();
		} catch (Exception ex) {
			log.error("发送全局通知消息失败:" + ex.getMessage());
		}
		return "刷新失败";
	}
}
