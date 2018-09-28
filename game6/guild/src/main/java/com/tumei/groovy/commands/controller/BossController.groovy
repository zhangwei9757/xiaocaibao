package com.tumei.groovy.commands.controller

import com.tumei.common.fight.HerosStruct
import com.tumei.common.utils.JsonUtil
import com.tumei.common.webio.BattleResultStruct

import com.tumei.dto.boss.BossDto
import com.tumei.dto.boss.BossGuildDto
import com.tumei.dto.boss.BossRoleDto
import com.tumei.groovy.contract.IBossSystem
import com.tumei.modelconf.Readonly
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
 * Created by Leon on 2018/2/5.
 *
 * boss战的输入输出
 */
class BossController {
    static final Log log = LogFactory.getLog(BossController.class)

    @Autowired
    private IBossSystem bossSystem
    
    @Autowired
    private Readonly readonly

    @Autowired
    private ApplicationContext ctx;

    @ApiOperation(value = "刷新配置")
    @RequestMapping(value = "/boss/flush", method = RequestMethod.GET)
    @ResponseBody
    String flush() {
        readonly.refresh()
        return "刷新成功"
    }

    @ApiOperation(value = "结算boss")
    @RequestMapping(value = "/boss/conclusion", method = RequestMethod.GET)
    @ResponseBody
    String conclusion() {
        log.info("--- 开始结算boss战 ---")
        bossSystem.conclusion()
        log.info("--- 结束结算boss战 ---")

        return "Boss结算."
    }

    @ApiOperation(value = "查看当前boss血量")
    @RequestMapping(value = "/boss/bossLife", method = RequestMethod.GET)
    @ResponseBody
    String bossLife() {
        return bossSystem.bossLife()
    }

    @ApiOperation(value = "测试攻击boss")
    @RequestMapping(value = "/boss/callFight", method = RequestMethod.POST)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "bs", value = "战斗数据", required = true, dataType = "HerosStruct", paramType = "parameter"),
    ])
    @ResponseBody
    BattleResultStruct bossTestFight(HttpServletRequest request) {
        // 获取BattleStruct 从body中
        String line = null;
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader br = request.getReader();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            HerosStruct bs = JsonUtil.Unmarshal(sb.toString(), HerosStruct.class)
            return bossSystem.callFight(bs)
        } catch (Exception ex) {
            log.error("战斗错误:" + ex.message)
        }
        return null;
    }


    @ApiOperation(value = "获取排行榜")
    @RequestMapping(value = "/boss/rank", method = RequestMethod.GET)
    @ResponseBody
    List<BossRoleDto> bossRank(HttpServletRequest request) {
        return bossSystem.getRanks()
    }

    @ApiOperation(value = "获取公会排行榜")
    @RequestMapping(value = "/boss/guildrank", method = RequestMethod.GET)
    @ResponseBody
    List<BossGuildDto> bossGuildRank(HttpServletRequest request) {
        return bossSystem.getGuildRanks()
    }

    @ApiOperation(value = "提交当前角色信息, 第一次进入boss提交")
    @RequestMapping(value = "/boss/getInfo", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "玩家ID", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "昵称", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    BossDto getInfo(HttpServletRequest request) {
        long uid = Long.parseLong(request.getParameter("uid"))
        String name = request.getParameter("name")

//        IBossSystem bs = ctx.getBean(IBossSystem.class)
        return bossSystem.getInfo(uid, name)
    }

    @ApiOperation(value = "获取当前boss的血量")
    @RequestMapping(value = "/boss/getLife", method = RequestMethod.GET)
    @ResponseBody
    Long getLife(HttpServletRequest request) {
        return bossSystem.getLife();
    }

    @ApiOperation(value = "打开关停boss")
    @RequestMapping(value = "/boss/enabled", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "flag", value = "开关", required = true, dataType = "boolean", paramType = "query"),
    ])
    @ResponseBody
    String enabled(HttpServletRequest request) {
        Boolean flag = Boolean.parseBoolean(request.getParameter("flag"))

        return bossSystem.enable(flag)
    }
}
