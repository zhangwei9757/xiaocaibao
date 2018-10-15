package com.tumei.controller;

import com.tumei.common.DaoGame;
import com.tumei.common.RemoteService;
import com.tumei.game.GameServer;
import com.tumei.groovy.contract.IMineSystem;
import com.tumei.model.DailyTaskBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.TaskItemBean;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2017/1/18 0018.
 */
@RestController
@RequestMapping(value = "/helper")
public class HelperController {
	private static final Log log = LogFactory.getLog(HelperController.class);

	@Autowired
	private DaoGame dao;

	@Autowired
	private GameServer server;

	@Autowired
	private RemoteService rs;

	@Autowired
	private IMineSystem mineSystem;

	@ApiOperation(value = "查看矿区玩家信息")
	@RequestMapping(value = "/infoMines", method = RequestMethod.GET)
	public String infoMines() {
		return mineSystem.dumpPlayers();
	}

	@ApiOperation(value = "调整玩家日常任务中的积分")
	@RequestMapping(value = "/modifyDailyTaskScore", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "score", value = "积分", required = true, dataType = "int", paramType = "query"),})
	public String modifyDailyTaskScore(long id, int score) {
		server.close(id);
		DailyTaskBean dtb = dao.findDailyTask(id);
		dtb.setScore(score);
		return "积分调整成功";
	}

	@ApiOperation(value = "调整玩家日常任务数值")
	@RequestMapping(value = "/modifyDailyTask", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "tid", value = "任务id", required = true, dataType = "int", paramType = "query"), @ApiImplicitParam(name = "progress", value = "设定数值", required = true, dataType = "int", paramType = "query"),})
	public String modifyDailyTask(long id, int tid, int progress) {
		server.close(id);

		DailyTaskBean dtb = dao.findDailyTask(id);
		TaskItemBean tib = dtb.getTasks().get(tid - 1);
		tib.progress = progress;
		if (tib.status == 0 && progress >= tib.limit) {
			tib.status = 1;
		}
		return "设置成功";
	}

	@ApiOperation(value = "刷新日常任务")
	@RequestMapping(value = "/flushDailyTask", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),})
	public String flushDailyTask(long id) {
		server.close(id);

		RoleBean rb = dao.findRole(id);
		DailyTaskBean dtb = dao.findDailyTask(id);
		dtb.flush(rb.getVip());
		return "日常任务刷新";
	}


}
