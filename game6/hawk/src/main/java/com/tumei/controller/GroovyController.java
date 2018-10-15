package com.tumei.controller;

import com.google.common.base.Strings;
import com.mongodb.*;
import com.tumei.centermodel.ProductBean;
import com.tumei.centermodel.ServerBean;
import com.tumei.centermodel.ServersBean;
import com.tumei.common.utils.RandomUtil;
import com.tumei.configs.MongoTemplateConfig;
import com.tumei.groovy.GroovyLoader;
import com.tumei.model.GroupBean;
import com.tumei.model.RoleBean;
import com.tumei.model.structs.GroupRole;
import com.tumei.modelconf.CodeBean;
import com.tumei.modelconf.CodeBeanRepository;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/1/18 0018.
 * <p>
 * 负责groovy脚本:
 * 1. 发布
 * 2. 刷新
 * 3. 简单测试
 */
@RestController
@RequestMapping(value = "/groovy")
public class GroovyController {
	private Log log = LogFactory.getLog(GroovyController.class);

	@Autowired
	@Qualifier("centerTemplate")
	private MongoTemplate mongoTemplate;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private GroovyLoader groovyLoader;

	@Autowired
	private CodeBeanRepository codeBeanRepository;

	@Autowired
	private MongoTemplateConfig mongoTemplateConfig;

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

			int[] arr = Arrays.stream(fields).mapToInt(Integer::parseInt).toArray();
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
		StringBuilder codes = new StringBuilder();
		for (int i = 0; i < count; ++i) {
			CodeBean cb = new CodeBean();
			try {
				String code = RandomUtil.randomChars(8);
				cb.setId(code);

				String[] fields = content.split(",");
				if (fields.length % 2 != 0) {
					return "奖励的内容应该以半角逗号分割，并且数量为偶数，ID,COUNT的形式。";
				}

				int[] arr = Arrays.stream(fields).mapToInt(Integer::parseInt).toArray();
				cb.setAwards(arr);
				codeBeanRepository.save(cb);
				codes.append(code).append("\n");
			} catch (Exception ex) {
				log.info("新增优惠码错误:" + ex.getMessage());
			}
		}

