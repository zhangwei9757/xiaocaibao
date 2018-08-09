package com.tumei.common;

import com.tumei.common.service.BaseRemoteService;
import com.tumei.common.utils.JwtUtil;
import com.tumei.dto.MessageDto;
import com.tumei.dto.MessageDtos;
import com.tumei.dto.boss.BossAwardDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/28 0028.
 */
@Service
public class RemoteService extends BaseRemoteService {
	private static Log log = LogFactory.getLog(RemoteService.class);

	private static RemoteService instance;

	public static RemoteService getInstance() {
		return instance;
	}

	private static final String center_prefix = "http://Center";

	private static final String xxkg_prefix = "http://xxkg";

	private HttpHeaders headers;

	@PostConstruct
	public void init() {
		String token = JwtUtil.generate365Days(999L, "gm", "USER,ADMIN");
		log.warn("jwtToken:[" + token + "].");
		headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
		instance = this;
	}

	public boolean notifyGroup(int zone, Object obj) {
		String url = xxkg_prefix + "{zone}/group/notifyMessage";
		return restTemplate.postForEntity(url, obj, Boolean.class, zone).getBody();
	}

	public void notifyApproval(int zone, long gid, long role) {
		String url = xxkg_prefix + "{zone}/group/notifyApproval?gid={gid}&role={role}";
		restTemplate.getForObject(url, boolean.class, zone, gid, role);
	}

	/**
	 * boss个人奖励
	 * @return
	 */
	public void sendBossAwards(int zone, Map<Long, Integer> roles) {
		try {
			String url = xxkg_prefix + "{zone}/boss/awards";
			BossAwardDto dto = new BossAwardDto();
			dto.roles = roles;
			HttpEntity<BossAwardDto> entity = new HttpEntity<>(dto, headers);
			restTemplate.exchange(url, HttpMethod.POST, entity, Integer.class, zone);
		} catch (Exception ex) {
			log.error("奖励，分区(" + zone + ") 未发送成功，玩家(" + roles + "):" + ex.getMessage());
		}
	}

	/**
	 * boss公会奖励
	 * @return
	 */
	public void sendBossGroupAwards(int zone, Map<Long, Integer> roles) {
		try {
			String url = xxkg_prefix + "{zone}/boss/groupAwards";
			BossAwardDto dto = new BossAwardDto();
			dto.roles = roles;
			HttpEntity<BossAwardDto> entity = new HttpEntity<>(dto, headers);
			restTemplate.exchange(url, HttpMethod.POST, entity, Integer.class, zone);
		} catch (Exception ex) {
			log.error("奖励，分区(" + zone + ") 未发送成功，玩家(" + roles + "):" + ex.getMessage());
		}
	}

	/**
	 * 请求中心服务器广播所有服务器信息
	 * @return
	 */
	public void broadcast(String msg) {
		try {
			String url = center_prefix + "/cmd/sendNotify?zone=0&msg={msg}";
			restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class, msg);
			log.info("发送全局信息");
			return;
		} catch (Exception ex) {
			log.error("全局广播信息失败:" + ex.getMessage());
		}
	}

	/**
	 * 发送全局通知消息
	 * @param zone 如果为负数,则要通知所有服务器
	 * @return
	 */
	public void sendAllMessage(int zone, MessageDto msg) {
		try {
			HttpEntity<MessageDto> he = new HttpEntity<>(msg, headers);
			String url = xxkg_prefix + "{zone}/cmd/broadcastMessage";
			for (int i = 1; i <= zone; ++i) {
				restTemplate.exchange(url, HttpMethod.POST, he, String.class, i);
			}
		} catch (Exception ex) {
			log.error("发送全局聊天消息失败:" + ex.getMessage());
		}
	}

	/**
	 * 发送全局通知消息
	 * @param zone 如果为负数,则要通知所有服务器
	 * @return
	 */
	public void sendGuildMessage(int zone, List<Long> roles, List<MessageDto> msg) {
		try {
			MessageDtos md = new MessageDtos();
			md.msgs = msg;
			md.roles = roles;
			HttpEntity<MessageDtos> he = new HttpEntity<>(md, headers);
			String url = xxkg_prefix + "{zone}/cmd/broadcastGroupMessage";
			restTemplate.exchange(url, HttpMethod.POST, he, String.class, zone);
		} catch (Exception ex) {
			log.error("发送公会聊天消息失败:" + ex.getMessage());
		}
	}

}
