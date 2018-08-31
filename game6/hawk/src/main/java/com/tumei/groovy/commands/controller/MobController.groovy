package com.tumei.controller

import com.google.common.base.Strings
import com.tumei.MobService
import com.tumei.centermodel.MobBean
import com.tumei.centermodel.UserBean
import com.tumei.centermodel.UserBeanRepository
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.apache.catalina.servlet4preview.http.HttpServletRequest
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import java.text.SimpleDateFormat
import java.time.Instant

/**
 * Created by Administrator on 2016/12/16 0016.
 *
 * 广告服务访问的接口
 */
class MobController {
    private static final Log log = LogFactory.getLog(this.class)

    @Autowired
    private MobService mobService
    @Autowired
    @Qualifier(value = "centerTemplate")
    private MongoTemplate mongoTemplate

    @Autowired
    private UserBeanRepository userBeanRepository


    /**
     * 获取请求的真实ip地址
     * @param request
     * @return
     */
    private String getRealAddr(javax.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For") //  Squid 代理
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP") // apache 代理
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP") // weblogic 代理
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT-IP") // other 代理
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X_Real_IP") // nginx 代理
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (!Strings.isNullOrEmpty(ip)) {
            ip = ip.split(",")[0]
        }

        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr()
        }
        return ip
    }

    @ApiOperation(value = "收集广告点击详情")
    @RequestMapping(value = "/ad/mobcastle", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "clickid", value = "唯一标识", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "deviceid", value = "设备标识", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "ip", value = "网络地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "os_v", value = "系统版本", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "s1", value = "广告位id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "callback", value = "确认回调地址", required = true, dataType = "String", paramType = "query"),
    ])
    String mobcastle(HttpServletRequest request) {
        try {
            String clickid = request.getParameter("clickid")
            String deviceid = "" //request.getParameter("deviceid")
            String ip = getRealAddr(request)
//            String os = request.getHeader("User-Agent")
            String s1 = "" //request.getParameter("s1")
            String callback = request.getParameter("callback")

            mobService.addClick(clickid, s1, deviceid, ip, callback);
        } catch (Exception ex) {
            log.error("点击错误:" + ex.getMessage())
        }

        return "redirect:https://itunes.apple.com/cn/app/%E5%BF%8D%E8%80%85%E4%BC%A0%E5%A5%87/id1343081267?mt=8"
    }

    @ApiOperation(value = "查看点击分析")
    @RequestMapping(value = "/ad/result", method = RequestMethod.GET)
    @ApiImplicitParams([
    ])
    @ResponseBody
    String result(HttpServletRequest request) {
        return mobService.analyse()
    }

    @ApiOperation(value = "查看最新的几条数据内容")
    @RequestMapping(value = "/ad/view", method = RequestMethod.GET)
    @ApiImplicitParams([
    ])
    @ResponseBody
    String view(HttpServletRequest request) {
        return mobService.view()
    }

    @ApiOperation(value = "查看指定时间的收入")
    @RequestMapping(value = "/ad/queryincome", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "date", value = "日期(20141111)", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    String queryincome(HttpServletRequest request) {
        String date = request.getParameter("date")

        int total = 0
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            Date s = sdf.parse(date)
            Date e = Date.from(Instant.ofEpochMilli(s.getTime() + (24*3600_000)))
            Criteria criteria = Criteria.where("ts").lt(e).gte(s)
            criteria.and("account").gt(1L)

            List<MobBean> rbs = mongoTemplate.find(new Query(criteria), MobBean.class);
            for (MobBean rb : rbs) {
                UserBean ub = userBeanRepository.findById(rb.account)
                if (ub != null) {
                    total += ub.charge
                }
            }
        } catch (Exception e) {
            log.error("查询广告带来的玩家收入错误:" + e.getMessage());
        }

        return String.format("当日广告带来的玩家到目前为止的总收入:" + total);
    }


    @ApiOperation(value = "查看指定时间的点击转化率")
    @RequestMapping(value = "/ad/queryeffect", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "date", value = "日期(20141111)", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    String queryeffect(HttpServletRequest request) {
        String date = request.getParameter("date")

        float clicks = 0
        float gotcha = 0
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            Date s = sdf.parse(date)
            Date e = Date.from(Instant.ofEpochMilli(s.getTime() + (24*3600_000)))
            Criteria criteria = Criteria.where("ts").lt(e).gte(s)
//            criteria.and("account").gt(1L)

            List<MobBean> rbs = mongoTemplate.find(new Query(criteria), MobBean.class);
            for (MobBean rb : rbs) {
                if (rb.account != 0) {
                    gotcha += 1
                }
            }
            clicks = rbs.size()
        } catch (Exception e) {
            log.error("查询广告转化率错误:" + e.getMessage());
        }

        return String.format("当日广告点击数(%f), 转化注册数(%f), 效率(%f)", clicks, gotcha, gotcha/clicks);
    }
}

