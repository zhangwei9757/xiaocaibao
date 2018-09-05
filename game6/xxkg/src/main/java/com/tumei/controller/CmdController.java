package com.tumei.controller;

import com.tumei.GameConfig;
import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.service.CacheIt;
import com.tumei.common.utils.SystemUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.controller.cmd.CmdServerInfo;
import com.tumei.game.GameServer;
import com.tumei.game.protos.notifys.NotifyRedPoint;
import com.tumei.game.services.OpenRankService;
import com.tumei.model.DataStaBean;
import com.tumei.model.MailsBean;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

/**
 * Created by Administrator on 2017/1/18 0018.
 */
@RestController
@RequestMapping(value = "/cmd")
public class CmdController {
	private Log log = LogFactory.getLog(CmdController.class);

	@Autowired
	private GameServer server;

	@Autowired
	private DaoService dao;

	@Autowired
	private CacheIt cacheIt;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private OpenRankService openRankService;

	@ApiOperation(value = "给指定点玩家增加邮件", tags = {"邮件"})
	@RequestMapping(value = "/addmail", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
			@ApiImplicitParam(name = "title", value = "标题", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "query")
	})
	public String addmail(long id, String title, String content) {
		MailsBean msb = dao.findMails(id);
		msb.addInfoMail(title, content);
		NotifyRedPoint nrp = new NotifyRedPoint();
		nrp.infos.put(1000, 1);
		server.send(id, nrp);
		return "成功增加邮件";
	}

	@ApiOperation(value = "给指定点玩家增加奖励邮件", tags = {"邮件"})
	@RequestMapping(value = "/addawardmail", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
			@ApiImplicitParam(name = "title", value = "标题", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "awards", value = "奖励", required = true, dataType = "String", paramType = "query")
	})
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

	@ApiOperation(value = "发送开服竞赛奖励")
	@RequestMapping(value = "/sendOpenAwards", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "day", value = "几日奖励", required = true, dataType = "int", paramType = "query"),})
	public String sendOpenAwards(int day) {
		switch (day) {
			case 3:
				openRankService.calcday3();
				break;
			case 5:
				openRankService.calcday5();
				break;
			case 7:
				openRankService.calcday7();
				break;
		}

		return "发送完毕";
	}

	@ApiOperation(value = "刷新缓存")
	@RequestMapping(value = "/flushCache", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),})
	public String flushCache(long uid) {
		cacheIt.invalidate(uid);
		return "刷新玩家:" + uid;
	}


	@ApiOperation(value = "发送客户端顶部通知")
	@RequestMapping(value = "/broadcastSysInfo", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "data", value = "内容", required = true, dataType = "String", paramType = "query"),})
	public String broadcastSysInfo(String data) {
		server.pushServerMsg(data);
		return "广播信息:[" + data + "]";
	}


	@ApiOperation(value = "刷新游戏配置")
	@RequestMapping(value = "/configs", method = RequestMethod.GET)
	public String getGameConfigs() throws Exception {
		GameConfig.getInstance().reload();
		return GameConfig.getInstance().toString();
	}

	@ApiOperation(value = "刷新节日配置")
	@RequestMapping(value = "/flushFestival", method = RequestMethod.GET)
	public String flushFestival() {
		Readonly.getInstance().refreshFest();
		return "刷新节日配置";
	}

	@ApiOperation(value = "刷新只读配置")
	@RequestMapping(value = "/flushReadonly", method = RequestMethod.GET)
	public String flushReadonly() {
		Readonly.getInstance().refresh();
		return "只读配置刷新完毕";
	}

	@ApiOperation(value = "刷新活动配置")
	@RequestMapping(value = "/flushActivity", method = RequestMethod.GET)
	public String flushActivity() {
		Readonly.getInstance().refreshActivity();
		return "活动配置刷新完毕";
	}


	@ApiOperation(value = "获取服务器信息")
	@RequestMapping(value = "/getInfo", method = RequestMethod.GET)
	public CmdServerInfo getInfo() {
//		log.info("当前线程:" + Thread.currentThread().getId());
		CmdServerInfo csi = new CmdServerInfo();
		csi.users = server.size();

		DataStaBean dsb = DaoService.getInstance().findDataSta(TimeUtil.getToday());
		csi.dau = dsb.getDau();
		csi.danu = dsb.getNews().size();
		csi.charge = dsb.getCharge();
		csi.newCharge = dsb.getNewCharge();


		RuntimeMXBean rmb = ManagementFactory.getRuntimeMXBean();
		csi.bootTime = rmb.getStartTime();
		csi.upTime = rmb.getUptime() / 1000;

		ThreadMXBean tmb = ManagementFactory.getThreadMXBean();
		csi.threadCount = tmb.getThreadCount();
		csi.peekThreadCount = tmb.getPeakThreadCount();

		Runtime rt = Runtime.getRuntime();
		csi.totalMemory = SystemUtil.byteToMb(rt.totalMemory());
		csi.maxMemory = SystemUtil.byteToMb(rt.maxMemory());
		csi.freeMemory = SystemUtil.byteToMb(rt.freeMemory());
		csi.proccessor = rt.availableProcessors();
//		log.info("csi:" + csi);

		return csi;
	}
}
