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
	static final Log log = LogFactory.getLog(RunnerBean.class);

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private GroovyLoader groovyLoader;

	@PostConstruct
	public void init() {

		String[] ps = new String[]{
                // 注册协议
                ("RequestLogin"),
                ("RequestCodeGet"),
                ("RequestChangeName"),
                ("RequestHotArenaInfo"),
                ("RequestHotArenaFight"),
                ("RequestHotArenaStore"),
                ("RequestHotArenaBuy"),

                ("RequestLadderChoose"),
                ("RequestLadderFight"),
                ("RequestLadderHonor"),
                ("RequestLadderInfo"),
                ("RequestLadderVideos"),
                ("RequestHonorAward"),

                ("RequestWakeEquip"),

                // 节日互动
                ("RequestFestivalInfo"),
                ("RequestFestivalBuy"),
                ("RequestFestivalGet"),
                // 开服活动
                ("RequestOpenRanks"),

                ("RequestBossInfo"),
                ("RequestBossLife"),
                ("RequestBossRank"),
                ("RequestBossGroupRank"),
                ("RequestBossFight"),
                ("RequestBossCourage"),

                ("RequestX7Sign"),
                ("RequestAdsAward"),

				// 圣物
                ("RequestRelicSummon"),
                ("RequestRelicSummonBuy"),
                ("RequestRelicActivate"),
                ("RequestRelicChange"),
                ("RequestRelicUp"),
                ("RequestRelicOn"),
                ("RequestRelicHeroUp"),

				// 注灵狂欢, 与终极抽奖
				("RequestLimitTasks"),
				("RequestLimitAward"),

				// 战争学院活动
				("RequestWarTasks"),
				("RequestWarStart"),
				("RequestWarComplete"),
				("RequestWarCancel"),

				// 每日限时领取活动
				("RequestLoginLimitReceive"),
				("RequestLimitReceive"),

				// 神秘商店活动
				("RequestRdshop"),
				("RequestRdshopActive"),
				("RequestRdshopComplete"),
				("RequestRdshopCancel"),

				// 公会红包
				//("RequestGuildbagOpen"),
				//("RequestGuildbagReceive")
		};

		for (String n : ps) {
			groovyLoader.registerProtocol("xxkg", n);
		}

		groovyLoader.registerService("common", "GroovyBattle", 1000L);
		groovyLoader.registerService("xxkg", "GroovyMineSystem", 5000L);
		groovyLoader.registerController("xxkg", "RoleController");

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
