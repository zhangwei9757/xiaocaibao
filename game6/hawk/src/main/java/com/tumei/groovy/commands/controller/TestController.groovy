package com.tumei.controller

import com.google.common.base.Strings
import com.tumei.MobService
import com.tumei.centermodel.*
import com.tumei.centermodel.beans.ServerBean
import com.tumei.centermodel.beans.UserRoleBean
import com.tumei.common.DaoUtils
import com.tumei.common.ErrCode
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.common.utils.HttpUtils
import com.tumei.common.utils.JsonUtil
import com.tumei.common.utils.JwtUtil
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.util.stream.Collectors

/**
 * 测试用groovy 脚本
 */
class TestController {
    private static final Log log = LogFactory.getLog(this.class)

    @Autowired
    private MobService mobService

    @Autowired
    private Readonly readonly

    @Autowired
    private AuthenticationManager authenticationManager

    @Autowired
    private DaoUtils dao

    @Autowired
    private AccountBeanRepository accountBeanRepository

    @Autowired
    private ServersBeanRepository serversBeanRepository

    /**
     * 获取请求的真实ip地址
     * @param request
     * @return
     */
    private String getRealAddr(HttpServletRequest request) {
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

    /**
     * 读取当前可用的服务器配置
     *
     * @return
     */
    private List<ServerBean> getServers(boolean isGm) {
        List<ServersBean> ssbs = serversBeanRepository.findAll()
        ServersBean ssb
        if (ssbs.size() == 0) {
            ssb = new ServersBean()
            ssb.setDescrition("首次自动生成的公告信息")
            if (true) {
                ServerBean sb = new ServerBean()
                sb.id = 1
                sb.name = "测试服务器[自动生成]"
                sb.host = "ws://192.168.1.222:5001/ws"
                sb.status = "开服"
                ssb.getServers().add(sb)
            }
            ssbs.add(ssb)
            serversBeanRepository.save(ssbs)
        } else {
            ssb = ssbs.get(0)
            if (ssb.getServers().size() <= 0) {
                ServerBean sb = new ServerBean()
                sb.id = 1
                sb.name = "测试服务器[自动生成]"
                sb.host = "ws://192.168.1.222:5001/ws"
                sb.status = "开服"
                ssb.getServers().add(sb)
                serversBeanRepository.save(ssbs)
            }
        }

        List<ServerBean> sbs = ssb.getServers()
        long now = System.currentTimeMillis()
        return sbs.stream().filter({ sb ->
            // 有日期描述的时候，并且日期小于当前时间，显示
            if (sb.start != null) {
                if (sb.start.getTime() > now && !isGm) {
                    log.info("存储时间为:" + sb.start.getTime() + " now:" + now)
                    return false
                }
            }
            return true
        }).collect(Collectors.toList())
    }

    @ApiOperation(value = "登录")
    @RequestMapping(value = "/logon", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "account", value = "帐号", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "version", value = "客户端版本", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "bundle", value = "客户端包名", required = false, dataType = "String", paramType = "query"),
    ])
    @ResponseBody Reply logon(HttpServletRequest request) {
        try {
            String account = request.getParameter("account")
            String password = request.getParameter("password")
//            String idfa = request.getParameter("idfa")
            String version = request.getParameter("version")
            String bundle = request.getParameter("bundle")

            // 检测包名是否合法
//            if (bundle != null && !readonly.getAllBundles().containsKey(bundle.trim())) {
//                return new Reply(ErrCode.未知来源的安装包)
//            }

            // 帐号是否非法
            if (Strings.isNullOrEmpty(account) || !checkName(account)) {
                if (Strings.isNullOrEmpty(account) || !checkName(account)) {
                    return new Reply(ErrCode.非法的帐号名)
                }
            }

            // 版本比较
            if (Strings.isNullOrEmpty(version) || !checkVersion(version)) {
                return new Reply(ErrCode.版本过低)
            }

            // 查询存在
            AccountBean ub = accountBeanRepository.findByAccount(account)
            if (ub == null) {
                return new Reply(ErrCode.帐号不存在) // 帐号不存在
            }

            boolean hasUser = false
            boolean isGM = false
            // 判断登录权限和GM权限
            if (!Strings.isNullOrEmpty(ub.getRole())) {
                String[] roles = ub.getRole().split(",")
                for (int i = 0; i < roles.size(); ++i) {
                    String r = roles[i].trim()
                    if (r.equalsIgnoreCase("USER")) {
                        hasUser = true
                    } else if (r.equalsIgnoreCase("ADMIN")) {
                        isGM = true
                    }
                }
            }

            if (!hasUser) {
                return new Reply(ErrCode.帐号权限被封禁)
            }

            // 记录登录信息
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(account, password)
            Authentication authentication = authenticationManager.authenticate(token)

            SecurityContext sc = SecurityContextHolder.getContext()
            sc.setAuthentication(authentication)

            // 获取认证后的帐号对应的uid,并保存到session中，传递到game server
            HttpSession session = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest().getSession()
            session.setAttribute("uid", ub.getId())

            log.info("帐号(" + account + ") 认证成功, uid(" + ub.getId() + "), session(" + session.getId() + ")")

            // 读取该帐号相关的最近登录服务器id和有帐号的服务器id
            Reply r = new Reply(ErrCode.成功)
            // 返回一个jwt token, 客户端可以选择使用cookie+session的方式，也可以使用jwtToken的验证模式
            r.jwt = JwtUtil.generate(ub.getId(), ub.getAccount(), ub.getRole())

            UserBean u = dao.findUser(ub.getId())
            if (u != null) {
                r.last = u.getLast()
                r.relates.addAll(u.getRelates())
                log.info("--- 最近登录的服务器id:" + r.last + " 相关服务器帐号个数:" + r.relates.size())
            }

            r.servers.addAll(getServers(isGM))

            dao.addUser(u.getId())

            return r
        } catch (AuthenticationException ae) {
            log.error("认证错误: " + ae.getMessage())
            return new Reply(ErrCode.帐号不存在或密码错误)
        } catch (Exception ex) {
            log.error("错误: " + ex.getMessage(), ex)
        }

        return new Reply(ErrCode.系统错误)
    }

    @ApiOperation(value = "QuickSDK登录")
    @RequestMapping(value = "/logonQ", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "渠道玩家id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "token", value = "令牌", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "version", value = "客户端版本", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "bundle", value = "客户端包名", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "ccode", value = "channel code", required = false, dataType = "String", paramType = "query"),
    ])
    @ResponseBody Reply logonQ(HttpServletRequest request) {
        try {
            String uid = request.getParameter("uid")
            String token = request.getParameter("token")
            String version = request.getParameter("version")
            String pcode = "76030988121282500240860743252589"
            String ccode = request.getParameter("ccode")
//            String bundle = request.getParameter("bundle")

            String guid = ccode + "_" + uid

            // 检测包名是否合法
//            if (bundle != null && !readonly.getAllBundles().containsKey(bundle.trim())) {
//                return new Reply(ErrCode.未知来源的安装包)
//            }

            // 版本比较
            if (!checkVersion(version)) {
                return new Reply(ErrCode.版本过低)
            }

            // 验证帐号对应的token是否有效
            String url = "http://checkuser.sdk.quicksdk.net/v2/checkUserInfo";

            try {
                HashMap<String, String> params = new HashMap<>()
                params.put("token", token)
                params.put("product_code", pcode)
                params.put("uid", uid)
                params.put("channel_code", ccode)
                String ydRtn = HttpUtils.sendGet(url, params)

                if (!ydRtn.equalsIgnoreCase("1")) {
                    log.error("登录失败:" + ydRtn)
                    return new Reply(ErrCode.Quick认证失败, ydRtn)
                } else {
                    log.warn("Quick认证玩家成功[" + uid + "].")
                }
            } catch (Exception e) {
                log.error("exception:" + e.toString())
                e.printStackTrace()
                return new Reply(ErrCode.Quick认证失败)
            }

            String account = String.format("__QK%s", guid)

            // 查询存在
            AccountBean ub = accountBeanRepository.findByAccount(account)
            if (ub == null) {
                // 直接创建对应的帐号
                ub = new AccountBean()
                ub.setId(dao.nextVal())
                ub.setAccount(account)
                ub.setPasswd(account)
                ub.setCreatetime(new Date())
                ub.setRole("USER")
                accountBeanRepository.save(ub)
            }

            // 记录登录信息
            UsernamePasswordAuthenticationToken upa = new UsernamePasswordAuthenticationToken(account, account)
            Authentication authentication = authenticationManager.authenticate(upa)

            if (!authentication.getAuthorities().stream().anyMatch({ ga ->
                if (ga.getAuthority().equalsIgnoreCase("USER")) {
                    return true
                }
                return false
            })) {
                return new Reply(ErrCode.帐号权限被封禁)
            }

            SecurityContext sc = SecurityContextHolder.getContext()
            sc.setAuthentication(authentication)

            // 获取认证后的帐号对应的uid,并保存到session中，传递到game server
            HttpSession session = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest().getSession()
            session.setAttribute("uid", ub.getId())

            log.info("帐号(" + account + ") 认证成功, uid(" + ub.getId() + "), session(" + session.getId() + ")")

            // 读取该帐号相关的最近登录服务器id和有帐号的服务器id
            Reply r = new Reply(ErrCode.成功)
            r.jwt = JwtUtil.generate(ub.getId(), ub.getAccount(), ub.getRole())
            UserBean u = dao.findUser(ub.getId())
            if (u != null) {
                r.last = u.getLast()
                r.relates.addAll(u.getRelates())
                log.info("--- 最近登录的服务器id:" + r.last + " 相关服务器帐号个数:" + r.relates.size())
            }

            boolean isGM = authentication.getAuthorities().stream().anyMatch({ ga ->
                if (ga.getAuthority().equalsIgnoreCase("ADMIN")) {
                    return true
                }
                return false
            })
            r.servers.addAll(getServers(isGM))

            return r
        } catch (AuthenticationException ae) {
            log.error("认证错误: " + ae.getMessage())
            return new Reply(ErrCode.帐号不存在或密码错误)
        } catch (Exception ex) {
            log.error("错误: " + ex.getMessage(), ex)
        }

        return new Reply(ErrCode.系统错误)
    }




    @ApiOperation(value = "云顶登录")
    @RequestMapping(value = "/logonYD", method = RequestMethod.GET)
    @ApiImplicitParams([
        @ApiImplicitParam(name = "uid", value = "登录回调玩家id", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "ydtoken", value = "令牌", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "idfa", value = "机器码", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "version", value = "客户端版本", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "bundle", value = "客户端包名", required = false, dataType = "String", paramType = "query"),
    ])
    @ResponseBody Reply logonYD(HttpServletRequest request) {
        try {
            String uid = request.getParameter("uid")
            String ydtoken = request.getParameter("ydtoken")
//            String idfa = request.getParameter("idfa")
            String version = request.getParameter("version")
            String bundle = request.getParameter("bundle")

            String appid = "6358"

            // 检测包名是否合法, 并获取正确的appid
            if (bundle != null) {
                BundleBean bb = readonly.getAllBundles().getOrDefault(bundle.trim(), null)
                if (bb == null) {
                    return new Reply(ErrCode.未知来源的安装包)
                }

                appid = bb.appid
            }

            // 版本比较
            if (!checkVersion(version)) {
                return new Reply(ErrCode.版本过低)
            }

            // 验证帐号对应的token是否有效
            String url = "http://ins.app-fame.com/act.aspx"

            try {
                HashMap<String, String> params = new HashMap<>()
                params.put("act", "118")
                params.put("userid", uid)
                params.put("usertoken", ydtoken)
                params.put("appid", appid)
                String ydRtn = HttpUtils.sendGet(url, params)

                YDReturn ydReturn = JsonUtil.Unmarshal(ydRtn, YDReturn.class)
                if (ydReturn.result != "1") {
                    log.error("登录失败:" + ydReturn.toString())
                    return new Reply(ErrCode.云顶认证失败, ydReturn.desc)
                } else {
                    log.warn("云顶认证玩家成功[" + uid + "].")
                }
            } catch (Exception e) {
                log.error("exception:" + e.toString())
                e.printStackTrace()
                return new Reply(ErrCode.云顶认证失败)
            }

            String account = String.format("__YD%s", uid)

            // 查询存在
            AccountBean ub = accountBeanRepository.findByAccount(account)
            if (ub == null) {
                // 直接创建对应的帐号
                ub = new AccountBean()
                ub.setId(dao.nextVal())
                ub.setAccount(account)
                ub.setPasswd(account)
                ub.setCreatetime(new Date())
                ub.setRole("USER")
                accountBeanRepository.save(ub)
            }

            // 记录登录信息
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(account, account)
            Authentication authentication = authenticationManager.authenticate(token)

            if (!authentication.getAuthorities().stream().anyMatch({ ga ->
                if (ga.getAuthority().equalsIgnoreCase("USER")) {
                    return true
                }
                return false
            })) {
                return new Reply(ErrCode.帐号权限被封禁)
            }

            SecurityContext sc = SecurityContextHolder.getContext()
            sc.setAuthentication(authentication)

            // 获取认证后的帐号对应的uid,并保存到session中，传递到game server
            HttpSession session = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest().getSession()
            session.setAttribute("uid", ub.getId())

            log.info("帐号(" + account + ") 认证成功, uid(" + ub.getId() + "), session(" + session.getId() + ")")

            // 读取该帐号相关的最近登录服务器id和有帐号的服务器id
            Reply r = new Reply(ErrCode.成功)
            r.jwt = JwtUtil.generate(ub.getId(), ub.getAccount(), ub.getRole())
            UserBean u = dao.findUser(ub.getId())
            if (u != null) {
                r.last = u.getLast()
                r.relates.addAll(u.getRelates())
                log.info("--- 最近登录的服务器id:" + r.last + " 相关服务器帐号个数:" + r.relates.size())
            }

            boolean isGM = authentication.getAuthorities().stream().anyMatch({ ga ->
                if (ga.getAuthority().equalsIgnoreCase("ADMIN")) {
                    return true
                }
                return false
            })
            r.servers.addAll(getServers(isGM))
            dao.addUser(u.getId())

            return r
        } catch (AuthenticationException ae) {
            log.error("认证错误: " + ae.getMessage())
            return new Reply(ErrCode.帐号不存在或密码错误)
        } catch (Exception ex) {
            log.error("错误: " + ex.getMessage(), ex)
        }

        return new Reply(ErrCode.系统错误)
    }


    @ApiOperation(value = "注册帐号")
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    @ApiImplicitParams([
        @ApiImplicitParam(name = "account", value = "帐号名", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "idfa", value = "机器码", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "version", value = "客户端版本", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "bundle", value = "客户端包名", required = false, dataType = "String", paramType = "query"),
    ])
    @ResponseBody Reply register(HttpServletRequest request) {
        try {

            String account = request.getParameter("account")
            String password = request.getParameter("password")
//            String idfa = request.getParameter("idfa")
            String version = request.getParameter("version")
            String bundle = request.getParameter("bundle")

            // 检测包名是否合法
//            if (bundle != null && !readonly.getAllBundles().containsKey(bundle.trim())) {
//                return new Reply(ErrCode.未知来源的安装包)
//            }
            String ip = getRealAddr(request)
            String os = request.getHeader("User-Agent")

            /**
             * 帐号名字检测，非法屏蔽
             */
            if (Strings.isNullOrEmpty(account) || !checkName(account)) {
                return new Reply(ErrCode.非法的帐号名)
            }

            // 版本比较
            if (Strings.isNullOrEmpty(version) || !checkVersion(version)) {
                return new Reply(ErrCode.版本过低)
            }

            AccountBean user
            synchronized (this) {
                user = accountBeanRepository.findByAccount(account)
                if (user != null) {
                    return new Reply(ErrCode.重复的帐号名) // 重复的帐号名
                }

                user = new AccountBean()
                user.setId(dao.nextVal())
                user.setAccount(account)
                user.setPasswd(password)
                user.setCreatetime(new Date())
                user.setRole("USER")
                user.setSource(bundle)
                user.setIp(ip);
                user.setOs(os);
                accountBeanRepository.save(user)

                MobBean mb = mobService.conclude(ip, user.getId())
                if (mb != null) { // 归因成功,回调一下通知mobcastle.
                    RemoteService.instance.mobNotify(mb.callback)
                }
            }

            // 记录登录
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(account, password)
            Authentication authentication = authenticationManager.authenticate(token)
            SecurityContextHolder.getContext().setAuthentication(authentication)

            // 获取认证后的帐号对应的uid,并保存到session中，传递到game server
            HttpSession session = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest().getSession()
            session.setAttribute("uid", user.getId())
            log.info("帐号(" + account + ") 注册成功")

            Reply r = new Reply(ErrCode.成功)
            r.jwt = JwtUtil.generate(user.getId(), user.getAccount(), user.getRole())
            UserBean u = dao.findUser(user.getId())
            if (u != null) {
                r.last = u.getLast()
                r.relates.addAll(u.getRelates())
            }

            r.servers.addAll(getServers(false))

            dao.addUser(u.getId())
            return r
        } catch (Exception ex) {
            log.error("注册帐号错误:", ex)
        }

        return new Reply(ErrCode.系统错误)
    }

    @RequestMapping(value = "/uid", method = RequestMethod.GET)
    @ResponseBody String uid(HttpSession session) {
        Object obj = session.getAttribute("uid")
        if (obj != null) {
            return obj.toString()
        }
        return "没有对应的session"
    }

    /**
     * 版本检测
     * @param version
     * @return
     */
    private boolean checkVersion(String version) {
        if (version == null) {
            return false
        }

//        String[] fields = version.split(".")

        return true
    }

    /**
     * 帐号不能以__开头，这个是第三方登录保留帐号
     * @param account
     * @return
     */
    private boolean checkName(String account) {
        if (account.startsWith("__")) {
            return false
        }

        return true
    }
}

/**
 * 登录，注册返回
 */
class Reply {
    /**
     * 错误码
     */
    public int err
    /**
     * 错误字符串
     */
    public String result
    /**
     * 帐号下有角色的服务器id
     */
    public List<UserRoleBean> relates = new ArrayList<>()
    /**
     * 最近登录的服务器
     */
    public int last = 0

    public List<ServerBean> servers = new ArrayList<>()

    public String jwt = ""

    Reply(ErrCode ec) {
        err = (int) ec.ordinal()
        result = ec.name()
    }

    Reply(ErrCode ec, String msg) {
        err = (int) ec.ordinal()
        result = msg
    }

    Reply(ErrCode ec, int _last, List<UserRoleBean> _relates) {
        this(ec)
        last = _last
        relates.addAll(_relates)
    }
}

// 云顶认证返回结构
class YDReturn {
    private String result
    private String desc

    String getResult() {
        return result
    }

    void setResult(String result) {
        this.result = result
    }

    String getDesc() {
        return desc
    }

    void setDesc(String desc) {
        this.desc = desc
    }

    YDReturn() {}

    @Override
    String toString() {
        return "YDReturn{" + "result=" + result + ", desc='" + desc + '\'' + '}'
    }
}

