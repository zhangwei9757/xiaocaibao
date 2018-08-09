package com.tumei.controller;

import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.tumei.GameConfig;
import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.common.service.GameCache;
import com.tumei.common.utils.SystemUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.controller.cmd.CmdServerInfo;
import com.tumei.dto.MessageDto;
import com.tumei.dto.MessageDtos;
import com.tumei.game.GameServer;
import com.tumei.game.protos.notifys.NotifyMessage;
import com.tumei.game.protos.notifys.NotifyRedPoint;
import com.tumei.game.services.RankService;
import com.tumei.game.services.RobService;
import com.tumei.groovy.GroovyLoader;
import com.tumei.model.*;
import com.tumei.modelconf.CodeBeanRepository;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
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
	private static final Log log = LogFactory.getLog(CmdController.class);

	@Autowired
	private GameServer server;

	@Autowired
	private DaoGame dao;

	@Autowired
	private GameCache gameCache;

	@Autowired
	private RobService robService;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private RankService rankService;

	@Autowired
	private CodeBeanRepository codeBeanRepository;

	@Autowired
	private GroovyLoader groovyLoader;

	@ApiOperation(value = "是否热更新状态")
	@RequestMapping(value = "/isReloadable", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "mode", value = "状态:0 读取, 1 设置相反的值", required = true, dataType = "int", paramType = "query"),
	})
	public boolean isReloadable(int mode) {
		boolean rtn = groovyLoader.isReload();
		if (mode == 0) {
			return rtn;
		}

		rtn = !rtn;
		groovyLoader.setReload(rtn);
		return rtn;
	}

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
				rankService.calcday3();
				break;
			case 5:
				rankService.calcday5();
				break;
			case 7:
				rankService.calcday7();
				break;
		}

		return "发送完毕";
	}

	@ApiOperation(value = "刷新缓存")
	@RequestMapping(value = "/flushCache", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),})
	public String flushCache(long uid) {
		gameCache.invalidate(uid);
		return "刷新玩家:" + uid;
	}

	@ApiOperation(value = "发送客户端顶部通知")
	@RequestMapping(value = "/broadcastSysInfo", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "data", value = "内容", required = true, dataType = "String", paramType = "query"),})
	public String broadcastSysInfo(String data) {
		server.pushServerMsg(data);
		return "广播信息:[" + data + "]";
	}

	@ApiOperation(value = "世界聊天通知")
	@RequestMapping(value = "/broadcastMessage", method = RequestMethod.POST)
	@ApiImplicitParams({@ApiImplicitParam(name = "msg", value = "内容", required = true, dataType = "MessageDto", paramType = "parameter"),})
	public void broadcastMessage(@RequestBody MessageDto msg) {
		NotifyMessage nm = new NotifyMessage();
		nm.data.add(msg);
		NotifyMessage.pushWorld(msg);
		GameServer.getInstance().broadcast(nm);
	}

	@ApiOperation(value = "公会聊天通知")
	@RequestMapping(value = "/broadcastGroupMessage", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "msgs", value = "内容", required = true, dataType = "MessageDtos", paramType = "parameter"),
	})
	public void broadcastGroupMessage(@RequestBody MessageDtos dtos) {
		NotifyMessage nm = new NotifyMessage();
		nm.data.addAll(dtos.msgs);
		GameServer.getInstance().broadcast(nm, dtos.roles);
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

		DataStaBean dsb = DaoGame.getInstance().findDataSta(TimeUtil.getToday());
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

	@ApiOperation(value = "查看所有碎片与对应的玩家信息")
	@RequestMapping(value = "/infoRobInfo", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "item", value = "碎片ID", required = true, dataType = "int", paramType = "query")
})
	public String infoRobInfo(int item) {
		return robService.findAllByItem(item).toString();
	}


	@ApiOperation(value = "迁移玩家")
	@RequestMapping(value = "/migrate", method = RequestMethod.GET)

	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "zone", value = "目的区", required = true, dataType = "int", paramType = "query"), @ApiImplicitParam(name = "from", value = "源数据库", required = true, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "to", value = "目的数据库", required = true, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "surl", value = "源连接串", defaultValue = "mongodb://192.168.1.109:27017", required = true, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "durl", value = "目的连接串", defaultValue = "mongodb://192.168.1.109:27017", required = true, dataType = "String", paramType = "query"),})

	public String migrate(long id, int zone, String from, String to, String surl, String durl) {
		try {
			// 建立连接1
			MongoTemplate s, d;
			{
				MongoURI uri = new MongoURI(surl);
				Mongo mongo = new Mongo(uri);
				SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, from);
				s = new MongoTemplate(factory);
				s.setWriteResultChecking(WriteResultChecking.EXCEPTION);
			}
			// 建立连接2
			{
				MongoURI uri = new MongoURI(durl);
				Mongo mongo = new Mongo(uri);
				SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, to);
				d = new MongoTemplate(factory);
				d.setWriteResultChecking(WriteResultChecking.EXCEPTION);
			}

			long did = id / 1000 * 1000 + zone;
			{
				RoleBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), RoleBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), RoleBean.class);
					d.insert(tmp);
				}
			}
			{
				HerosBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), HerosBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), HerosBean.class);
					d.insert(tmp);
				}
			}
			{
				PackBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), PackBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), PackBean.class);
					tmp.setId(tmp.getId() / 1000 * 1000 + zone);
					d.insert(tmp);
				}
			}
			{
				MailsBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), MailsBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), MailsBean.class);
					d.insert(tmp);
				}
			}
			{
				StaBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), StaBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), StaBean.class);
					d.insert(tmp);
				}
			}
			{
				ChargeBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), ChargeBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), ChargeBean.class);
					d.insert(tmp);
				}
			}
			{
				RuneBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), RuneBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), RuneBean.class);
					d.insert(tmp);
				}
			}
			{
				StarBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), StarBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), StarBean.class);
					d.insert(tmp);
				}
			}
			{
				SceneBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), SceneBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), SceneBean.class);
					d.insert(tmp);
				}
			}
			{
				SummonBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), SummonBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), RuneBean.class);
					d.insert(tmp);
				}
			}
			{
				StoreBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), StoreBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), StoreBean.class);
					d.insert(tmp);
				}
			}
			{
				DailySceneBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), DailySceneBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), RuneBean.class);
					d.insert(tmp);
				}
			}
			{
				FireRaidBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), FireRaidBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), FireRaidBean.class);
					d.insert(tmp);
				}
			}
			{
				TreasureBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), TreasureBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), TreasureBean.class);
					d.insert(tmp);
				}
			}
			{
				DailyTaskBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), DailyTaskBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), DailyTaskBean.class);
					d.insert(tmp);
				}
			}

			{
				ActivityBean tmp = s.findOne(new Query(Criteria.where("id").is(id)), ActivityBean.class);
				if (tmp != null) {
					tmp.setId(did);
					d.remove(new Query(Criteria.where("id").is(did)), ActivityBean.class);
					d.insert(tmp);
				}
			}

		} catch (Exception e) {
			return e.getMessage();
		}
		return "迁移成功";
	}

}
