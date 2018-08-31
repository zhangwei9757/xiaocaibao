package com.tumei.controller;

import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.dto.arena.ArenaAwardDto;
import com.tumei.dto.boss.BossAwardDto;
import com.tumei.game.GameServer;
import com.tumei.modelconf.BossrankConf;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/18 0018.
 *
 * 跨服竞技场排名提升通知
 *
 */
@RestController
@RequestMapping(value = "/boss")
public class BossController {
	static final Log log = LogFactory.getLog(BossController.class);

	@Autowired
	private DaoService dao;

	@Autowired
	private GameServer server;

	@ApiOperation(value = "boss个人奖励")
	@RequestMapping(value = "/awards", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "info", value = "奖励", required = true, dataType = "BossAwardDto", paramType = "parameter"),
	})
	public int bossAwards(@RequestBody BossAwardDto info) {
		List<BossrankConf> bcs = Readonly.getInstance().getBossrankConfs();

		info.roles.forEach((uid, rank) -> {
			String awds = "";
			for (BossrankConf bc : bcs) {
				if (rank <= bc.rank) {
					for (int i = 0; i < bc.reward1.length; ++i) {
						awds += bc.reward1[i];
						if (i < bc.reward1.length - 1) {
							awds += ",";
						}
					}
					break;
				}
			}

			log.info("首领战斗的奖励发送给:" + uid + " 内容:" + awds + " 排名:" + rank);
			server.sendAwardMail(uid, "首领战个人奖励", "恭喜您夺得首领战第<color=green>" + rank + "</color>名.", awds);
		});

		return 0;
	}

	@ApiOperation(value = "boss公会奖励")
	@RequestMapping(value = "/groupAwards", method = RequestMethod.POST)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "info", value = "奖励", required = true, dataType = "BossAwardDto", paramType = "parameter"),
	})
	public int groupAwards(@RequestBody BossAwardDto info) {
		List<BossrankConf> bcs = Readonly.getInstance().getBossrankConfs();

		info.roles.forEach((uid, rank) -> {
			String awds = "";
			for (BossrankConf bc : bcs) {
				if (rank <= bc.rank) {
					for (int i = 0; i < bc.reward2.length; ++i) {
						awds += bc.reward2[i];
						if (i < bc.reward2.length - 1) {
							awds += ",";
						}
					}
					break;
				}
			}

			log.info("boss战斗的公会奖励发送给:" + uid + " 内容:" + awds + " 排名:" + rank);
			server.sendAwardMail(uid, "首领战公会奖励", "恭喜您所在的公会，夺得首领战第<color=green>" + rank + "</color>名.", awds);
		});

		return 0;
	}

}

