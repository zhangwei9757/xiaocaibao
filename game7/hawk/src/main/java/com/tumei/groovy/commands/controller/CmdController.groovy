package com.tumei.controller

import com.google.common.base.Strings
import com.tumei.centermodel.*
import com.tumei.centermodel.beans.UserRoleStruct
import com.tumei.common.DaoCenter
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.common.utils.JsonUtil
import com.tumei.groovy.GroovyLoader
import com.tumei.modelconf.HeroConf
import com.tumei.modelconf.ItemConf
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.scheduling.annotation.Async
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Created by Administrator on 2016/12/16 0016.
 * <p>
 * 内网服务访问的接口
 *
 * 部分接口只要admin权限
 * 部分接口还需要owern权限
 *
 */
class CmdController {
    private static final Log log = LogFactory.getLog(CmdController.class)

    @Autowired
    private DaoCenter dao

    @Autowired
    @Qualifier("centerTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier("confTemplate")
    private MongoTemplate confTemplate;

    @Autowired
    private AccountBeanRepository accountBeanRepository;

    @Autowired
    private Readonly readonly;

    @Autowired
    private RemoteService remoteService;

    @Autowired
    private GroovyLoader groovyLoader;

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


    @ApiOperation(value = "重新加载只读配置: 比如允许的包名")
    @RequestMapping(value = "/cmd/reload", method = RequestMethod.GET)
    @ResponseBody String reload() {
        readonly.reload();
        return readonly.debug();
    }

    @Autowired
    private ServerBeanRepository serverBeanRepository;

    @ApiOperation(value = "查看网页支付活动id")
    @RequestMapping(value = "/cmd/infoWebBuff", method = RequestMethod.GET)
    @ResponseBody String infoWebBuff() {
        int val = 1000_00;
        log.info("value:" + val);
        return readonly.getWebBuff()
    }

    @ApiOperation(value = "向xxkg服务器发送通知")
    @RequestMapping(value = "/cmd/sendNotify", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "zone", value = "服务器id(0标识所有)", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "msg", value = "消息", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody String sendNotify(HttpServletRequest request) {
        int zone = Integer.parseInt(request.getParameter("zone"))
        String msg = request.getParameter("msg")

        if (zone == 0) { // 如果传递0,查询一共有多少个服务器,然后通知,写成负数,发送的时候就会发送所有这些服务器了!
            zone = -serverBeanRepository.count();
        }
        remoteService.sendNotifyMsg(zone, msg);
        return "发送结束"
    }

    @ApiOperation(value = "根据玩家的id获取玩家信息，包括帐号名，密码")
    @RequestMapping(value = "/cmd/getInfoByUserId", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "不包含服务器区号的id", required = true, dataType = "long", paramType = "query")
    ])
    @ResponseBody String getInfoByUserId(HttpServletRequest request) {
        long uid = Long.parseLong(request.getParameter("uid"));
        AccountBean accountBean = accountBeanRepository.findById(uid);
        if (accountBean != null) {
            return "帐号(" + accountBean.getAccount() + ") 密码(" + accountBean.getPasswd() + ") 权限(" + accountBean.getRole() + ")."
        }
        return "该帐号不存在"
    }

