package com.tumei.groovy.commands.controller

import com.google.common.base.Strings
import com.tumei.common.utils.JsonUtil
import com.tumei.dto.arena.*
import com.tumei.groovy.contract.IArenaSystem
import com.tumei.common.Readonly
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
 * Created by Leon on 2017/11/6 0006.
 */
class ArenaController {
    static final Log log = LogFactory.getLog(ArenaController.class)

    @Autowired
    private ApplicationContext ctx

    @Autowired
    private Readonly readonly

    @Autowired
    private IArenaSystem arenaSystem


    @ApiOperation(value = "刷新配置")
    @RequestMapping(value = "/flush", method = RequestMethod.GET)
    @ResponseBody
    String flush() {
        readonly.refresh()
        return "刷新成功"
    }

    @ApiOperation(value = "强制结算天梯")
    @RequestMapping(value = "/ladderOver", method = RequestMethod.GET)
    @ResponseBody
    String LadderOver() {
        arenaSystem.schedule()
        return "天梯赛结束"
    }

    @ApiOperation(value = "强制发送每天奖励")
    @RequestMapping(value = "/ladderDayAward", method = RequestMethod.GET)
    @ResponseBody
    String LadderDayAward() {
        arenaSystem.arenaSchedule()
        return "发送天梯赛日奖励"
    }

    void debug() {
        log.info("信息没有查询到:")
    }

    static String getBody(HttpServletRequest request) {
        try {
            StringBuilder sb = new StringBuilder()
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))
            String line
            while ((line = reader.readLine()) != null) {
                sb.append(line)
            }
            return sb.toString()
        } catch (Exception ex) {

        }
        return null
    }

    @ApiOperation(value = "输入战斗服务器状态")
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    void status() {
        debug()
    }

    @ApiOperation(value = "提交玩家信息")
    @RequestMapping(value = "/submitInfo", method = RequestMethod.POST)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "data", value = "玩家信息", required = true, dataType = "String", paramType = "parameter")])
    @ResponseBody
    String submitInfo(HttpServletRequest request) {
        String body = getBody(request)
        if (body == null) {
            return "无法获取参数"
        }

        ArenaRoleDto data = JsonUtil.Unmarshal(body, ArenaRoleDto.class)
        if (data == null) {
            return "无法获取参数"
        }

        // 刷新当前玩家信息，如果玩家不存在，则载入数据
        IArenaSystem ias = ctx.getBean(IArenaSystem.class)
        ias.submitInfo(data)

        return null
    }

    @ApiOperation(value = "玩家进入竞技场获取信息")
    @RequestMapping(value = "/getInfo", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "玩家ID", required = true, dataType = "long", paramType = "query")])
    @ResponseBody
    ArenaInfo getInfo(HttpServletRequest request) {
        String uidStr = request.getParameter("uid")
        if (Strings.isNullOrEmpty(uidStr)) {
            return null
        }

        long uid = Long.parseLong(uidStr)

        // 刷新当前玩家信息，如果玩家不存在，则载入数据
        IArenaSystem ias = ctx.getBean(IArenaSystem.class)
        return ias.getInfo(uid)
    }

    @ApiOperation(value = "挑战")
    @RequestMapping(value = "/fight", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "玩家ID", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "peer", value = "对方的排名,不受玩家发生变动影响", required = true, dataType = "int", paramType = "query"),
    ])
    @ResponseBody
    ArenaFightResult fight(HttpServletRequest request) {
        String uidStr = request.getParameter("uid")
        String peerStr = request.getParameter("peer")
        long uid = Long.parseLong(uidStr)
        int peer = Integer.parseInt(peerStr)

        // 刷新当前玩家信息，如果玩家不存在，则载入数据
        return arenaSystem.fight(uid, peer)
    }


    @ApiOperation(value = "进入天体赛")
    @RequestMapping(value = "/ladder", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "玩家ID", required = true, dataType = "long", paramType = "query"),
    ])
    @ResponseBody
    LadderInfoDto ladder(HttpServletRequest request) {
        String uidStr = request.getParameter("uid")
        long uid = Long.parseLong(uidStr)

        // 刷新当前玩家信息，如果玩家不存在，则载入数据
        return arenaSystem.enterLadder(uid)
    }

    @ApiOperation(value = "选择slot分组")
    @RequestMapping(value = "/chooseSlot", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "玩家ID", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "slot", value = "分组(0,1)", required = true, dataType = "int", paramType = "query"),
    ])
    @ResponseBody
    int chooseSlot(HttpServletRequest request) {
        String uidStr = request.getParameter("uid")
        long uid = Long.parseLong(uidStr)
        String slotStr = request.getParameter("slot")
        int slot = Integer.parseInt(slotStr)

        // 刷新当前玩家信息，如果玩家不存在，则载入数据
        return arenaSystem.chooseSlot(uid, slot)
    }

    @ApiOperation(value = "挑战天梯")
    @RequestMapping(value = "/fightLadder", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "玩家ID", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "pid", value = "对方ID", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "group", value = "分组", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "index", value = "分组序号", required = true, dataType = "int", paramType = "query"),
    ])
    @ResponseBody
    LadderFightResult fightLadder(HttpServletRequest request) {
        long uid = Long.parseLong(request.getParameter("uid"))
        long pid = Long.parseLong(request.getParameter("pid"))
        int group = Integer.parseInt(request.getParameter("group"))
        int index = Integer.parseInt(request.getParameter("index"))

        // 刷新当前玩家信息，如果玩家不存在，则载入数据
        return arenaSystem.fightLadder(uid, pid, group, index)
    }

    @ApiOperation(value = "获取天梯荣誉")
    @RequestMapping(value = "/getHonor", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "玩家ID", required = true, dataType = "long", paramType = "query"),
    ])
    @ResponseBody
    LadderHonorDto getHonor(HttpServletRequest request) {
        long uid = Long.parseLong(request.getParameter("uid"))

        // 刷新当前玩家信息，如果玩家不存在，则载入数据
        return arenaSystem.getHonor(uid)
    }

    @ApiOperation(value = "获取战败视频")
    @RequestMapping(value = "/getVideos", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "玩家ID", required = true, dataType = "long", paramType = "query"),
    ])
    @ResponseBody
    List<LadderHonorDto> getVideos(HttpServletRequest request) {
        long uid = Long.parseLong(request.getParameter("uid"))

        // 刷新当前玩家信息，如果玩家不存在，则载入数据
        return arenaSystem.getVideos(uid)
    }

}
