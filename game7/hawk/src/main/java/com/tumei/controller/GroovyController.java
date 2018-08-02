package com.tumei.controller;

import com.google.common.base.Strings;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.tumei.centermodel.ProductBean;
import com.tumei.centermodel.ServerBean;
import com.tumei.common.utils.RandomUtil;
import com.tumei.configs.MongoTemplateConfig;
import com.tumei.groovy.GroovyLoader;
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

import javax.xml.ws.Service;
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
	private ApplicationContext context;

	@Autowired
	private GroovyLoader groovyLoader;

	@Autowired
	private CodeBeanRepository codeBeanRepository;

	@Autowired
	private MongoTemplateConfig mongoTemplateConfig;


	@ApiOperation(value = "增加优惠码")
	@RequestMapping(value = "/addCodes", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "prefix", value = "前缀用来区分不同的类型,2个字符", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "mode", value = "类型,同一类型只能领取一次", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "count", value = "个数", required = true, dataType = "int", paramType = "query"),

        @ApiImplicitParam(name = "level", value = "等级限制", required = false, dataType = "int", paramType = "query", defaultValue = "0"),
        @ApiImplicitParam(name = "vip", value = "vip等级限制", required = false, dataType = "int", paramType = "query", defaultValue = "0"),
	})
	public String addCodes(String prefix, String mode, String content, int count, int level, int vip) {
		if (prefix.length() != 2) {
			return "prefix必须2个字符,数字或者字母,大小写无所谓";
		}

		String codes = "";
		for (int i = 0; i < count; ++i) {
			CodeBean cb = new CodeBean();
			try {
				String code = prefix.toUpperCase() + RandomUtil.randomChars(6);
				cb.setMode(mode);
				cb.setId(code);
				cb.setLevel(level);
				cb.setVip(vip);

				String[] fields = content.split(",");
				if (fields.length % 2 != 0) {
					return "奖励的内容应该以半角逗号分割，并且数量为偶数，ID,COUNT的形式。";
				}

				int[] arr = Arrays.stream(fields).mapToInt(Integer::parseInt).toArray();
				cb.setAwards(arr);
				codeBeanRepository.save(cb);
				codes += code + "\n";
			} catch (Exception ex) {
				log.info("新增优惠码错误:" + ex.getMessage());
			}
		}

		return codes;
	}


	@ApiOperation("云顶增加商品")
	@RequestMapping(value = "/addProducts", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "products", value = "商品名", dataType = "String", paramType = "query"),
	})
	public String addProducts(String products) {
		try {
			String url = "mongodb://192.168.1.109";

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

	@ApiOperation(value = "合服，从from数据库合并到to数据库")
	@RequestMapping(value = "/merge", method = RequestMethod.GET)

	@ApiImplicitParams( {
			@ApiImplicitParam(name = "guild", value = "公会所在数据库", defaultValue = "tm6-1", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "from", value = "准备合区 x1,x2,x3,多个一起合", defaultValue = "tm6-1", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "to", value = "目的合区 x1001", required = true, defaultValue = "tm6-1001", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "zone", value = "合并后的新区id", required = true, defaultValue = "1", dataType = "int", paramType = "query"),
	})

	public String merge(String guild, String from, String to, int zone) {
		try {
			// 预处理，删除所有待合成的数据库中的数据表
//			mongoTemplateConfig.dropDb(to);
			MongoTemplate dest = mongoTemplateConfig.otherTemplate(to);
			dest.dropCollection("Role");


			// 全体角色的集合
			HashMap<Long, DBObject> roles = new HashMap<>();
			// 本次待合并的数据库需要迁移到目的数据库的uid/1000
			HashSet<Long> newguy = new HashSet<>();

			String[] fss = from.split(",");
			for (String fs : fss) {
				MongoTemplate source = mongoTemplateConfig.otherTemplate(fs);
				// 1. 检索出所有的角色信息
				newguy.clear();
				DBCursor cursor = source.getCollection("Role").find();
				cursor.toArray().forEach(dbo -> {
					long uid = (Long)dbo.get("id") / 1000;
					int level = (Integer)dbo.get("level");
					int exp = (Integer)dbo.get("exp");
					int vip = (Integer)dbo.get("vip");
					int vipexp = (Integer)dbo.get("vipexp");
					int totaltime = (Integer) dbo.get("totaltime");

					// 如果已经存在的角色，则比较：
					// a1: vip较大的留存
					// a2: 等级较高的留存
					// a3: totaltime较大的留存
					// a4: 以上全部相同, 不要覆盖
					DBObject old = roles.getOrDefault(uid, null);
					if (old != null) {
						int old_vip = (Integer)old.get("vip");
						int old_vipexp = (Integer)old.get("vipexp");
						int old_level = (Integer)old.get("level");
						int old_exp = (Integer)old.get("exp");
						int old_totaltime = (Integer) old.get("totaltime");

						boolean flag = false;
						if (vip > old_vip || (vip == old_vip && vipexp > old_vipexp)) {
							flag = true;
						} else if (level > old_level || (level == old_level && exp > old_exp)) {
							flag = true;
						} else if (totaltime > old_totaltime) {
							flag = true;
						}

						if (flag) {
							roles.put(uid, dbo);
							newguy.add(uid);
						}
					} else {
						if (vip > 0 || vipexp > 0 || level > 20) { // 20级一下的垃圾号不要
							// 如果没有该角色可以插入
							roles.put(uid, dbo);
							newguy.add(uid);
						}
					}
				});

				// 2. 对于以上分析合成的roles,只有uid/1000满足以上roles的其他表中的数据，才会合并到目的数据库中


				String[] colls = new String[]{
						"Role.Charge", "Role.Heros", "Role.FireRaid", "Role.Mails", "Role.Package", "Role.Scene", "Role.Summon"
				};

				for (String name : colls) {
					dest.dropCollection(name);
					List<DBObject> dbos = source.getCollection(name).find().toArray();
					for (DBObject dbo : dbos) {
						long uid = ((Long)dbo.get("id")) / 1000;
						long uuid = uid * 1000 + zone;
						if (newguy.contains(uid)) {
							dbo.put("id", uuid);
							dbo.removeField("_id");
							dest.upsert(Query.query(Criteria.where("id").is(uuid)), Update.fromDBObject(dbo), name);
						}
					}
				}


				MongoTemplate gd = mongoTemplateConfig.otherTemplate(guild);
				{
					String name = "Role.Group";
					dest.dropCollection(name);
					List<DBObject> dbos = source.getCollection(name).find().toArray();
					for (DBObject dbo : dbos) {
						long originid = ((Long)dbo.get("id"));
						long uid = originid / 1000;
						long gid = ((Long)dbo.get("gid"));
						long uuid = uid * 1000 + zone;
//						if (newguy.contains(uid)) {
//							dbo.put("id", uuid);
//							dbo.removeField("_id");
//							dest.upsert(Query.query(Criteria.where("id").is(uuid)), Update.fromDBObject(dbo), name);
//
//							// 如果合并后的角色id和 原始角色id不同，则需要修改公会内角色信息
//							if (originid != uuid) {
//								GroupBean gb = gd.findOne(Query.query(Criteria.where("id").is(gid)), GroupBean.class);
//								if (gb != null) {
//									gb.preRoles.clear();
//									GroupRole gr = gb.roles.getOrDefault(originid, null);
//									if (gr != null) {
//										// 删除原来id的角色
//										gb.roles.remove(originid);
//										// 改变角色id
//										gr.id = uuid;
//										// 增加当前id的角色
//										gb.roles.put(uuid, gr);
//										gd.save(gb);
//									}
//								}
//							}
//						} else { // 如果该角色处于公会，但是又不是本次需要合并的角色，证明该角色被合并掉了，需要将公会中包含该角色的人物清除掉
//							GroupBean gb = gd.findOne(Query.query(Criteria.where("id").is(gid)), GroupBean.class);
//							if (gb != null) {
//								gb.preRoles.clear();
//								gb.roles.remove(originid);
//								gd.save(gb);
//							}
//						}
					}
				}

			}

			roles.forEach((uid, dbo) -> {
				dbo.put("id", uid * 1000 + zone);
				dest.save(dbo, "Role");
			});

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
			@ApiImplicitParam(name = "from", value = "准备合区 x1", defaultValue = "x71", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "to", value = "目的合区 x2", required = true, defaultValue = "x72", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "a", value = "1", required = true, dataType = "long", paramType = "query"),
			@ApiImplicitParam(name = "b", value = "2", required = true, dataType = "long", paramType = "query"),
	})

	public String migrate(long uid, String from, String to, long a, long b) {
		try {
			// 预处理，删除所有待合成的数据库中的数据表
//			mongoTemplateConfig.dropDb(to);
			MongoTemplate dest = mongoTemplateConfig.otherTemplate(to);

			a += uid * 1000;
			b += uid * 1000;

			String[] fss = from.split(",");
			for (String fs : fss) {
				MongoTemplate source = mongoTemplateConfig.otherTemplate(fs);

				// 2. 对于以上分析合成的roles,只有uid/1000满足以上roles的其他表中的数据，才会合并到目的数据库中


				String[] colls = new String[]{
						"Role", "Role.Charge", "Role.Heros", "Role.FireRaid", "Role.Mails", "Role.Package", "Role.Scene", "Role.Summon"
				};

				for (String name : colls) {

					DBObject dbo = source.findOne(Query.query(Criteria.where("id").is(a)), DBObject.class, name);
					dest.remove(Query.query(Criteria.where("id").is(b)), name);
					dbo.removeField("_id");
					dbo.put("id", b);
					dest.save(dbo, name);
				}
			}

		} catch (Exception e) {
			log.error("错误:", e);
			return e.getMessage();
		}

		return "迁移成功";
	}
}