    @ApiOperation(value = "根据id查询玩家")
    @RequestMapping(value = "/cmd/infoAccount", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "不包含服务器区号的id", required = true, dataType = "String", paramType = "query")
    ])
    @ResponseBody AccountBean infoAccount(HttpServletRequest request) {
        String acc = request.getParameter("uid")
        try {
            long uid = Long.parseLong(acc)
            return accountBeanRepository.findById(uid)
        } catch (Exception ex) {

        }

        return accountBeanRepository.findByAccount(acc)
    }

    @ApiOperation(value = "根据玩家的id 查询到玩家之后绑定到另外一个id")
    @RequestMapping(value = "/cmd/bindAccount", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "acc", value = "account", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "oid", value = "另外一个id", required = true, dataType = "long", paramType = "query"),
    ])
    @ResponseBody String bindAccount(HttpServletRequest request) {
        String acc = request.getParameter("acc")
        long oid = Long.parseLong(request.getParameter("oid"))

        AccountBean accountBean = accountBeanRepository.findByAccount(acc);
        if (accountBean != null) {
            accountBean.setId(oid)
            accountBeanRepository.save(accountBean);

            return "绑定成功"
        }
        return "该帐号不存在";
    }

    @ApiOperation(value = "所有服务器")
    @RequestMapping(value = "/cmd/getServers", method = RequestMethod.GET)
    @ApiImplicitParams([
    ])
    @ResponseBody List<ServerBean> getServers(HttpServletRequest request) {
        return serverBeanRepository.findAll()
    }

    @ApiOperation(value = "调整服务器,包括开服")
    @RequestMapping(value = "/cmd/saveServer", method = RequestMethod.POST)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "server", value = "服务器信息", required = true, dataType = "ServerBean", paramType = "parameter"),
    ])
    @ResponseBody String saveServer(HttpServletRequest request) {
        String body = getBody(request)
        if (body == null) {
            return "无法获取参数"
        }

        ServerBean data = JsonUtil.Unmarshal(body, ServerBean.class)
        if (data == null) {
            return "无法获取参数"
        }

        if (Strings.isNullOrEmpty(data.objectId)) {
            // 新服务器

        }

        serverBeanRepository.save(data)
        return "成功"
    }

    @ApiOperation(value = "删除服务器")
    @RequestMapping(value = "/cmd/delServer", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "oid", value = "服务器信息", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody String delServer(HttpServletRequest request) {
        String oid = request.getParameter("oid")

        serverBeanRepository.delete(oid)
        return "成功"
    }

    @ApiOperation(value = "是否热更新状态")
    @RequestMapping(value = "/isReloadable", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "mode", value = "状态:0 读取, 1 设置相反的值", required = true, dataType = "int", paramType = "query"),
    ])
    boolean isReloadable(HttpServletRequest request) {
        int mode = Integer.parseInt(request.getParameter("mode"))
        boolean rtn = groovyLoader.isReload();
        if (mode == 0) {
            return rtn;
        }

        rtn = !rtn;
        groovyLoader.setReload(rtn);
        return rtn;
    }

    @ApiOperation(value = "guild是否热更新状态")
    @RequestMapping(value = "/cmd/isGuildReloadable", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "mode", value = "状态:0 读取, 1 设置相反的值", required = true, dataType = "int", paramType = "query"),
    ])
    @ResponseBody boolean isGuildReloadable(HttpServletRequest request) {
        int mode = Integer.parseInt(request.getParameter("mode"))
        return remoteService.groupReloadable(mode)
    }

    @ApiOperation(value = "刷新guild")
    @RequestMapping(value = "/cmd/flushGuild", method = RequestMethod.GET)
    @ResponseBody String flushGuild(HttpServletRequest request) {
        return remoteService.flushGroup()
    }


    @ApiOperation(value = "根据玩家的id 修改密码")
    @RequestMapping(value = "/cmd/modifyPassword", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "不包含服务器区号的id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", paramType = "query")
    ])
    @ResponseBody String modifyPassword(HttpServletRequest request) {
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
    @ResponseBody String modifyAdmin(HttpServletRequest request) {
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
    @ResponseBody int getIncome(HttpServletRequest request) {
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

    @ApiOperation(value = "最近七日的收入")
    @RequestMapping(value = "/cmd/recentInfos", method = RequestMethod.GET)
    @ApiImplicitParams([
        @ApiImplicitParam(name = "date", value = "开始日期(包含)格式：[2017-9-1]", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody List<RecentIncome> recentInfos(HttpServletRequest request) {
        String dt = request.getParameter("date")
        LocalDate s = LocalDate.parse(dt, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        List<RecentIncome> rtn = new ArrayList<>()

        for (int i = 0; i < 15; ++i) {
            LocalDate ldt = s.plusDays(-i)
            int today = ldt.getYear() * 10000 + ldt.getMonthValue() * 100 + ldt.getDayOfMonth()
            Criteria criteria = Criteria.where("day").is(today)
            DailyStaBean dsb = mongoTemplate.findOne(new Query(criteria), DailyStaBean.class)

            if (dsb != null) {
                // 没有检测过留存的进行计算, 并保存
                if (i >= 0 && (!dsb.checked && dsb.nusers.size() > 0)) {
                    for (int j = 1; j <= 7; ++j) {
                        LocalDate l = ldt.plusDays(j);
                        today = l.getYear() * 10000 + l.getMonthValue() * 100 + l.getDayOfMonth()
                        criteria = Criteria.where("day").is(today)
                        DailyStaBean other = mongoTemplate.findOne(new Query(criteria), DailyStaBean.class)
                        if (other != null) {
                            int intersect = dsb.nusers.intersect(other.users).size();
                            dsb.rs[j-1] = intersect * 100 / dsb.nusers.size()

                            if (j == 7) {// 第七日的完成,则不需要计算了
                                //dsb.checked = true;
                            }
                        } else {
                            dsb.rs[j-1] = 0
                        }
                    }

                    mongoTemplate.save(dsb);
                }

                RecentIncome ri = new RecentIncome();
                ri.day = dsb.day
                ri.rs = Arrays.copyOf(dsb.rs, dsb.rs.length)
                ri.charge = dsb.charge
                ri.ncharge = dsb.ncharge
                ri.dau = dsb.users.size()
                ri.danu = dsb.nusers.size()
                ri.dacu = dsb.cusers.size()

                if (ri.dau > 0) {
                    ri.arpu = ri.charge / ri.dau / 100
                }
                int cusers = dsb.cusers.size()
                if (cusers > 0) {
                    ri.arppu = ri.charge / cusers / 100
                }
                int nusers = dsb.nusers.size()
                if (nusers > 0) {
                    ri.arnpu = ri.ncharge / nusers / 100
                }

                rtn.add(ri)
            }
        }

        return rtn
    }

    @ApiOperation(value = "获取指定日期的新玩家")
    @RequestMapping(value = "/cmd/getNewUsers", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "begin", value = "开始日期(包含)格式：[2017-9-1]", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束日期(不包含)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "source", value = "渠道(空为全部)", required = false, dataType = "String", paramType = "query")
    ])
    @ResponseBody long getNewUsers(HttpServletRequest request) {
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

    @ApiOperation(value = "获取最高充值的玩家")
    @RequestMapping(value = "/cmd/getTopAccount", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", value = "是否指定玩家id", required = false, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "前n个", required = true, dataType = "int", paramType = "query"),
    ])
    @ResponseBody List<TopCharge> getTopAccount(HttpServletRequest request) {
        long id = 0
        String idstr = request.getParameter("id")
        if (!Strings.isNullOrEmpty(idstr)) {
            id = Long.parseLong(idstr)
        }
        int count = Integer.parseInt(request.getParameter("count"))

        List<TopCharge> rtn = new ArrayList<>()

        List<AccountBean> accs
        if (id > 0) {
            accs = mongoTemplate.find(new Query(Criteria.where("id").is(id)), AccountBean.class)
        } else {
            accs = mongoTemplate.find(new Query().with(new Sort(Sort.Direction.DESC, "charge")).limit(count), AccountBean.class)
        }

        try {
            for (AccountBean acc : accs) {
                TopCharge tc = new TopCharge()
                tc.id = acc.id
                tc.account = acc.account
                tc.charge = acc.charge
                tc.createtime = acc.createtime

                UserBean u = dao.findUser(acc.id)
                if (u != null) {
                    for (UserRoleStruct urs : u.relates) {
                        tc.infos.add(String.format("zone:%s,role:%s,name:%s,vip:%s,level:%s", urs.server, urs.role, urs.name, urs.vip, urs.level))
                    }
                }
                rtn.add(tc)
            }
        } catch (Exception e) {

        }
        return rtn;
    }

    @ApiOperation(value = "获取最近的操作日志")
    @RequestMapping(value = "/cmd/getAdminLogs", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "count", value = "前n个", required = true, dataType = "int", paramType = "query"),
    ])
    @ResponseBody List<GmOperBean> getAdminLogs(HttpServletRequest request) {
        int count = Integer.parseInt(request.getParameter("count"))

        try {
            return mongoTemplate.find(new Query().with(new Sort(Sort.Direction.DESC, "time")).limit(count), GmOperBean.class);
        } catch (Exception e) {

        }
        return null;
    }

    @ApiOperation(value = "获取所有物品")
    @RequestMapping(value = "/cmd/getItems", method = RequestMethod.GET)
    @ApiImplicitParams([
    ])
    @ResponseBody List<ItemConf> getItems(HttpServletRequest request) {
        return confTemplate.findAll(ItemConf.class)
    }

    @ApiOperation(value = "获取所有英雄")
    @RequestMapping(value = "/cmd/getHeros", method = RequestMethod.GET)
    @ApiImplicitParams([
    ])
    @ResponseBody List<HeroConf> getHeros(HttpServletRequest request) {
        return confTemplate.findAll(HeroConf.class)
    }

    @Async
    @ApiOperation(value = "查看公告")
    @RequestMapping(value = "/cmd/getBulletin", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "file", value = "文件地址", required = true, dataType = "String", paramType = "query")
    ])
    @ResponseBody String getBulletin(HttpServletRequest request) {
        String file = request.getParameter("file")
        try {
            for (char c in file.toCharArray()) {
                if (!c.letterOrDigit) {
                    return "错误的文件名字"
                }
            }

            file = "/home/ubuntu/html/v1/" + file + ".html"
            return Files.readAllLines(Paths.get(file)).join("\n")
        } catch (Exception ex) {
            log.error("查看公告失败:" + ex.getMessage())
        }
        return "无法查看"
    }


    @Async
    @ApiOperation(value = "修改公告")
    @RequestMapping(value = "/cmd/fixBulletin", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "file", value = "文件地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "data", value = "公告内容", required = true, dataType = "String", paramType = "query")
    ])
    @ResponseBody String fixBulletin(HttpServletRequest request) {
        log.info("fixBulletin.")
        String file = request.getParameter("file")
        String data = request.getParameter("data")
        try {
            for (char c in file.toCharArray()) {
                if (!c.letterOrDigit) {
                    return "错误的文件名字"
                }
            }

            file = "/home/ubuntu/html/v1/" + file + ".html"

            log.info("data:" + data)

            Files.write(Paths.get(file), data.getBytes(Charset.defaultCharset()))
            return data
        } catch (Exception ex) {
            log.error("修改公告失败:" + ex.getMessage())
        }
        return "修改失败"
    }

    @ApiOperation(value = "检查充值订单，充值金额归纳到每个账号上")
    @RequestMapping(value = "/cmd/makeAccountOpenRmb", method = RequestMethod.GET)
    @ResponseBody String makeAccountOpenRmb(HttpServletRequest request) {
        try {
            // 1. 将所有账号的openrmb归0
            mongoTemplate.updateMulti(new Query(), Update.update("openrmb", 0), AccountBean.class)

            // 2. 遍历账号中charge大于0的，将该值除以100，记录到openrmb中
            List<AccountBean> abs = mongoTemplate.find(Query.query(Criteria.where("charge").gt(599)), AccountBean.class)
            for (AccountBean ab in abs) {
                ab.openrmb = (int)(ab.charge / 100)
                accountBeanRepository.save(ab)
            }

        } catch (Exception ex) {
            log.error("修改公告失败:" + ex.getMessage())
            return "失败:" + ex.getMessage()
        }
        return "完成"
    }
}

class RecentIncome {
    public int day;
    public int charge;  // 总充值
    public int ncharge; // 新增玩家充值
    public float[] rs = new float[7];  // 新增玩家7日留存
    public int dau; // 当日总活跃玩家
    public int danu; // 当日新增玩家
    public int dacu; // 当日充值玩家
    public float arpu; // 活跃玩家平均收入
    public float arppu; // 充值玩家平均收入
    public float arnpu; // 新增玩家平均收入
}

class TopCharge {
    public long id
    public String account
    public int charge
    public Date createtime
    public List<String> infos = new ArrayList<>()
}
