package com.tumei.controller;

import com.tumei.common.DaoService;
import com.tumei.common.RemoteService;
import com.tumei.common.fight.FightStruct;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameServer;
import com.tumei.game.protos.notifys.NotifyRedPoint;
import com.tumei.game.services.RobService;
import com.tumei.model.*;
import com.tumei.model.beans.TaskItemBean;
import com.tumei.modelconf.CodeBean;
import com.tumei.modelconf.CodeBeanRepository;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/1/18 0018.
 */
@RestController
@RequestMapping(value = "/helper")
public class HelperController {
	private Log log = LogFactory.getLog(HelperController.class);

	@Autowired
	private DaoService dao;

	@Autowired
	private GameServer server;

	@Autowired
	private RemoteService rs;

	@Autowired
	private CodeBeanRepository codeBeanRepository;

//	@Autowired
//	private IMineSystem mineSystem;
//
//	@ApiOperation(value = "查看矿区玩家信息")
//	@RequestMapping(value = "/infoMines", method = RequestMethod.GET)
//	public String infoMines() {
//		return mineSystem.dumpPlayers();
//	}

	@ApiOperation(value = "给指定点玩家增加邮件")
	@RequestMapping(value = "/addmail", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "title", value = "标题", required = true, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "query")})
	public String addmail(long id, String title, String content) {
		MailsBean msb = dao.findMails(id);
		msb.addInfoMail(title, content);
		NotifyRedPoint nrp = new NotifyRedPoint();
		nrp.infos.put(1000, 1);
		server.send(id, nrp);
		return "成功增加邮件";
	}

	@ApiOperation(value = "给指定点玩家增加奖励邮件")
	@RequestMapping(value = "/addawardmail", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "title", value = "标题", required = true, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "awards", value = "奖励", required = true, dataType = "String", paramType = "query")})
	public String addawardmail(long id, String title, String content, String awards) {
		MailsBean msb = dao.findMails(id);
		msb.addAwardMail(title, content, awards);
		NotifyRedPoint nrp = new NotifyRedPoint();
		nrp.infos.put(1000, 1);
		server.send(id, nrp);
		return "成功增加邮件";
	}

	@ApiOperation(value = "给全服玩家增加奖励邮件")
	@RequestMapping(value = "/addawardmailAll", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "title", value = "标题", required = true, dataType = "String", paramType = "query"),
	@ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "query"),
	@ApiImplicitParam(name = "awards", value = "奖励", required = true, dataType = "String", paramType = "query")})
	public String addawardmailAll(String title, String content, String awards) {
		List<Long> ids = dao.getAllIds();
		for (long id : ids) {
			MailsBean msb = dao.findMails(id);
			msb.addAwardMail(title, content, awards);
		}

		return "服务器内发送邮件";
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


	@ApiOperation(value = "获取本人当前队伍的战斗力")
	@RequestMapping(value = "/getPower", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),})
	public String getPower(long id) {
		HerosBean hsb = dao.findHeros(id);
		long power = rs.callPower(hsb.createHerosStruct());

		return "战斗力:" + power;
	}

	@ApiOperation(value = "增加优惠码")
	@RequestMapping(value = "/addCode", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "query")
	})
	public String addCode(String content) {
		CodeBean cb = new CodeBean();
		try {
			String code = RandomUtil.randomChars(8);
			cb.setId(code);

			String[] fields = content.split(",");
			if (fields.length % 2 != 0) {
				return "奖励的内容应该以半角逗号分割，并且数量为偶数，ID,COUNT的形式。";
			}

			int[] arr = Arrays.stream(fields).mapToInt((s) -> {
				return Integer.parseInt(s);
			}).toArray();
			cb.setAwards(arr);
			codeBeanRepository.save(cb);
			return "新增优惠码:" + code;
		} catch (Exception ex) {
			return "新增优惠码错误:" + ex.getMessage();
		}
	}

	@ApiOperation(value = "增加优惠码")
	@RequestMapping(value = "/addCodes", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "count", value = "个数", required = true, dataType = "int", paramType = "query")
	})
	public String addCodes(String content, int count) {
		String codes = "";
		for (int i = 0; i < count; ++i) {
			CodeBean cb = new CodeBean();
			try {
				String code = RandomUtil.randomChars(8);
				cb.setId(code);

				String[] fields = content.split(",");
				if (fields.length % 2 != 0) {
					return "奖励的内容应该以半角逗号分割，并且数量为偶数，ID,COUNT的形式。";
				}

				int[] arr = Arrays.stream(fields).mapToInt((s) -> {
					return Integer.parseInt(s);
				}).toArray();
				cb.setAwards(arr);
				codeBeanRepository.save(cb);
				codes += code + "\n";
			} catch (Exception ex) {
				log.info("新增优惠码错误:" + ex.getMessage());
			}
		}

		return codes;
	}



	@ApiOperation(value = "将玩家建号时间提前一天")
	@RequestMapping(value = "/backUserCreate", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "id", value = "玩家ID", required = true, dataType = "long", paramType = "query")
	})
	public String backUserCreate(long id) {
		RoleBean rb = dao.findRole(id);
		long t = rb.getCreatetime().getTime() - 3600*24*1000;

		Instant instance = Instant.ofEpochMilli(t);
		rb.setCreatetime(Date.from(instance));

		return "帐号创建时间已经改为:" + rb.getCreatetime();
	}

	@Autowired
	private RobService robService;

	@ApiOperation(value = "查看所有碎片与对应的玩家信息")
	@RequestMapping(value = "/infoRobInfo", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "item", value = "碎片ID", required = true, dataType = "int", paramType = "query")
})
	public String infoRobInfo(int item) {
		return robService.findAllByItem(item).toString();
	}


	@ApiOperation(value = "模拟战斗前置计算，获取信息")
	@RequestMapping(value = "/simSelf", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "uid", value = "ID", required = true, dataType = "long", paramType = "query")
	})
	public String simSelf(long uid) {
		HerosBean hsb = DaoService.getInstance().findHeros(uid);
		return rs.callSimSelf(hsb.createHerosStruct());
	}


}
