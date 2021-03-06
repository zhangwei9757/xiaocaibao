package com.tumei.groovy.commands.controller

import com.tumei.centermodel.GmOperBean
import com.tumei.centermodel.GmOperBeanRepository
import com.tumei.centermodel.ReceiptBean
import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.utils.RandomUtil
import com.tumei.controller.user.BasicUserInfo
import com.tumei.controller.user.PackInfo
import com.tumei.dto.db2proto.NameValue
import com.tumei.game.GameServer
import com.tumei.game.services.RankService
import com.tumei.model.*
import com.tumei.modelconf.RaidConf
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest

/**
 * Created by Leon on 2018/3/12.
 */
class RoleController {
    private static final Log log = LogFactory.getLog(RoleController.class)

    @Autowired
    private ApplicationContext context
    @Autowired
    private DaoService dao
    @Autowired
    private GameServer server
    @Autowired
    private GmOperBeanRepository gmOperBeanRepository
    @Autowired
    private RankService openRankService

    private BasicUserInfo search(long id) {
        if (dao.findById(id) == null) {
            return null
        }

        BasicUserInfo bui = new BasicUserInfo()
        bui.id = id
        RoleBean rb = dao.findRole(id)
        bui.name = rb.getNickname()
        bui.level = rb.getLevel()
        bui.exp = rb.getExp()
        bui.vip = rb.getVip()
        bui.vipExp = rb.getVipexp()
        bui.fr = rb.getPlaytime()
        bui.fs = rb.getSaytime()

        PackBean pb = dao.findPack(id)
        bui.gem = pb.getGem()
        bui.coin = pb.getCoin()

        ChargeBean cb = dao.findCharge(id)
        bui.charge = cb.getTotal()
        bui.online = server.exists(id)

        return bui
    }


    @ApiOperation(value = "查询玩家基本信息")
    @RequestMapping(value = "/role/search", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
    ])
    @ResponseBody BasicUserInfo searchById(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))
        return search(id)
    }

    @ApiOperation(value = "查询玩家基本信息(昵称)")
    @RequestMapping(value = "/role/searchByName", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "name", value = "玩家昵称", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody BasicUserInfo searchByName(HttpServletRequest request) {
        String name = request.getParameter("name")
        long id = dao.findByName(name)
        if (id == 0) {
            return null
        }
        return search(id)
    }

    @ApiOperation(value = "调整经验等级")
    @RequestMapping(value = "/role/fixLevel", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "level", value = "等级", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "exp", value = "等级", required = false, defaultValue = "1", dataType = "int", paramType = "query")])
    @ResponseBody String fixLevel(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))
        int level = Integer.parseInt(request.getParameter("level"))
        int exp = 1

        if (dao.findById(id) == null) {
            return null
        }

        RoleBean rb = dao.findRole(id)
        server.close(id)

        rb.setLevel(level)
        rb.setExp(exp)
        gmOperBeanRepository.save(new GmOperBean(2, rb.getNickname() + ": 设置等级:" + level + " 经验:" + exp))

        return "当前等级修改为:" + level + "级，exp:" + exp
    }

    @ApiOperation(value = "调整vip经验等级")
    @RequestMapping(value = "/role/fixVipLevel", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "level", value = "等级", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "exp", value = "等级", required = false, defaultValue = "1", dataType = "int", paramType = "query")])
    @ResponseBody String fixVipLevel(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))
        int level = Integer.parseInt(request.getParameter("level"))
        int exp = Integer.parseInt(request.getParameter("exp"))

        if (dao.findById(id) == null) {
            return null
        }

        RoleBean rb = dao.findRole(id)
        server.close(id)

        rb.setVip(level)
        rb.setVipexp(exp)
        gmOperBeanRepository.save(new GmOperBean(2, rb.getNickname() + ": 设置vip等级:" + level + " vip经验:" + exp))

        return "当前VIP等级修改为:" + level + "级，exp:" + exp
    }


    @ApiOperation(value = "封禁角色")
	@RequestMapping(value = "/role/forbidRole", method = RequestMethod.GET)
	@ApiImplicitParams([
        @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
        @ApiImplicitParam(name = "val", value = "value", required = true, dataType = "int", paramType = "query")
    ])
	@ResponseBody String forbidRole(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))
            int val = Integer.parseInt(request.getParameter("val"))
		if (dao.findById(id) == null) {
			return null
		}

		RoleBean rb = dao.findRole(id)
		server.close(id)

		rb.setPlaytime(val)
		gmOperBeanRepository.save(new GmOperBean(2, rb.getNickname() + ": 禁止登录."))

		return "成功"
	}

	@ApiOperation(value = "封禁角色发言")
	@RequestMapping(value = "/role/forbidSay", method = RequestMethod.GET)
	@ApiImplicitParams([
        @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
        @ApiImplicitParam(name = "val", value = "value", required = true, dataType = "int", paramType = "query")
    ])
    @ResponseBody String forbidSay(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))
        int val = Integer.parseInt(request.getParameter("val"))
		if (dao.findById(id) == null) {
			return null
		}

		RoleBean rb = dao.findRole(id)
		server.close(id)

		rb.setSaytime(val)
		gmOperBeanRepository.save(new GmOperBean(2, rb.getNickname() + ": 封禁聊天."))

		return "成功"
	}

    @ApiOperation(value = "模拟充值")
    @RequestMapping(value = "/role/charge", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "rmb", value = "钱（元）", required = true, dataType = "int", paramType = "query")
    ])
    @ResponseBody String fakeCharge(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))
        int rmb = Integer.parseInt(request.getParameter("rmb"))

        if (dao.findById(id) == null) {
            return null
        }

        ChargeBean cb = dao.findCharge(id)
        if (cb == null) {
            return "帐号不存在"
        }

        server.close(id)

        RoleBean rb = dao.findRole(id)

        ReceiptBean receiptBean = new ReceiptBean()

        receiptBean.tid = "ADMIN_" + RandomUtil.getRandom() + "_" + System.currentTimeMillis()
        receiptBean.uid = id
        receiptBean.zone = GameServer.getInstance().getZone()
        receiptBean.time = new Date()
        receiptBean.source = "admin"
        receiptBean.sourceUser = ""
        receiptBean.good = rmb + ""
        receiptBean.rmb = rmb
        receiptBean.channel = 999
        receiptBean.bundle = ""
        receiptBean.status = 0
        receiptBean.sandbox = 1

        // 保存到数据库之后，立刻发送给玩家
        if (dao.saveReceipt(receiptBean) == -1) {
            return "充值错误."
        }

        gmOperBeanRepository.save(new GmOperBean(1, String.format("[%s](%d) 获得充值(%s)元", rb.getNickname(), rb.getId(), rmb/100)))
        return "充值成功:" + rmb/100 + "元."
    }


