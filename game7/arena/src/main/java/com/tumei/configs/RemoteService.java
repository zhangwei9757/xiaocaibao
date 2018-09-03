package com.tumei.configs;

import com.tumei.common.service.BaseRemoteService;
import com.tumei.common.service.ServiceRouter;
import com.tumei.common.utils.JwtUtil;
import com.tumei.dto.arena.ArenaAwardDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.tumei.common.utils.Defs.xxkg_prefix;

/**
 * Created by Administrator on 2016/12/28 0028.
 */
@Service
public class RemoteService extends BaseRemoteService {
	private static final Log log = LogFactory.getLog(RemoteService.class);

	@Autowired
	private ServiceRouter sr;

	private HttpHeaders headers;

	@PostConstruct
	void init() {
		String token = JwtUtil.generate365Days(999L, "gm", "USER,ADMIN");
		log.warn("jwtToken:[" + token + "].");
		headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
	}

	/**
	 * 最高排名奖励
	 * @return
	 */
	public void sendPeekAward(long uid, int old, int now) {
		try {
			String url = xxkg_prefix + "{zone}/arena/peekUp?uid={uid}&old={old}&now={now}";
			restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Integer.class, sr.chooseZone(uid), uid, old, now);
		} catch (Exception ex) {
			log.error("玩家(" + uid + ") 战斗力从" + old + "提升到" + now + "同步邮件奖励失败.");
		}
	}

	/**
	 * 排名下降通知
	 * @return
	 */
	public void sendFailInfo(long uid, String peer, int grade, int now) {
		try {
			String url = xxkg_prefix + "{zone}/arena/peekDown?uid={uid}&peer={peer}&grade={grade}&now={now}";
			restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Integer.class, sr.chooseZone(uid), uid, peer, grade, now);
		} catch (Exception ex) {

		}
	}

	/**
	 * 21点奖励
	 * @return
	 */
	public void sendAwards(int zone, int[] awds, List<Long> roles) {
		try {
			String url = xxkg_prefix + "{zone}/arena/arenaAwards";
			ArenaAwardDto dto = new ArenaAwardDto();
			dto.awds = awds;
			dto.roles = roles;
			HttpEntity<ArenaAwardDto> entity = new HttpEntity<>(dto, headers);
			restTemplate.exchange(url, HttpMethod.POST, entity, Integer.class, zone);
			log.info("奖励，分区(" + zone + ") 发送成功，玩家(" + roles + ")");
		} catch (Exception ex) {
			log.error("奖励，分区(" + zone + ") 未发送成功，玩家(" + roles + "), mode(" + awds.length + "):" + ex.getMessage());
		}
	}


}
