package com.tumei.configs;

import com.tumei.common.fight.FightResult;
import com.tumei.common.fight.FightStruct;
import com.tumei.common.fight.PowerStruct;
import com.tumei.common.fight.SceneFightStruct;
import com.tumei.common.service.BaseRemoteService;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.JwtUtil;
import com.tumei.dto.arena.ArenaAwardDto;
import com.tumei.groovy.contract.IServiceRouter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by Administrator on 2016/12/28 0028.
 */
@Service
public class RemoteService extends BaseRemoteService {
	static final Log log = LogFactory.getLog(RemoteService.class);

	@Autowired
	private IServiceRouter sr;

	private HttpHeaders headers;

	@PostConstruct
	void init() {
		String token = JwtUtil.generate30Days(999L, "gm", "USER,ADMIN");
		log.warn("jwtToken:[" + token + "].");
		headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
	}

	/**
	 * 和真实玩家对打，只要传送玩家的信息即可
	 *
	 * @param arg
	 * @return
	 */
	public FightResult callFight(FightStruct arg) {
		FightResult reply = new FightResult();
		try {
			String url = Defs.SIMFIGHT_PREFIX + "/sim";
			reply = restTemplate.postForEntity(url, arg, FightResult.class).getBody();
			return reply;
		} catch (Exception ex) {
			log.error("请求战斗失败:" + ex.getMessage());
		}
		return reply;
	}

	public FightResult callSceneFight(SceneFightStruct arg) {
		FightResult reply = new FightResult();
		try {
			String url = Defs.SIMFIGHT_PREFIX + "/simScene";
			return restTemplate.postForEntity(url, arg, FightResult.class).getBody();
		} catch (Exception ex) {
			log.error("请求战斗失败:" + ex.getMessage());
		}
		return reply;
	}


	/**
	 * 获取战队的战斗力
	 *
	 * @param arg
	 * @return
	 */
	public long callPower(PowerStruct arg) {
		int reply = 0;
		String url = Defs.SIMFIGHT_PREFIX + "/calcPower";
		try {
			return restTemplate.postForEntity(url, arg, Integer.class).getBody();
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("计算战斗力失败:" + ex.getMessage() + " url:" + url);
		}
		return reply;
	}


	/**
	 * 最高排名奖励
	 * @return
	 */
	public void sendPeekAward(long uid, int old, int now) {
		try {
			String url = Defs.XXKG_PREFIX + "{zone}/arena/peekUp?uid={uid}&old={old}&now={now}";
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
			String url = Defs.XXKG_PREFIX + "{zone}/arena/peekDown?uid={uid}&peer={peer}&grade={grade}&now={now}";
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
			String url = Defs.XXKG_PREFIX + "{zone}/arena/arenaAwards";
			ArenaAwardDto dto = new ArenaAwardDto();
			dto.awds = awds;
			dto.roles = roles;
			HttpEntity<ArenaAwardDto> entity = new HttpEntity<>(dto, headers);
			restTemplate.exchange(url, HttpMethod.POST, entity, Integer.class, zone);
		} catch (Exception ex) {
			log.error("十点奖励，分区(" + zone + ") 未发送成功，玩家(" + roles + "), mode(" + awds + "):" + ex.getMessage());
		}
	}


}
