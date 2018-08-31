package com.tumei.controller

import com.google.common.base.Strings
import com.tumei.common.fight.*
import com.tumei.common.utils.JsonUtil
import com.tumei.groovy.GroovyLoader
import com.tumei.groovy.contract.IFightSystem
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
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by leon on 2016/12/30.
 * <p>
 * 给游戏服务器提供的接口:
 */
class SimfightController {
    private Log log = LogFactory.getLog(SimfightController.class)

    @Autowired
    private ApplicationContext ctx

    @Autowired
    private GroovyLoader groovyLoader

    @Autowired
    private Readonly readonly

    @ApiOperation(value = "刷新配置")
    @RequestMapping(value = "/flush", method = RequestMethod.GET)
    public
    @ResponseBody
    String flush() {
        readonly.refresh()
        return "刷新成功"
    }

    private AtomicInteger countSim = new AtomicInteger()
    private AtomicInteger countSimSelf = new AtomicInteger()
    private AtomicInteger countSimScene = new AtomicInteger()
    private AtomicInteger countSimGroup = new AtomicInteger()
    private AtomicInteger countCalcPower = new AtomicInteger()

    void debug() {
        String msg = String.format("++ 计算战斗力次数: %d \n" +
                "++ 模拟玩家之间战斗: %d \n" +
                "++ 模拟副本战斗: %d\n" +
                "++ 模拟公会副本战斗次数: %d\n" +
                "++ 测试模拟: %d\n",
                countCalcPower.intValue(),
                countSim.intValue(),
                countSimScene.intValue(),
                countSimGroup.intValue(),
                countSimSelf.intValue()
        )
        log.info(msg)
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
            return null
        }
    }

    @ApiOperation(value = "输入战斗服务器状态")
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    void status() {
        debug()
    }

    @ApiOperation(value = "测试玩家互相战斗返回")
    @RequestMapping(value = "/simSelf", method = RequestMethod.POST)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "data", value = "战斗数据", required = true, dataType = "FightStruct", paramType = "parameter")])
    @ResponseBody
    String simSelf(HttpServletRequest request) {
        String s = getBody(request)
        if (s == null) {
            return "无法获取参数"
        }
        FightStruct data = JsonUtil.Unmarshal(s, FightStruct.class)

        if (countSimSelf.incrementAndGet() % 100 == 0) {
            debug()
        }

        FightResult sfr = new FightResult()
        try {
            IFightSystem sb = ctx.getBean(IFightSystem.class)
            sb.buildTeam(1, data)

            return sb.debugLeft()
        } catch (Exception ex) {
            log.error("战斗错误: " + ex.getMessage(), ex)
            ex.printStackTrace()
            sfr.win = -1
            sfr.data = "战斗发生错误:" + ex.getMessage()
        }
        return "错误"
    }


    @ApiOperation(value = "玩家互相战斗返回")
    @RequestMapping(value = "/sim", method = RequestMethod.POST)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "data", value = "战斗数据", required = true, dataType = "FightStruct", paramType = "parameter")])
    @ResponseBody
    FightResult sim(HttpServletRequest request) {
        FightResult sfr = new FightResult()

        String s = getBody(request)
        if (Strings.isNullOrEmpty(s)) {
            sfr.data = "参数无法获取"
        } else {
            try {
                FightStruct data = JsonUtil.Unmarshal(s, FightStruct.class)

                IFightSystem sb = ctx.getBean(IFightSystem.class)

                sb.buildTeam(1, data)
                sb.buildTeam(2, data)

                if (data.getLeft().size() <= 0 || data.getRight().size() <= 0) {
                    println("sim self troop null:" + s)
                    sfr.win = -1
                } else {
                    sfr.win = sb.run()
                    sfr.data = sb.getFightData()
                }

                if (countSim.incrementAndGet() % 100 == 0) {
                    debug()
                }
                return sfr
            } catch (Exception ex) {
                log.error("战斗错误: " + ex.getMessage(), ex)
                ex.printStackTrace()
                sfr.data = "战斗发生错误:" + ex.getMessage()
            }

        }

        sfr.win = -1
        return sfr
    }

    @ApiOperation(value = "关卡战斗模拟")
    @RequestMapping(value = "/simScene", method = RequestMethod.POST)
    @ApiImplicitParams([@ApiImplicitParam(name = "data", value = "战斗数据", required = true, dataType = "SceneFightStruct", paramType = "parameter")])
    @ResponseBody
    FightResult simScene(HttpServletRequest request) {
        if (countSimScene.incrementAndGet() % 100 == 0) {
            debug()
        }

        FightResult sfr = new FightResult()

        String s = getBody(request)
        if (Strings.isNullOrEmpty(s)) {
            sfr.data = "参数无法获取"
        } else {
            try {
                SceneFightStruct data = JsonUtil.Unmarshal(s, SceneFightStruct.class)
                IFightSystem sb = ctx.getBean(IFightSystem.class)

                sb.buildSceneTeam(1, data)
                sb.buildTeamByStruct(2, data.getRight())

                if (data.getLeft().size() <= 0 || data.getRight().size() <= 0) {
                    println("scene self troop null:" + s)
                    sfr.win = -1
                } else {
                    sfr.win = sb.run()
                    sfr.data = sb.getFightData()
                }

                return sfr
            } catch (Exception ex) {
                log.error(String.format("simScene战斗错误: %s", ex.getMessage()), ex)
                for (StackTraceElement ste : ex.stackTrace) {
                    println("${ste.className}: " + ste.toString())
                }
                sfr.data = "战斗发生错误:" + ex.getMessage()
            }
        }

        sfr.win = -1
        return sfr
    }

    @ApiOperation(value = "公会副本战斗模拟")
    @RequestMapping(value = "/simGroup", method = RequestMethod.POST)
    @ApiImplicitParams([@ApiImplicitParam(name = "data", value = "战斗数据", required = true, dataType = "GroupFightStruct", paramType = "parameter")])
    @ResponseBody
    GroupFightResult simGroup(HttpServletRequest request) {
        if (countSimGroup.incrementAndGet() % 100 == 0) {
            debug()
        }

        GroupFightResult sfr = new GroupFightResult()

        String s = getBody(request)
        if (Strings.isNullOrEmpty(s)) {
            sfr.data = "参数无法获取"
        } else {
            try {
                GroupFightStruct data = JsonUtil.Unmarshal(s, GroupFightStruct.class) as GroupFightStruct
                if (data.getLeft().size() <= 0 || data.getRight().size() <= 0) {
                    sfr.win = -1
                } else {
                    IFightSystem sb = ctx.getBean(IFightSystem.class)

                    sb.buildGroupTeam(1, data)
                    sb.buildTeamByStruct(2, data.getRight())
                    sfr.win = sb.run()
                    sfr.data = sb.getFightData()
                    sfr.lifes = sb.getRightLifes()
                }
                return sfr
            } catch (Exception ex) {
                log.error("simScene战斗错误: " + ex.getMessage(), ex)
                ex.printStackTrace()
                sfr.data = "战斗发生错误:" + ex.getMessage()
            }
        }

        sfr.win = -1
        return sfr
    }

    @ApiOperation(value = "计算给定的队伍的战斗力")
    @RequestMapping(value = "/calcPower", method = RequestMethod.POST)
    @ApiImplicitParams([@ApiImplicitParam(name = "data", value = "战斗数据", required = true, dataType = "PowerStruct", paramType = "parameter")])
    @ResponseBody
    long calcPower(HttpServletRequest request) {
        if (countCalcPower.incrementAndGet() % 100 == 0) {
            debug()
        }

        String s = getBody(request)
        if (Strings.isNullOrEmpty(s)) {
            return 0
        }

        try {
            PowerStruct data = JsonUtil.Unmarshal(s, PowerStruct.class) as PowerStruct
            IFightSystem sb = ctx.getBean(IFightSystem.class)

            return sb.calcPower(data)
        } catch (Exception ex) {
            log.error("计算发生错误: " + ex.getMessage(), ex)
        }

        return 0
    }
}