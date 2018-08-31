package com.tumei.common;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.tumei.common.service.BaseRemoteService;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.JwtUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URLDecoder;

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


	private HttpHeaders headers;

	@PostConstruct
	void init() {
		_instance = this;
		String token = JwtUtil.generate30Days(999L, "gm", "USER,ADMIN");
		log.warn("jwtToken:[" + token + "].");
		headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
	}

	@HystrixCommand(fallbackMethod = "notifyShadow")
	public boolean notifyPay(int zone, Long id) {
		String url = Defs.XXKG_PREFIX + "{zone}/notifyPay?id={id}";
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
		try {
			String url = Defs.XXKG_PREFIX + "{zone}/cmd/broadcastSysInfo?data={msg}";
			if (zone < 0) {
				// 全局发送
				for (int i = 1; i <= -zone; ++i) {
					restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class, i, msg);
				}
			} else {
				restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class, zone, msg);
			}
		} catch (Exception ex) {

		}
	}

	public void mobNotify(String callback) {
		try {
			String url = URLDecoder.decode(callback, "UTF-8");
			simpleTemplate.getForObject(url, String.class);
		} catch (Exception ex) {
			log.error("通知mobcastle失败:" + ex.getMessage());
		}
	}
}
