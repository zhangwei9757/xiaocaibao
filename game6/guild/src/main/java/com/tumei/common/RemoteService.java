package com.tumei.common;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.tumei.common.fight.GroupFightResult;
import com.tumei.common.fight.GroupFightStruct;
import com.tumei.common.service.BaseRemoteService;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.JwtUtil;
import com.tumei.dto.boss.BossAwardDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/28 0028.
 */
@Service
public class RemoteService extends BaseRemoteService {
	static final Log log = LogFactory.getLog(RemoteService.class);

	private static RemoteService instance;

	public static RemoteService getInstance() {
		return instance;
	}

	private HttpHeaders headers;

	@PostConstruct
	public void init() {
		String token = JwtUtil.generate30Days(999L, "gm", "USER,ADMIN");
		log.warn("jwtToken:[" + token + "].");
		headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
		instance = this;
	}

	@HystrixCommand(fallbackMethod = "notifyShadow")
	public boolean notifyGroup(int zone, Object obj) {
		String url = Defs.XXKG_PREFIX + "{zone}/group/notifyMessage";
		return restTemplate.postForEntity(url, obj, Boolean.class, zone).getBody();
	}

	@HystrixCommand(fallbackMethod = "notifyShadow")
	public void notifyApproval(int zone, long gid, long role) {
		String url = Defs.XXKG_PREFIX + "{zone}/group/notifyApproval?gid={gid}&role={role}";
		restTemplate.getForObject(url, boolean.class, zone, gid, role);
	}

	public GroupFightResult callFight(GroupFightStruct arg) {
		GroupFightResult reply = null;
		try {
			String url = Defs.SIMFIGHT_PREFIX + "/simGroup";
			reply = restTemplate.postForEntity(url, arg, GroupFightResult.class).getBody();
		} catch (Exception ex) {
			log.error("请求战斗失败:" + ex.getMessage());
		}
		return reply;
	}

	/**
	 * boss个人奖励
	 * @return
	 */
	public void sendBossAwards(int zone, Map<Long, Integer> roles) {
		try {
			String url = Defs.XXKG_PREFIX + "{zone}/boss/awards";
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
			String url = Defs.XXKG_PREFIX + "{zone}/boss/groupAwards";
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
			String url = Defs.CENTER_PREFIX + "/cmd/sendNotify?zone=0&msg={msg}";
			restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class, msg);
			log.info("发送全局信息");
			return;
		} catch (Exception ex) {
			log.error("全局广播信息失败:" + ex.getMessage());
		}
	}

}
