package com.tumei.controller;

import com.google.common.base.Strings;
import com.tumei.common.DaoUtils;
import com.tumei.common.group.*;
import com.tumei.common.webio.AwardStruct;
import com.tumei.common.webio.BattleResultStruct;
import com.tumei.common.webio.BattleStruct;
import com.tumei.common.webio.RankStruct;
import com.tumei.controller.struct.*;
import com.tumei.model.GroupBean;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 * Created by leon on 2016/12/30.
 * <p>
 * 给游戏服务器提供的接口:
 */
@RestController
public class GroupController {
	private Log log = LogFactory.getLog(GroupController.class);

	@Autowired
	private DaoUtils dao;

	@Autowired
	private GroupService groupService;

	@ApiOperation(value = "创建公会")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "role", value = "创建者的信息", required = true, dataType = "GroupRoleMessage", paramType = "parameter"),
	@ApiImplicitParam(name = "name", value = "公会名字", required = true, dataType = "string", paramType = "query"),
	@ApiImplicitParam(name = "icon", value = "公会图标", required = true, dataType = "int", paramType = "query"),
	@ApiImplicitParam(name = "approval", value = "公会批准方案 0:自动 1:需要审批 2:拒绝加入", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	GroupReturn create(@RequestBody GroupRoleMessage role, String name, int icon, int approval) {
		GroupReturn rtn = new GroupReturn();
		try {
			long gid = dao.nextVal() * 1000 + (role.id % 1000);
			GroupBean gb = groupService.create(gid, name);
			if (gb == null) {
				rtn.result = "公会名字已经存在";
				return rtn;
			}
			gb.flush();

			gb.setIcon(icon);
			gb.setApproval(approval);

			// 将自己加入到这个公会中
			rtn.result = gb.join(role);
			if (Strings.isNullOrEmpty(rtn.result)) {
				rtn.group = gid;
				gb.setDirty(true);
			}
		} catch (Exception ex) {
			log.error("创建公会错误:", ex);
			rtn.result = "创建公会失败";
		}
		log.info("创建公会结果:" + rtn);
		return rtn;
	}

	@ApiOperation(value = "获取公会信息")
	@RequestMapping(value = "/infoGuild", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "玩家id", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	GroupMessage info(long gid, long role) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb != null) {
				gb.flush();
				GroupMessage gm = gb.createBody();
				if (!gb.getRoles().containsKey(role)) {
					gm.gid = 0; // 表示该成员不在此公会中了.
				}
				return gm;
			}
		} catch (Exception ex) {
			log.error("获取公会失败:", ex);
		}
		return null;
	}

	@ApiOperation(value = "获取公会消息记录")
	@RequestMapping(value = "/messages", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	List<String> message(long gid) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb != null) {
				return gb.getNotifys();
			}
		} catch (Exception ex) {

		}
		return null;
	}

	@ApiOperation(value = "按照公会id加入公会")
	@RequestMapping(value = "/join", method = RequestMethod.POST)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "role", value = "创建者的信息", required = true, dataType = "GroupRoleMessage", paramType = "parameter"),
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	String join(@RequestBody GroupRoleMessage role, long gid) {
		String result = "";
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			// 将自己加入到这个公会中
			result = gb.join(role);

			if (Strings.isNullOrEmpty(result)) {
				gb.setDirty(true);
			} else {
				// 如果加入公会失败，需要将之前的tryGroup还原，但是此时也许有另外的线程将他加入了公会，所以
				// 这里很麻烦，这里的方式默认认为公会gid既然这上一瞬间加入失败，在这一瞬间加入成功的机率非常低，所以不考虑
				// 只考虑另外一个公会将其批准加入，所以删除的时候带上gid进行比较。
				groupService.leaveGroup(role.id, gid);
			}
		} catch (Exception ex) {
			log.error("加入公会失败:", ex);
			result = "无法加入公会";
		}
		log.info("加入公会结果:" + result);
		return result;
	}

	@ApiOperation(value = "离开公会")
	@RequestMapping(value = "/leave", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "role", value = "玩家id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	String leave(long role, long gid) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			String rtn = gb.leave(role);

			if (Strings.isNullOrEmpty(rtn)) {
				gb.setDirty(true);
			}
			return rtn;
		} catch (Exception ex) {
			log.error("离开公会失败:", ex);
		}

		return "离开公会出错";
	}


	@ApiOperation(value = "查找公会")
	@RequestMapping(value = "/findById", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	GroupSimpleStruct findById(long gid) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return null;
			}

			return gb.createSimpleBody();
		} catch (Exception ex) {
			log.error("查找公会失败:", ex);
		}

		return null;
	}

	@ApiOperation(value = "按照公会名字查找公会")
	@RequestMapping(value = "/findByName", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "name", value = "公会名字", required = true, dataType = "string", paramType = "query"),
	})
	public
	@ResponseBody
	GroupSimpleStruct findByName(String name) {
		try {
			GroupBean gb = groupService.find(name);
			if (gb == null) {
				return null;
			}

			return gb.createSimpleBody();
		} catch (Exception ex) {
			log.error("查找公会失败:", ex);
		}

		return null;
	}


	@ApiOperation(value = "修改公会加入方式")
	@RequestMapping(value = "/modifyApproval", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "mode", value = "加入方式 0:自动 1：审批 2：拒绝", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	String modifyApproval(long gid, long role, int mode) {
		String rtn = null;
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			rtn = gb.modifyApproval(role, mode);

			if (Strings.isNullOrEmpty(rtn)) {
				gb.setDirty(true);
			}
		} catch (Exception ex) {
			log.error("修改公会加入方式错误:", ex);
			rtn = "异常错误:" + ex.getMessage();
		}
		return rtn;
	}

	@ApiOperation(value = "修改公会成员的权限级别")
	@RequestMapping(value = "/modify", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "target", value = "被操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "mode", value = "1：提升 2：降低", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	String modify(long gid, long role, long target, int mode) {
		String rtn = null;
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			rtn = gb.modify(role, target, mode);

			if (Strings.isNullOrEmpty(rtn)) {
				gb.setDirty(true);
			}
		} catch (Exception ex) {
			log.error("修改公会成员权限错误:", ex);
			rtn = "异常错误:" + ex.getMessage();
		}
		return rtn;
	}


	@ApiOperation(value = "审批待加入的成员")
	@RequestMapping(value = "/approve", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "target", value = "被操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "mode", value = "1：同意 2：拒绝", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	String approve(long gid, long role, long target, int mode) {
		String rtn = null;
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			rtn = gb.approve(role, target, mode);

			if (Strings.isNullOrEmpty(rtn)) {
				gb.setDirty(true);
			}
		} catch (Exception ex) {
			log.error("审批公会成员错误:", ex);
			rtn = "异常错误:" + ex.getMessage();
		}
		return rtn;
	}

	@ApiOperation(value = "踢人")
	@RequestMapping(value = "/kick", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "target", value = "被操作者", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	String kick(long gid, long role, long target) {
		String rtn = null;
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			rtn = gb.kick(role, target);

			if (Strings.isNullOrEmpty(rtn)) {
				gb.setDirty(true);
			}
		} catch (Exception ex) {
			log.error("审批公会成员错误:", ex);
			rtn = "异常错误:" + ex.getMessage();
		}
		return rtn;
	}


	@ApiOperation(value = "获取推荐的公会")
	@RequestMapping(value = "/recommand", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "zone", value = "玩家所在服务器", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	List<GroupSimpleStruct> recommand(int zone) {
		try {
			return groupService.findRecommands(zone);
		} catch (Exception ex) {
			log.error("获取公会推荐列表失败:", ex);
		}
		return null;
	}


	@ApiOperation(value = "弹劾会长")
	@RequestMapping(value = "/impeach", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	String impeach(long gid, long role) {
		String rtn = null;
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			rtn = gb.impeach(role);

			if (Strings.isNullOrEmpty(rtn)) {
				gb.setDirty(true);
			}
		} catch (Exception ex) {
			log.error("弹劾会长错误:", ex);
			rtn = "异常错误:" + ex.getMessage();
		}
		return rtn;
	}

	@ApiOperation(value = "修改公告")
	@RequestMapping(value = "/modifyDesc", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "desc", value = "公告", required = true, dataType = "string", paramType = "query"),
	})
	public
	@ResponseBody
	String modifyDesc(long gid, long role, String desc) {
		String rtn = null;
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			rtn = gb.modifyDesc(role, desc);

			if (Strings.isNullOrEmpty(rtn)) {
				gb.setDirty(true);
			}
		} catch (Exception ex) {
			log.error("修改公告错误:", ex);
			rtn = "异常错误:" + ex.getMessage();
		}
		return rtn;
	}

	@ApiOperation(value = "修改公告2")
	@RequestMapping(value = "/modifyNotify", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "desc", value = "公告", required = true, dataType = "string", paramType = "query"),
	})
	public
	@ResponseBody
	String modifyNotify(long gid, long role, String desc) {
		String rtn = null;
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			rtn = gb.modifyNotify(role, desc);

			if (Strings.isNullOrEmpty(rtn)) {
				gb.setDirty(true);
			}
		} catch (Exception ex) {
			log.error("修改通知错误:", ex);
			rtn = "异常错误:" + ex.getMessage();
		}
		return rtn;
	}

	@ApiOperation(value = "更新数据")
	@RequestMapping(value = "/logon", method = RequestMethod.POST)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "role", value = "创建者的信息", required = true, dataType = "GroupRoleMessage", paramType = "parameter"),
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	String logon(@RequestBody GroupRoleMessage role, long gid) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb != null) {
				if (gb.logon(role)) {
					gb.setDirty(true);
					return null;
				}
			}
		} catch (Exception ex) {
			log.error("创建公会错误:", ex);
			return "更新个人信息失败";
		}
		return "fail";
	}


	@ApiOperation(value = "公会捐献")
	@RequestMapping(value = "/donate", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "pg", value = "进度", required = true, dataType = "int", paramType = "query"),
	@ApiImplicitParam(name = "exp", value = "经验", required = true, dataType = "int", paramType = "query"),
	@ApiImplicitParam(name = "cb", value = "贡献", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	String donate(long gid, long role, int pg, int exp, int cb) {
		String rtn = null;
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "公会不存在";
			}

			rtn = gb.donate(role, pg, exp, cb);

			if (Strings.isNullOrEmpty(rtn)) {
				gb.setDirty(true);
			}
		} catch (Exception ex) {
			log.error("修改通知错误:", ex);
			rtn = "异常错误:" + ex.getMessage();
		}
		return rtn;
	}

	@ApiOperation(value = "公会捐献信息")
	@RequestMapping(value = "/donateProgress", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	Integer donateProgress(long gid, long role) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return -1;
			}
			if (!groupService.isInGroup(role, gid)) {
				return -2;
			}

			gb.flush();

			return gb.getLevel() * 10000 + gb.getProgress();
		} catch (Exception ex) {
			log.error("修改通知错误:", ex);
		}
		return -3;
	}


	@ApiOperation(value = "公会副本信息")
	@RequestMapping(value = "/sceneInfo", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	})
	public
	@ResponseBody
	GroupSceneStruct sceneInfo(long gid, long role) {
		GroupSceneStruct gss = new GroupSceneStruct();
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				gss.result = "公会不存在";
				return gss;
			}
			if (!groupService.isInGroup(role, gid)) {
				gss.result = "您已经不是该公会成员!";
				return gss;
			}
			gb.flush();

			gss.scene = gb.getScene().scene;
			gss.progress = Arrays.copyOf(gb.getScene().progress, 4);
			gb.getScene().roles.values().forEach((r) -> gss.roles.add(r));
		} catch (Exception ex) {
			log.error("修改通知错误:", ex);
		}
		return gss;
	}

	@ApiOperation(value = "公会副本战斗")
	@RequestMapping(value = "/sceneFight", method = RequestMethod.POST)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "fighter", value = "战斗数据", required = true, dataType = "BattleStruct", paramType = "parameter"),
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "index", value = "1-4关", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	BattleResultStruct sceneFight(@RequestBody BattleStruct fighter, long gid, long role, int index) {
		BattleResultStruct gss = new BattleResultStruct();
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				gss.result = "公会不存在";
				return gss;
			}
			if (!groupService.isInGroup(role, gid)) {
				gss.result = "您已经不是该公会成员!";
				return gss;
			}

			// 1. 请求本公会的战斗
			gb.flush();

			GroupRole r = gb.getRoles().get(role);

			// 2. 根据战斗结果返回值
			gb.callFight(r, fighter, index, gss);
		} catch (Exception ex) {
			log.error("修改通知错误:", ex);
		}
		return gss;
	}

	@ApiOperation(value = "公会副本指定关卡的奖励")
	@RequestMapping(value = "/sceneAwards", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "index", value = "1-4关", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	List<AwardStruct> sceneAwards(long gid, long role, int index) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return null;
			}
			if (!groupService.isInGroup(role, gid)) {
				return null;
			}

			// 1. 请求本公会的战斗
			gb.flush();
			// 2. 根据战斗结果返回值
			return gb.getSceneAwards(index - 1);
		} catch (Exception ex) {
			log.error("修改通知错误:", ex);
		}
		return null;
	}

	@ApiOperation(value = "获取指定关卡的奖励")
	@RequestMapping(value = "/getSceneAwards", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "role", value = "操作者", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "index", value = "1-4关", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	AwardStruct getSceneAwards(long gid, long role, int index) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return null;
			}
			if (!groupService.isInGroup(role, gid)) {
				return null;
			}

			// 1. 请求本公会的战斗
			gb.flush();
			// 2. 根据战斗结果返回值
			return gb.randomSceneAwards(index - 1);
		} catch (Exception ex) {
			log.error("修改通知错误:", ex);
		}
		return null;
	}

	@ApiOperation(value = "重置公会的更新日期")
	@RequestMapping(value = "/resetGroupDay", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	})
	public String resetGroupDay(long gid) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "没有这个公会";
			}

			gb.setFlushDay(0);
			return "完成";
		} catch (Exception ex) {
			log.error("修改通知错误:", ex);
		}
		return "发生错误";
	}

	@ApiOperation(value = "公会副本排行")
	@RequestMapping(value = "/ranks", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "mode", value = "排行方式", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	List<RankStruct> ranks(int mode) {
		List<RankStruct> rtn = groupService.getRanks(mode);
		try {

			// 根据RanksStruct gid获取对应的基础信息
			for (RankStruct rs : rtn) {
				groupService.fillRankStruct(rs.gid, rs);
			}
		} catch (Exception ex) {
			log.error("获取排行榜错误:", ex);
		}
		return rtn;
	}

	@ApiOperation(value = "指定公会副本的关卡")
	@RequestMapping(value = "/setSceneId", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "gid", value = "公会id", required = true, dataType = "long", paramType = "query"),
	@ApiImplicitParam(name = "scene", value = "副本id", required = true, dataType = "int", paramType = "query"),
	})
	public
	@ResponseBody
	String setSceneId(long gid, int scene) {
		try {
			GroupBean gb = groupService.find(gid);
			if (gb == null) {
				return "没有找到公会";
			}

			gb.flushToScene(scene);
		} catch (Exception ex) {
			log.error("查找公会失败:", ex);
			return "失败:" + ex.getMessage();
		}

		return "成功";
	}

}