		return codes.toString();
	}


	@ApiOperation("云顶增加商品")
	@RequestMapping(value = "/addProducts", method = RequestMethod.POST)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "host", value = "外网选择 1:云顶, 2:DZong", required = true, dataType = "int", paramType = "query"),
	@ApiImplicitParam(name = "products", value = "商品名", dataType = "String", paramType = "query"),
	@ApiImplicitParam(name = "clear", value = "是否清空对方表", dataType = "boolean", paramType = "query"),
	})
	public String addProducts(int host, String products, boolean clear) {
		try {
			String url = "";
			if (host == 1) {
				url = "mongodb://tumei006:tmPassword1@115.159.36.195";
			} else if (host == 2) {
				url = "mongodb://tumei006:tmPassword1@118.89.102.182";
			} else {
				return "选择错误的外网服务器.";
			}

			MongoTemplate dst = null;
			{
				MongoURI uri = new MongoURI(url);
				Mongo mongo = new Mongo(uri);
				mongo.getMongoOptions().socketTimeout = 3;
				SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, "center");
				dst = new MongoTemplate(factory);
				dst.setWriteResultChecking(WriteResultChecking.EXCEPTION);
			}

			String[] lines = products.split(";");
			for (String line : lines) {
				String[] fields = line.split("=");
				if (fields.length != 2) {
					continue;
				}

				int rmb = Integer.parseInt(fields[0].trim())*100;
				String n = fields[1].trim();

				log.info("rmb:" + rmb + ", name:" + n);
				ProductBean pb = new ProductBean();
				pb.rmb = rmb;
				pb.product = n;
				dst.insert(pb);
			}

			return "结束";
		} catch (Exception ex) {
			return "出错:" + ex.getMessage();
		}
	}


	@ApiOperation("同步conf")
	@RequestMapping(value = "/syncConf", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "host", value = "外网选择 1:云顶, 2:DZong", required = true, dataType = "int", paramType = "query"),
	@ApiImplicitParam(name = "name", value = "数据表名字", dataType = "String", paramType = "query"),
	@ApiImplicitParam(name = "clear", value = "是否清空对方表", dataType = "boolean", paramType = "query"),
	})
	public String syncConf(int host, String name, boolean clear) {
		try {
			String url = "";
			if (host == 1) {
				url = "mongodb://tumei006:tmPassword1@115.159.36.195";
			} else if (host == 2) {
				url = "mongodb://tumei006:tmPassword1@118.89.102.182";
			} else {
				return "选择错误的外网服务器.";
			}

			MongoTemplate local = null;
			MongoTemplate dst = null;
			{
				{
					MongoURI uri = new MongoURI("mongodb://192.168.1.109");
					Mongo mongo = new Mongo(uri);
					mongo.getMongoOptions().socketTimeout = 3;
					SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, "tm6conf");
					local = new MongoTemplate(factory);
					local.setWriteResultChecking(WriteResultChecking.EXCEPTION);
				}

				{
					MongoURI uri = new MongoURI(url);
					Mongo mongo = new Mongo(uri);
					mongo.getMongoOptions().socketTimeout = 3;
					SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, "tm6conf");
					dst = new MongoTemplate(factory);
					dst.setWriteResultChecking(WriteResultChecking.EXCEPTION);
				}
			}

			String msg = "";
			String[] ns = name.split(",");
			for (String n : ns) {
				if (!dst.collectionExists(n)) {
					msg += "创建新表 ";
				}

				{
					if (clear) {
						dst.dropCollection(n);
						msg += " 清理并更新表(" + n + ")\n";
					} else {
						msg += " 不清理增量 更新表(" + n + ")\n";
					}
					List<Object> all = local.findAll(Object.class, n);
					if (all.size() <= 0) {
						msg += "ERR:" + n + " 本地表内没有一行数据，请检查是否拼写错误";
					} else {
						dst.insert(all, n);
					}
				}
			}
			return msg;
		} catch (Exception ex) {
			return "出错:" + ex.getMessage();
		}
	}




	@ApiOperation("云顶开新服")
	@RequestMapping(value = "/makeNewYd", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "zone", value = "新服id", required = true, dataType = "int", paramType = "query"),
	@ApiImplicitParam(name = "host", value = "服务器地址: 2号(10.154.38.79)", defaultValue = "10.154.136.73:50013", dataType = "String", paramType = "query"),
	@ApiImplicitParam(name = "name", value = "开启的新服名字", dataType = "String", paramType = "query"),
	@ApiImplicitParam(name = "status", value = "服务器状态", required = true, dataType = "String", paramType = "query"),
	@ApiImplicitParam(name = "date", value = "服务器开放时间", defaultValue = "2017-12-30 9:00:00", dataType = "String", paramType = "query"),
	@ApiImplicitParam(name = "db", value = "数据库前缀(x，否则留空)", dataType = "String", paramType = "query"),})
	public String makeYd(String name, String db, int zone, String host, String status, String date) {

		try {
			Date d = null;
			if (!Strings.isNullOrEmpty(date)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				d = sdf.parse(date);
			}

			return makeNewServer("mongodb://tumei006:tmPassword1@115.159.36.195",
			name, status, db, zone, "center", host, d);
		} catch (Exception ex) {
			return "出错:" + ex.getMessage();
		}
	}





	@ApiOperation("查看D服的所有服务器列表.")
	@RequestMapping(value = "/listD", method = RequestMethod.GET)
	public String listD() {
		return listServers("mongodb://tumei006:tmPassword1@118.89.102.182", "center");
	}

	@ApiOperation("查看云顶服的所有服务器列表")
	@RequestMapping(value = "/listYd", method = RequestMethod.GET)
	public String listYd() {
		return listServers("mongodb://tumei006:tmPassword1@115.159.36.195", "center");
	}

	@ApiOperation("D开新服")
	@RequestMapping(value = "/makeNewD", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "zone", value = "新服id", required = true, dataType = "int", paramType = "query"),
		@ApiImplicitParam(name = "host", value = "服务器地址(118.89.102.182)", defaultValue = "118.89.157.197:5009", dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "name", value = "开启的新服名字", dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "status", value = "服务器状态", defaultValue = "新服", dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "date", value = "服务器开放时间(2017-12-30 9:00:00)", defaultValue = "2017-12-30 9:00:00", dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "db", value = "数据库前缀(x，否则留空)", dataType = "String", paramType = "query"),})
	public String makeD(String name, String db, int zone, String host, String status, String date) {

		try {
			Date d = null;
			if (!Strings.isNullOrEmpty(date)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				d = sdf.parse(date);
			}

			return makeNewServer("mongodb://tumei006:tmPassword1@118.89.102.182",
					name, status, db, zone, "center", host, d);
		} catch (Exception ex) {
			return "出错:" + ex.getMessage();
		}
	}



