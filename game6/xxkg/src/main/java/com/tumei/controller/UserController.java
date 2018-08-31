package com.tumei.controller;

import com.tumei.centermodel.GmOperBean;
import com.tumei.centermodel.GmOperBeanRepository;
import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.RemoteService;
import com.tumei.controller.user.BasicUserInfo;
import com.tumei.controller.user.HeroInfo;
import com.tumei.controller.user.PackInfo;
import com.tumei.game.GameServer;
import com.tumei.game.services.OpenRankService;
import com.tumei.model.*;
import com.tumei.model.beans.ArtifactBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.VipConf;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by Administrator on 2017/1/18 0018.
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {
	static final Log log = LogFactory.getLog(UserController.class);

	@Autowired
	private DaoService dao;

	@Autowired
	private GmOperBeanRepository gmOperBeanRepository;

	@Autowired
	private OpenRankService openRankService;

	@Autowired
	private GameServer server;

	@Autowired
	private RemoteService rs;

	@ApiOperation(value = "查询玩家基本信息")
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),})
	public BasicUserInfo searchById(long id) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		BasicUserInfo bui = new BasicUserInfo();
		bui.id = id;

		RoleBean rb = DaoService.getInstance().findRole(id);
		bui.name = rb.getNickname();
		bui.level = rb.getLevel();
		bui.exp = rb.getExp();
		bui.vip = rb.getVip();
		bui.vipExp = rb.getVipexp();

		PackBean pb = DaoService.getInstance().findPack(id);

		bui.gem = pb.getGem();
		bui.coin = pb.getCoin();

		ChargeBean cb = DaoService.getInstance().findCharge(id);
		bui.charge = cb.getTotal();

		bui.online = server.exists(id);

		return bui;
	}

	@ApiOperation(value = "查询玩家基本信息(昵称)")
	@RequestMapping(value = "/searchByName", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "name", value = "玩家昵称", required = true, dataType = "String", paramType = "query"),})
	public BasicUserInfo searchByName(String name) {
		long id = DaoService.getInstance().findByName(name);
		if (id == 0) {
			return null;
		}

		return searchById(id);
	}

	@ApiOperation(value = "模拟充值")
	@RequestMapping(value = "/fakeCharge", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "rmb", value = "钱（元）", required = true, dataType = "int", paramType = "query"),})
	public String fakeCharge(long id, int rmb) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		ChargeBean cb = DaoService.getInstance().findCharge(id);
		if (cb == null) {
			return "帐号不存在";
		}

		server.close(id);

		RoleBean rb = DaoService.getInstance().findRole(id);

		rmb *= 100;
		int gem = cb.doCharge(rmb, "fake", 0);
		cb.checkSendCards();

		ActivityBean ab = dao.findActivity(id);
		int vip = rb.getVip();
		int vipexp = rb.getVipexp();

		// 调整vip等级与经验
		vipexp += rmb / 10; // 继续累计经验，永远是充值的价格除以10
		VipConf vc = Readonly.getInstance().findVip(rb.getVip() + 1);
		while (vc != null && vipexp >= vc.num) {
			vipexp -= vc.num;
			vip += 1;
			ab.setVipDailyBag(0);
			vc = Readonly.getInstance().findVip(vip + 1);
		}
		rb.setVip(vip);
		rb.setVipexp(vipexp);

		openRankService.putCharge(id, rmb);
