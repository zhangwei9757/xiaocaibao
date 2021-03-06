package com.tumei;

import com.tumei.groovy.GroovyLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by Administrator on 2016/11/10 0010.
 */
@Component
public class RunnerBean implements ExitCodeGenerator {

	private Log log = LogFactory.getLog(RunnerBean.class);

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private GroovyLoader groovyLoader;

	@PostConstruct
	public void init() {
		String mod = "xxkg";

		groovyLoader.registerController(mod, "RoleController");

		// 注册协议
		groovyLoader.registerProtocol(mod, "RequestLogin");
		groovyLoader.registerProtocol(mod, "RequestCodeGet");
		groovyLoader.registerProtocol(mod, "RequestChangeName");
		groovyLoader.registerProtocol(mod, "RequestHotArenaInfo");
		groovyLoader.registerProtocol(mod, "RequestHotArenaFight");
		groovyLoader.registerProtocol(mod, "RequestHotArenaStore");
		groovyLoader.registerProtocol(mod, "RequestHotArenaBuy");

		groovyLoader.registerProtocol(mod, "RequestLadderChoose");
		groovyLoader.registerProtocol(mod, "RequestLadderFight");
		groovyLoader.registerProtocol(mod, "RequestLadderHonor");
		groovyLoader.registerProtocol(mod, "RequestLadderInfo");
		groovyLoader.registerProtocol(mod, "RequestLadderVideos");
		groovyLoader.registerProtocol(mod, "RequestHonorAward");

		groovyLoader.registerProtocol(mod, "RequestWakeEquip");

		// 节日互动
		groovyLoader.registerProtocol(mod, "RequestFestivalInfo");
		groovyLoader.registerProtocol(mod, "RequestFestivalBuy");
		groovyLoader.registerProtocol(mod, "RequestFestivalGet");
		// 开服活动
		groovyLoader.registerProtocol(mod, "RequestOpenRanks");

		// 神器
		groovyLoader.registerProtocol(mod, "RequestArtEquip");
		groovyLoader.registerProtocol(mod, "RequestArtStore");
		groovyLoader.registerProtocol(mod, "RequestArtStoreBuy");
		groovyLoader.registerProtocol(mod, "RequestArtUp");
		groovyLoader.registerProtocol(mod, "RequestArtcomUp");
		groovyLoader.registerProtocol(mod, "RequestArtcomDown");
		groovyLoader.registerProtocol(mod, "RequestArtSummonInfo");
		groovyLoader.registerProtocol(mod, "RequestArtSummon");

		groovyLoader.registerProtocol(mod, "RequestBossInfo");
		groovyLoader.registerProtocol(mod, "RequestBossLife");
		groovyLoader.registerProtocol(mod, "RequestBossRank");
		groovyLoader.registerProtocol(mod, "RequestBossGroupRank");
		groovyLoader.registerProtocol(mod, "RequestBossFight");
		groovyLoader.registerProtocol(mod, "RequestBossCourage");

		// 怪兽入侵
		groovyLoader.registerProtocol(mod, "RequestBuyDebris");
		groovyLoader.registerProtocol(mod, "RequestBuyResurgence");
		groovyLoader.registerProtocol(mod, "RequestInvadingChargeAward");
		groovyLoader.registerProtocol(mod, "RequestInvadingChargeAwardInfo");
		groovyLoader.registerProtocol(mod, "RequestInvadingDebrisAttack");
		groovyLoader.registerProtocol(mod, "RequestInvadingDebrisAttackInfo");
		groovyLoader.registerProtocol(mod, "RequestInvadingInfo");
		groovyLoader.registerProtocol(mod, "RequestInvadingLoginAward");
		groovyLoader.registerProtocol(mod, "RequestInvadingRanksInfo");

		// 战争学院
		groovyLoader.registerProtocol(mod, "RequestWarCancel");
		groovyLoader.registerProtocol(mod, "RequestWarComplete");
		groovyLoader.registerProtocol(mod, "RequestWarStart");
		groovyLoader.registerProtocol(mod, "RequestWarTasks");

		// 圣物模块
		groovyLoader.registerProtocol(mod, "RequestRelicActivate");
		groovyLoader.registerProtocol(mod, "RequestRelicChange");
		groovyLoader.registerProtocol(mod, "RequestRelicHeroUp");
		groovyLoader.registerProtocol(mod, "RequestRelicOn");
		groovyLoader.registerProtocol(mod, "RequestRelicSummon");
		groovyLoader.registerProtocol(mod, "RequestRelicSummonBuy");
		groovyLoader.registerProtocol(mod, "RequestRelicUp");

		log.info(this.getClass().getName() + " init.");
	}

	@PreDestroy
	public void dispose() {
		log.warn("--- dispose: " + this.getClass().getName());
	}

	@Override
	public int getExitCode() {
		return 0;
	}
}