//	@ApiOperation("内网开新服")
//	@RequestMapping(value = "/makeNewServer", method = RequestMethod.GET)
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "zone", value = "新服id", required = true, dataType = "int", paramType = "query"),
//		@ApiImplicitParam(name = "host", value = "服务器地址", defaultValue = "192.168.1.222:5001", dataType = "String", paramType = "query"),
//		@ApiImplicitParam(name = "name", value = "开启的新服名字", dataType = "String", paramType = "query"),
//		@ApiImplicitParam(name = "status", value = "服务器状态", defaultValue = "新服", dataType = "String", paramType = "query"),
//		@ApiImplicitParam(name = "date", value = "服务器开放时间", defaultValue = "2017-12-30 9:00:00", dataType = "String", paramType = "query"),
//		@ApiImplicitParam(name = "db", value = "数据库前缀(需要创建数据库是填写，否则留空)", dataType = "String", paramType = "query"),})
//	public String makeInner(String name, String db, int zone, String status, String host, String date) {
//
//		try {
//			Date d = null;
//			if (!Strings.isNullOrEmpty(date)) {
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//				d = sdf.parse(date);
//			}
//
//			return makeNewServer("mongodb://192.168.1.109",
//				name, status, db, zone, "tm6center", host, d);
//		} catch (Exception ex) {
//			return "出错:" + ex.getMessage();
//		}
//	}

	public String listServers(String url, String db) {
		// 1. 创建数据库 x${zone}
		// 2. 将local中的Role.Ranks拷贝到新数据库
		// 3. 修改center中的Servers
		try {
			MongoTemplate center;
			{
				MongoURI uri = new MongoURI(url);
				Mongo mongo = new Mongo(uri);

				{
					SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, db);
					center = new MongoTemplate(factory);
					center.setWriteResultChecking(WriteResultChecking.EXCEPTION);
				}
			}

			ServersBean ssb = center.findOne(new Query(), ServersBean.class);
			if (ssb != null) {
				String msg = "";
				for (ServerBean sb : ssb.getServers()) {
					msg += sb.toString() + "\n";
				}

				return msg;
			}

		} catch (Exception ex) {

		}
		return "错误";
	}



	public String makeNewServer(String url, String name, String status, String db, int zone, String sdb, String host, Date date) {
		// 1. 创建数据库 x${zone}
		// 2. 将local中的Role.Ranks拷贝到新数据库
		// 3. 修改center中的Servers
		try {
			if (!Strings.isNullOrEmpty(db)) {
				db = db + zone;
			}

			MongoTemplate local = null;
			MongoTemplate dst = null;
			MongoTemplate center;
			{
				MongoURI uri = new MongoURI(url);
				Mongo mongo = new Mongo(uri);
				mongo.getMongoOptions().socketTimeout = 3;
				if (!Strings.isNullOrEmpty(db)) {
					SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, "local");
					local = new MongoTemplate(factory);
					local.setWriteResultChecking(WriteResultChecking.EXCEPTION);
				}

				if (!Strings.isNullOrEmpty(db)) {
					SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, db);
					dst = new MongoTemplate(factory);
					dst.setWriteResultChecking(WriteResultChecking.EXCEPTION);
				}

				{
					SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, sdb);
					center = new MongoTemplate(factory);
					center.setWriteResultChecking(WriteResultChecking.EXCEPTION);
				}
			}

			if (!Strings.isNullOrEmpty(db)) {
				if (dst.collectionExists("Role.Ranks")) {

				} else {
					List<Object> rbs = local.findAll(Object.class, "Role.Ranks");
					dst.insert(rbs, "Role.Ranks");
				}
			}

			ServersBean ssb = center.findOne(new Query(), ServersBean.class);
			if (ssb != null) {
				for (ServerBean sb : ssb.getServers()) {
					if (sb.id == zone) {
						if (!Strings.isNullOrEmpty(name)) {
							sb.name = name;
						}
						if (date != null) {
							sb.start = date;
						}
						if (!Strings.isNullOrEmpty(host)) {
							sb.host = host;
						}
						if (!Strings.isNullOrEmpty(status)) {
							sb.status = status;
						}

						center.save(ssb);
						return "更新已经存在服务器:" + sb.toString();
					}
				}

				// 需要创建
				ServerBean sb  = new ServerBean();
				sb.id = zone;
				sb.name = name;
				sb.start = date;
				sb.host = host;
				ssb.getServers().add(sb);

				center.save(ssb);
				return "新增服务器:" + sb.toString();
			}

		} catch (Exception ex) {
			return "创建新服数据库失败:" + ex.getMessage();
		}

		return "开启新服(" + name + ") zone(" + zone + ").";
	}


	@ApiOperation(value = "合服，从from数据库合并到to数据库")
	@RequestMapping(value = "/merge", method = RequestMethod.GET)

	@ApiImplicitParams( {
			@ApiImplicitParam(name = "froms", value = "准备合区 1", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "to", value = "目的合区 2", required = true, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "prefix", value = "数据库前缀", required = true, dataType = "String", paramType = "query"),
	})

	public String merge(String froms, int to, String prefix) {
		try {
			MongoTemplate dest = mongoTemplateConfig.otherTemplate(prefix + to);

			String[] fs = froms.split(",");
			int f1 = Integer.parseInt(fs[0]);
			int f2 = Integer.parseInt(fs[1]);

			for (int from = f1; from <= f2; ++from) {

				MongoTemplate source = mongoTemplateConfig.otherTemplate(prefix + from);

				// 2. 对于以上分析合成的roles,只有uid/1000满足以上roles的其他表中的数据，才会合并到目的数据库中
				String[] colls = new String[]{
						"Role.Activity",
						"Role.Charge",
						"Role.DailyScene",
						"Role.FireRaid",
						"Role.Friends",
						"Role.Group",
						"Role.Heros",
						"Role.Invading",
						"Role.Mails",
						"Role.Package",
						"Role.Robs",
						"Role.Rune",
						"Role.Scene",
						"Role.Sta",
						"Role.Star",
						"Role.Store",
						"Role.Summon",
						"Role.Tasks",
						"Role.Treasure",
						"Role.War",
						"Role.boss"
				};

				// 找到所有在from数据库中Role表的角色，然后全部迁移到to中
                List<DBObject> rs = source.findAll(DBObject.class, "Role");

                for (DBObject rb : rs) {
                    long a = (long)rb.get("id");

					dest.remove(Query.query(Criteria.where("id").is(a)), "Role");
					dest.upsert(Query.query(Criteria.where("id").is(a)), Update.fromDBObject(rb), "Role");


					for (String name : colls) {
						log.info("准备转移角色数据表:" + name + " from:" + from + " to " + to);
						DBObject dbo = source.findOne(Query.query(Criteria.where("id").is(a)), DBObject.class, name);
						if (dbo != null) {
							if (name.equalsIgnoreCase("Role.Activity")) {
								dbo.put("lastIndex", -1);
								dbo.put("lastCumIdx", -1);
								dbo.put("lastDcIdx", -1);
								dbo.put("campaignRound", -1);
								dbo.put("dbLocalRound", -1);
							}

							dest.remove(Query.query(Criteria.where("id").is(a)), name);
							dest.upsert(Query.query(Criteria.where("id").is(a)), Update.fromDBObject(dbo), name);
						}
					}
				}
			}

		} catch (Exception e) {
			log.error("错误:", e);
			return e.getMessage();
		}

		return "合服成功";
	}


	@ApiOperation(value = "迁移，从from数据库合并到to数据库")
	@RequestMapping(value = "/migrate", method = RequestMethod.GET)

	@ApiImplicitParams( {
			@ApiImplicitParam(name = "uid", value = "账号", required = true, dataType = "Long", paramType = "query"),
			@ApiImplicitParam(name = "from", value = "准备合区 1", required = true, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "to", value = "目的合区 2", required = true, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "prefix", value = "数据库前缀", required = true, dataType = "String", paramType = "query"),
	})

	public String migrate(long uid, int from, int to, String prefix) {
		try {
			// 预处理，删除所有待合成的数据库中的数据表
//			mongoTemplateConfig.dropDb(to);
			MongoTemplate dest = mongoTemplateConfig.otherTemplate(prefix + to);

			long a = uid * 1000 + from;

			{
				MongoTemplate source = mongoTemplateConfig.otherTemplate(prefix + from);

				// 2. 对于以上分析合成的roles,只有uid/1000满足以上roles的其他表中的数据，才会合并到目的数据库中
				String[] colls = new String[]{
						"Role",
						"Role.Activity",
						"Role.Charge",
						"Role.DailyScene",
//						"Role.Festival",
						"Role.FireRaid",
                        "Role.Friends",
						"Role.Group",
                        "Role.Heros",
						"Role.Invading",
                        "Role.Mails",
						"Role.Package",
						"Role.Robs",
						"Role.Rune",
						"Role.Scene",
						"Role.Sta",
						"Role.Star",
						"Role.Store",
						"Role.Summon",
						"Role.Tasks",
						"Role.Treasure",
						"Role.War",
						"Role.boss"
				};

				for (String name : colls) {
				    log.info("准备转移角色数据表:" + name + " from:" + from + " to " + to);
					DBObject dbo = source.findOne(Query.query(Criteria.where("id").is(a)), DBObject.class, name);
					if (dbo != null) {
//						dbo.removeField("_id");
//						dbo.put("id", b);

						if (name.equalsIgnoreCase("Role.Activity")) {
							dbo.put("lastIndex", -1);
							dbo.put("lastCumIdx", -1);
							dbo.put("lastDcIdx", -1);
							dbo.put("campaignRound", -1);
							dbo.put("dbLocalRound", -1);
						}

                        dest.remove(Query.query(Criteria.where("id").is(a)), name);
						dest.upsert(Query.query(Criteria.where("id").is(a)), Update.fromDBObject(dbo), name);
					}
				}
			}

		} catch (Exception e) {
			log.error("错误:", e);
			return e.getMessage();
		}

		return "迁移成功";
	}

}