//		log.warn("管理员(" + getAdmin() + ") 给玩家(" + id + ") 充值人民币(" + rmb + "), 获得钻石(" + gem + ").");
		gmOperBeanRepository.save(new GmOperBean(1, "给玩家(" + id + ") 充值人民币(" + rmb + "), 获得钻石(" + gem + ")."));
		return "充值成功，增加钻石:" + gem + "枚.";
	}

	private String getAdmin() {
		Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (p != null) {
			UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)p;
			if (token != null) {
				return token.getName();
			}
			return p.toString();
		}
		return "未知";
	}


	@ApiOperation(value = "查看战场英雄")
	@RequestMapping(value = "/infoHeros", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),})
	public HeroInfo infoHeros(long id) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		HeroInfo hi = new HeroInfo();

		HerosBean hsb = DaoService.getInstance().findHeros(id);

		hi.skin = hsb.getSkin();

		Arrays.stream(hsb.getHeros()).forEach((hb) -> {
			if (hb != null) {
				HeroBean other = hb.clone();
				hi.heros.add(other);
			}
		});

		Arrays.stream(hsb.getAssists()).forEach((hb) -> {
			if (hb != null) {
				HeroBean other = hb.clone();
				hi.assits.add(other);
			}
		});

		hi.chain = hsb.getChain();
		hi.chainattr = hsb.getChainattr();

		hsb.getSkins().values().stream().forEach(ss -> {
			hi.skins.put(ss.id, ss.level);
		});

		return hi;
	}

	@ApiOperation(value = "查看背包")
	@RequestMapping(value = "/infoPacks", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),})
	public PackInfo infoPacks(long id) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		PackInfo hi = new PackInfo();

		PackBean pb = DaoService.getInstance().findPack(id);


		pb.getHeros().forEach((k, hb) -> {
			if (hb != null) {
				HeroBean other = hb.clone();
				hi.heros.add(other);
			}
		});

		pb.getEquips().forEach((k, hb) -> {
			if (hb != null) {
				EquipBean other = hb.clone();
				hi.equips.add(other);
			}
		});

		hi.items.putAll(pb.getItems());

		return hi;
	}

	@ApiOperation(value = "调整经验等级")
	@RequestMapping(value = "/setLevel", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "level", value = "等级", required = true, dataType = "int", paramType = "query"), @ApiImplicitParam(name = "exp", value = "等级", required = true, dataType = "int", paramType = "query"),})
	public String setLevel(long id, int level, int exp) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		RoleBean rb = DaoService.getInstance().findRole(id);
		server.close(id);

		rb.setLevel(level);
		rb.setExp(exp);

		return "当前等级修改为:" + level + "级，exp:" + exp;
	}

	@ApiOperation(value = "调整VIP经验等级,不涉及玩家的充值记录")
	@RequestMapping(value = "/setVipLevel", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "level", value = "等级", required = true, dataType = "int", paramType = "query"), @ApiImplicitParam(name = "exp", value = "等级", required = true, dataType = "int", paramType = "query"),})
	public String setVipLevel(long id, int level, int exp) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		RoleBean rb = DaoService.getInstance().findRole(id);
		server.close(id);

		rb.setVip(level);
		rb.setVipexp(exp);

//		log.warn("管理员(" + getAdmin() + ") 给玩家(" + id + ") 设置vip为:" + level + " exp:" + exp);
		gmOperBeanRepository.save(new GmOperBean(2, "给玩家(" + id + ") 设置vip为(" + level + "),  经验(" + exp + ")."));

		return "当前VIP等级修改为:" + level + "级，exp:" + exp;
	}


	@ApiOperation(value = "封禁角色")
	@RequestMapping(value = "/forbidRole", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "time", value = "时间，19700101的秒数", required = true, dataType = "date", paramType = "query"),})
	public String forbidRole(long id, long time) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		RoleBean rb = DaoService.getInstance().findRole(id);
		server.close(id);

		rb.setPlaytime(time * 1000);

		return "成功";
	}

	@ApiOperation(value = "封禁角色发言")
	@RequestMapping(value = "/forbidRoleSay", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "time", value = "时间，19700101的秒数", required = true, dataType = "date", paramType = "query"),})
	public String forbidRoleSay(long id, long time) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		RoleBean rb = DaoService.getInstance().findRole(id);
		server.close(id);

		rb.setSaytime(time * 1000);

		return "成功";
	}


	@ApiOperation(value = "修改玩家角色的创建时间")
	@RequestMapping(value = "/setCreateTime", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "date", value = "时间", required = true, dataType = "date", paramType = "query"),})
	public String setCreateTime(long id, Date date) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		RoleBean rb = DaoService.getInstance().findRole(id);
		server.close(id);

		rb.setCreatetime(date);

		return "成功";
	}


	@ApiOperation(value = "调整玩家的副本关卡")
	@RequestMapping(value = "/setScene", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"), @ApiImplicitParam(name = "scene", value = "场景", required = true, dataType = "int", paramType = "query"),})
	public String setScene(long id, int scene) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		SceneBean sb = DaoService.getInstance().findScene(id);
		server.close(id);

		sb.setScene(scene);

//		log.warn("管理员(" + getAdmin() + ") 给玩家(" + id + ") 设置副本关卡:" + scene);
		gmOperBeanRepository.save(new GmOperBean(3, "给玩家(" + id + ") 设置关卡(" + scene+ ")"));

		return "当前场景设定为:" + scene;
	}

	@ApiOperation(value = "删除玩家神器")
	@RequestMapping(value = "/delArt", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
		@ApiImplicitParam(name = "art", value = "神器id", required = true, dataType = "int", paramType = "query"),
		@ApiImplicitParam(name = "artcom", value = "神器部件id,0标识删除所有部件", required = true, dataType = "int", paramType = "query"),
	})
	public String delArt(long id, int art, int artcom) {
		if (DaoService.getInstance().findById(id) == null) {
			return null;
		}

		HerosBean hsb = DaoService.getInstance().findHeros(id);
		server.close(id);

		if (artcom == 0) {
			hsb.getArtifacts().remove(art);
		} else {
			ArtifactBean ab = hsb.getArtifacts().getOrDefault(art, null);
			if (ab != null) {
				ab.getComs().remove(artcom);
			}
		}

//		log.warn("管理员(" + getAdmin() + ") 给玩家(" + id + ") 设置副本关卡:" + scene);

		return "删除成功.";
	}

}
