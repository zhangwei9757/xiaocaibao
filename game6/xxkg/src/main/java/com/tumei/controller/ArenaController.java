package com.tumei.controller;

import com.tumei.common.DaoService;
import com.tumei.common.utils.Defs;
import com.tumei.dto.arena.ArenaAwardDto;
import com.tumei.game.GameServer;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.instrument.classloading.SimpleThrowawayClassLoader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/1/18 0018.
 *
 * 跨服竞技场排名提升通知
 *
 */
@RestController
@RequestMapping(value = "/arena")
public class ArenaController {
	static final Log log = LogFactory.getLog(ArenaController.class);

	@Autowired
	private DaoService dao;

	@Autowired
	private GameServer server;

	@ApiOperation(value = "最高排名变动通知")
	@RequestMapping(value = "/peekUp", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "通知玩家", required = true, dataType = "long",
					paramType = "parameter"),
			@ApiImplicitParam(name = "old", value = "提升的排名", required = true, dataType = "int", paramType = "parameter"),
			@ApiImplicitParam(name = "now", value = "当前排名", required = true, dataType = "int", paramType = "parameter"),
	})
	public int peekUp(long uid, int old, int now) {
		int diff = 1500 - now;
		if (diff > 0) {
			if (diff < old) { // 低于1500排名的部分去掉
				old = diff;
			}
			// 历史排名奖励*20
			if (Defs.ISBT) {
				old *= 20;
			}
			server.sendAwardMail(uid, "跨服竞技场最高排名奖励", "排名提升到第" + (now+1) + "名.", "10," + old);
		}
		return 0;
	}

	@ApiOperation(value = "最高排名变动通知")
	@RequestMapping(value = "/peekDown", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "uid", value = "通知玩家", required = true, dataType = "long", paramType = "parameter"),
		@ApiImplicitParam(name = "peer", value = "攻击方", required = true, dataType = "String", paramType = "parameter"),
		@ApiImplicitParam(name = "grade", value = "品质", required = true, dataType = "int", paramType = "parameter"),
		@ApiImplicitParam(name = "now", value = "当前排名", required = true, dataType = "int", paramType = "parameter"),
	})
	public int peekDown(long uid, String peer, int grade, int now) {
		server.sendInfoMail(uid, "跨服竞技场防守失败", "玩家" + Defs.getColorString(grade, peer) + "在跨服竞技场中轻松击败了你,你的排名降至" + Defs.getColorString(1, "" + (now + 1)) + "名");
		return 0;
	}

	@ApiOperation(value = "10点奖励")
	@RequestMapping(value = "/arenaAwards", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "info", value = "奖励", required = true, dataType = "ArenaAwardDto", paramType = "parameter"),
	})
	public int arenaAwards(@RequestBody ArenaAwardDto info) {
		String awds = "";
		for (int i = 0; i < info.awds.length; ++i) {
			awds += info.awds[i];
			if (i < info.awds.length - 1) {
				awds += ",";
			}
		}

		for (long uid : info.roles) {
			server.sendAwardMail(uid, "跨服竞技场奖励", "奖励", awds);
		}

		return 0;
	}


}