//    @ApiOperation(value = "查看战场英雄")
//    @RequestMapping(value = "/role/heros", method = RequestMethod.GET)
//    @ApiImplicitParams([
//            @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query")
//    ])
//    @ResponseBody HerosStruct heros(HttpServletRequest request) {
//        long id = Long.parseLong(request.getParameter("id"))
//
//        if (dao.findById(id) == null) {
//            return null
//        }
//
//        HerosBean hsb = dao.findHeros(id)
//
//        return hsb.createHerosStruct()
//    }

    @ApiOperation(value = "查看背包")
	@RequestMapping(value = "/role/packs", method = RequestMethod.GET)
	@ApiImplicitParams([
            @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query")
    ])
	@ResponseBody PackInfo packs(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))

		if (dao.findById(id) == null) {
			return null
		}

		PackInfo hi = new PackInfo()
		PackBean pb = dao.findPack(id)

		pb.getHeros().forEach({ k, hb ->
			if (hb != null) {
				hi.heros.add(hb)
			}
		})

		pb.getEquips().forEach({ k, hb ->
			if (hb != null) {
				hi.equips.add(hb)
			}
		})

		hi.items.putAll(pb.getItems())

		return hi
	}

    @ApiOperation(value = "修改玩家角色的创建时间")
    @RequestMapping(value = "/role/setCreateTime", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "date", value = "时间", required = true, dataType = "long", paramType = "query"),
    ])
    @ResponseBody String setCreateTime(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))
        long date = Long.parseLong(request.getParameter("date"))

        if (dao.findById(id) == null) {
            return null
        }
        server.close(id)

        RoleBean rb = dao.findRole(id)
        rb.setCreatetime(new Date(date))
        gmOperBeanRepository.save(new GmOperBean(2, rb.getNickname() + ": 修改创建时间."))

        return "成功"
    }

    @ApiOperation(value = "调整玩家的副本关卡")
    @RequestMapping(value = "/role/setScene", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "scene", value = "场景", required = true, dataType = "int", paramType = "query"),
    ])
    @ResponseBody String setScene(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))
        int scene = Integer.parseInt(request.getParameter("scene"))

        if (dao.findById(id) == null) {
            return null
        }
        server.close(id)

        SceneBean sb = dao.findScene(id)
        sb.setScene(scene)

        gmOperBeanRepository.save(new GmOperBean(3, "给玩家(" + id + ") 设置关卡(" + scene+ ")"))

        return "当前场景设定为:" + scene
    }


//    @ApiOperation(value = "获取通关最高的玩家信息")
//    @RequestMapping(value = "/role/getTopScene", method = RequestMethod.GET)
//    @ApiImplicitParams([
//            @ApiImplicitParam(name = "mode", value = "模式", required = true, dataType = "int", paramType = "query"),
//    ])
//    @ResponseBody List<NameValue> getTopScene(HttpServletRequest request) {
//        int mode = Integer.parseInt(request.getParameter("mode"))
//        return openRankService.getRanks(mode)
//    }

}
