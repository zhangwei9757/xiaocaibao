package com.tumei.controller

import com.google.common.base.Strings
import com.tumei.centermodel.*
import com.tumei.centermodel.beans.ServerBean
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
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

import javax.servlet.http.HttpServletRequest
import java.text.SimpleDateFormat

/**
 * Created by Administrator on 2016/12/16 0016.
 * <p>
 * 内网服务访问的接口
 */
class CmdController {
    private Log log = LogFactory.getLog(this.class);

    @Autowired
    @Qualifier("centerTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    private AccountBeanRepository accountBeanRepository;

    @Autowired
    private Readonly readonly;

    @Autowired
    private RemoteService remoteService;

    @ApiOperation(value = "重新加载只读配置: 比如允许的包名")
    @RequestMapping(value = "/cmd/reload", method = RequestMethod.GET)
    @ResponseBody String reload() {
        readonly.reload();
        return readonly.debug();
    }

    class ServerInfo {
        public int zone;
        public String url;

        public ServerInfo() {}

        public ServerInfo(int _zone, String _url) {
            zone = _zone;
            url = _url;
        }
    }

    @Autowired
    private ServersBeanRepository serversBeanRepository;

    @ApiOperation(value = "查看网页支付活动id")
    @RequestMapping(value = "/cmd/infoWebBuff", method = RequestMethod.GET)
    @ResponseBody String infoWebBuff() {
        int val = 1000_00;
        log.info("valu:" + val);
        return readonly.getWebBuff()
    }


    @ApiOperation(value = "获取所有Game服务器列表")
    @RequestMapping(value = "/cmd/getGameServers", method = RequestMethod.GET)
    public @ResponseBody List<ServerInfo> getGameServers() {
        List<ServerInfo> servers = new ArrayList<>();
        List<ServersBean> ssbs = serversBeanRepository.findAll();
        ServersBean ssb;
        if (ssbs.size() != 0) {
            ssb = ssbs.get(0);
            for (ServerBean sb : ssb.getServers()) {
                servers.add(new ServerInfo(sb.id, sb.host));
            }
        }
        return servers;
    }

    @ApiOperation(value = "向xxkg服务器发送通知")
    @RequestMapping(value = "/cmd/sendNotify", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "zone", value = "服务器id(0标识所有)", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "msg", value = "消息", required = true, dataType = "String", paramType = "query"),
    ])
    public @ResponseBody String sendNotify(HttpServletRequest request) {
        int zone = Integer.parseInt(request.getParameter("zone"))
        String msg = request.getParameter("msg")

        if (zone == 0) {
            zone = -2//serversBeanRepository.count()
        }
        return remoteService.sendNotifyMsg(zone, msg);
    }

    @ApiOperation(value = "根据玩家的id获取玩家信息，包括帐号名，密码")
    @RequestMapping(value = "/cmd/getInfoByUserId", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "不包含服务器区号的id", required = true, dataType = "long", paramType = "query")
    ])
    public @ResponseBody String getInfoByUserId(HttpServletRequest request) {
        long uid = Long.parseLong(request.getParameter("uid"));
        AccountBean accountBean = accountBeanRepository.findById(uid);
        if (accountBean != null) {
            return "帐号(" + accountBean.getAccount() + ") 密码(" + accountBean.getPasswd() + ") 权限(" + accountBean.getRole() + ").";
        }
        return "该帐号不存在";
    }

    @ApiOperation(value = "根据玩家的id 修改密码")
    @RequestMapping(value = "/cmd/modifyPassword", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "不包含服务器区号的id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", paramType = "query")
    ])
    public @ResponseBody String modifyPassword(HttpServletRequest request) {
        long uid = Long.parseLong(request.getParameter("uid"))
        String password = request.getParameter("password")
        AccountBean accountBean = accountBeanRepository.findById(uid);
        if (accountBean != null) {
            accountBean.setPasswd(password);
            accountBeanRepository.save(accountBean);

            return "修改成功, 帐号(" + accountBean.getAccount() + ") 密码(" + accountBean.getPasswd() + ") 权限(" + accountBean.getRole() + ").";
        }
        return "该帐号不存在";
    }

    @ApiOperation(value = "根据玩家的id 修改玩家权限")
    @RequestMapping(value = "/cmd/modifyAdmin", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "不包含服务器区号的id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "admin", value = "权限(USER,ADMIN),注意英文逗号分割，普通权限则只保留USER", required = true, dataType = "String", paramType = "query")
    ])
    public @ResponseBody String modifyAdmin(HttpServletRequest request) {
        long uid = Long.parseLong(request.getParameter("uid"))
        String admin = request.getParameter("admin")
        AccountBean accountBean = accountBeanRepository.findById(uid);
        if (accountBean != null) {
            accountBean.setRole(admin);
            accountBeanRepository.save(accountBean);

            return "修改成功, 帐号(" + accountBean.getAccount() + ") 密码(" + accountBean.getPasswd() + ") 权限(" + accountBean.getRole() + ").";
        }
        return "该帐号不存在";
    }


    @ApiOperation(value = "获取指定日期的收入")
    @RequestMapping(value = "/cmd/getIncome", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "begin", value = "开始日期(包含)格式：[2017-9-1]", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束日期(不包含)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "sandbox", value = "0:正式,1:测试", required = true, defaultValue = "0", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "zone", value = "服务器区号(0表示所有)", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "source", value = "渠道(空为全部)", required = false, dataType = "String", paramType = "query")
    ])
    public @ResponseBody int getIncome(HttpServletRequest request) {
        String begin = request.getParameter("begin")
        String end = request.getParameter("end")
        int sandbox = Integer.parseInt(request.getParameter("sandbox"))
        int zone = Integer.parseInt(request.getParameter("zone"))
        String source = request.getParameter("source")

        int total = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date s = sdf.parse(begin);
            Date e = sdf.parse(end);

            if (zone < 0) {
                zone = 0
            }

            Criteria criteria = Criteria.where("time").lt(e).gte(s);
            criteria.and("sandbox").is(sandbox);

            if (zone != 0) {
                criteria.and("zone").is(zone);
            }
            if (!Strings.isNullOrEmpty(source)) {
                criteria.and("source").is(source);
            }

            List<ReceiptBean> rbs = mongoTemplate.find(new Query(criteria), ReceiptBean.class);
            for (ReceiptBean rb : rbs) {
                total += rb.rmb;
            }
        } catch (Exception e) {
            log.error("传入的参数错误，无法解析:" + e.getMessage());
        }

        return total;
    }

    @ApiOperation(value = "获取指定日期的新玩家")
    @RequestMapping(value = "/cmd/getNewUsers", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "begin", value = "开始日期(包含)格式：[2017-9-1]", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束日期(不包含)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "source", value = "渠道(空为全部)", required = false, dataType = "String", paramType = "query")
    ])
    public @ResponseBody long getNewUsers(HttpServletRequest request) {
        String begin = request.getParameter("begin")
        String end = request.getParameter("end")
        String source = request.getParameter("source")

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date s = sdf.parse(begin);
            Date e = sdf.parse(end);

            Criteria criteria = Criteria.where("createtime").lt(e).gte(s);
            if (!Strings.isNullOrEmpty(source)) {
                criteria.and("source").is(source);
            }

            return mongoTemplate.count(new Query(criteria), AccountBean.class);
        } catch (Exception e) {
            log.error("传入的参数错误，无法解析:" + e.getMessage());
        }

        return -1;
    }
}
