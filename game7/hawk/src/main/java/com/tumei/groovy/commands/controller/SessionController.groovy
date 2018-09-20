package com.tumei.controller

import com.google.common.base.Strings
import com.tumei.centermodel.*
import com.tumei.centermodel.beans.UserRoleStruct
import com.tumei.common.DaoCenter
import com.tumei.common.Readonly
import com.tumei.common.utils.ErrCode
import com.tumei.common.utils.HttpUtils
import com.tumei.common.utils.JsonUtil
import com.tumei.common.utils.JwtUtil
import com.tumei.common.utils.MD5Util
import io.jsonwebtoken.Claims
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest
import java.util.stream.Collectors

/**
 * 测试用groovy 脚本
 */
class SessionController {
    private static final Log log = LogFactory.getLog(SessionController.class)

    @Autowired
    private Readonly readonly

    @Autowired
    private DaoCenter dao

    @Autowired
    private AccountBeanRepository accountBeanRepository

    @Autowired
    private ServerBeanRepository serverBeanRepository

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
     * 读取当前可用的服务器信息
     *
     * @return
     */
    private List<ServerBean> getServers(boolean isGm, int minid) {
        List<ServerBean> ssbs = serverBeanRepository.findAll()
        if (ssbs.size() <= 0) {
            ServerBean sb = new ServerBean()
            sb.id = 1
            sb.name = "1区"
            sb.host = "192.168.1.223:5001"
            sb.status = "新服"
            sb.start = new Date()
            serverBeanRepository.save(sb)
            ssbs.add(sb)
        }

        long now = System.currentTimeMillis()
        return ssbs.stream().filter({ sb ->
            if (sb.id < minid) {
                return false
            }

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
    @RequestMapping(value = "/flushServers", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "token", value = "登录后获取的秘钥", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    List<ServerBean> flushServers(HttpServletRequest request) {
        try {
            String token = request.getParameter("token")
            Claims claims = JwtUtil.verify(token)
            long uid = (long) (Long.parseLong(claims.getId()) / 1000)

            AccountBean ub = accountBeanRepository.findById(uid)
            if (ub != null) {
                boolean isGM = (ub.getRole().indexOf("ADMIN") != -1)
                return getServers(isGM, 1)
            }
        } catch (Exception ex) {
            log.error("刷新服务器列表失败，无法获取token信息:" + ex.message)
        }

        return null
    }


    @ApiOperation(value = "登录")
    @RequestMapping(value = "/logon", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "account", value = "帐号", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "version", value = "客户端版本", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "bundle", value = "客户端包名", required = false, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    Reply logon(HttpServletRequest request) {
        try {
            String account = request.getParameter("account")
            String password = request.getParameter("password")
            String version = request.getParameter("version")
            String bundle = request.getParameter("bundle")

            if (Strings.isNullOrEmpty(account)) {
                return new Reply(ErrCode.帐号不存在)
            }

            account = account.toLowerCase()

            // 检测包名是否合法
//            if (bundle != null && !readonly.getAllBundles().containsKey(bundle.trim())) {
//                return new Reply(ErrCode.未知来源的安装包)
//            }

            // 帐号是否非法
//            if (!checkName(account)) {
//                return new Reply(ErrCode.非法的帐号名)
//            }

            // 版本比较
            if (!checkVersion(version)) {
                return new Reply(ErrCode.版本过低)
            }

            // 查询存在
            AccountBean ub = accountBeanRepository.findByAccount(account)
            if (ub == null) {
                return new Reply(ErrCode.帐号不存在) // 帐号不存在
            }

            if (!ub.getPasswd().equalsIgnoreCase(password)) {
                return new Reply(ErrCode.帐号不存在或密码错误)
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

            log.info("帐号(" + account + ") 认证成功, uid(" + ub.getId() + ")")

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

            r.servers.addAll(getServers(isGM, 1))
            dao.addUser(u.getId(), false)
            return r
        } catch (AuthenticationException ae) {
            log.error("认证错误: " + ae.getMessage())
            return new Reply(ErrCode.帐号不存在或密码错误)
        } catch (Exception ex) {
            log.error("错误: " + ex.getMessage(), ex)
        }

        return new Reply(ErrCode.系统错误)
    }

    @ApiOperation(value = "登录")
    @RequestMapping(value = "/logon_gm", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "account", value = "帐号", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    Reply logon_gm(HttpServletRequest request) {
        try {
            String account = request.getParameter("account")
            String password = request.getParameter("password")

            if (Strings.isNullOrEmpty(account)) {
                return new Reply(ErrCode.帐号不存在)
            }

            account = account.toLowerCase()


            // 查询存在
            AccountBean ub = accountBeanRepository.findByAccount(account)
            if (ub == null) {
                return new Reply(ErrCode.帐号不存在) // 帐号不存在
            }

            if (!ub.getPasswd().equalsIgnoreCase(password)) {
                return new Reply(ErrCode.帐号不存在或密码错误)
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

            if (!hasUser || !isGM) {
                return new Reply(ErrCode.帐号权限被封禁)
            }

            log.info("帐号(" + account + ") 认证成功, uid(" + ub.getId() + ")")

            // 读取该帐号相关的最近登录服务器id和有帐号的服务器id
            Reply r = new Reply(ErrCode.成功)
            // 返回一个jwt token, 客户端可以选择使用cookie+session的方式，也可以使用jwtToken的验证模式
            r.jwt = JwtUtil.generate(ub.getId(), ub.getAccount(), ub.getRole())

            r.servers.addAll(getServers(isGM, 0))
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
    @ResponseBody
    Reply logonQ(HttpServletRequest request) {
        try {
            String uid = request.getParameter("uid")
            String token = request.getParameter("token")
            String version = request.getParameter("version")
            String pcode = "76030988121282500240860743252589"
            String ccode = request.getParameter("ccode")
            String bundle = request.getParameter("bundle")
            String ip = getRealAddr(request)

            String guid = ccode + "_" + uid

            // 版本比较
            if (!checkVersion(version)) {
                return new Reply(ErrCode.版本过低)
            }

            // 验证帐号对应的token是否有效
            String url = "http://checkuser.sdk.quicksdk.net/v2/checkUserInfo";

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

            String account = String.format("__QK%s", guid)

            boolean isNew = false
            boolean isGM = false
            boolean hasUser = false
            // 查询存在
            AccountBean ub
            synchronized (this) {
                ub = accountBeanRepository.findByAccount(account)
                if (ub == null) {
                    // 直接创建对应的帐号
                    ub = new AccountBean()
                    ub.setId(dao.nextVal())
                    ub.setAccount(account)
                    ub.setPasswd(account)
                    ub.setCreatetime(new Date())
                    ub.setRole("USER")
                    ub.setSource(bundle)
                    ub.setIp(ip)
                    accountBeanRepository.save(ub)
                    isNew = true
                }
            }

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

            log.info("帐号(" + account + ") 认证成功, uid(" + ub.getId() + ")")

            // 读取该帐号相关的最近登录服务器id和有帐号的服务器id
            Reply r = new Reply(ErrCode.成功)
            r.jwt = JwtUtil.generate(ub.getId(), ub.getAccount(), ub.getRole())
            UserBean u = dao.findUser(ub.getId())
            if (u != null) {
                r.last = u.getLast()
                r.relates.addAll(u.getRelates())
                log.info("--- 最近登录的服务器id:" + r.last + " 相关服务器帐号个数:" + r.relates.size())
            }

            r.servers.addAll(getServers(isGM, 1))
            dao.addUser(u.getId(), isNew)

            return r
        } catch (Exception ex) {
            log.error("错误: " + ex.getMessage(), ex)
        }

        return new Reply(ErrCode.系统错误)
    }


    @ApiOperation(value = "小七SDK登录")
    @RequestMapping(value = "/logonX7", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "token", value = "令牌", required = true, dataType = "String", paramType = "query"),

            @ApiImplicitParam(name = "version", value = "客户端版本", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "bundle", value = "客户端包名", required = false, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    Reply logonX7(HttpServletRequest request) {
        try {
            String token = request.getParameter("token")
            String version = request.getParameter("version")
            String bundle = request.getParameter("bundle")

            String appkey = "e1a91ca0a00a53eb91b263008134d593"
            String ip = getRealAddr(request)


            // 验证帐号对应的token是否有效
            String url = "https://api.x7sy.com/user/test_check_login";

            // 1. appkey + token 生成签名
            String sign = MD5Util.encode(appkey + token);
            log.info("小七登录请求，token:" + token + ", appkey:" + appkey + ", sign:" + sign)

            // 2. 发送 token + sign 给小七服务器进行认证
            HashMap<String, String> params = new HashMap<>()
            params.put("tokenkey", token)
            params.put("sign", sign)
            String rtn = HttpUtils.sendGet(url, params)

            // 3. 获取认证返回的具体用户信息
            X7Rtn x7Rtn = JsonUtil.Unmarshal(rtn, X7Rtn.class)
            if (x7Rtn.errorno != 0) {
                return new Reply(ErrCode.X7认证失败)
            }

            // 4. 根据用户信息决定是否注册用户,或者直接登录
            String account = String.format("__X7%s", x7Rtn.data.guid)

            boolean isNew = false
            boolean isGM = false
            boolean hasUser = false
            // 查询存在
            AccountBean ub
            synchronized (this) {
                ub = accountBeanRepository.findByAccount(account)
                if (ub == null) {
                    // 直接创建对应的帐号
                    ub = new AccountBean()
                    ub.setId(dao.nextVal())
                    ub.setAccount(account)
                    ub.setPasswd(account)
                    ub.setCreatetime(new Date())
                    ub.setRole("USER")
                    ub.setSource(bundle)
                    ub.setChannel("x7")
                    ub.setIp(ip)
                    accountBeanRepository.save(ub)
                    isNew = true
                }
            }

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

            log.info("帐号(" + account + ") 认证成功, uid(" + ub.getId() + ")")

            // 读取该帐号相关的最近登录服务器id和有帐号的服务器id
            Reply r = new Reply(ErrCode.成功)
            r.account = x7Rtn.data.guid
            r.jwt = JwtUtil.generate(ub.getId(), ub.getAccount(), ub.getRole())
            UserBean u = dao.findUser(ub.getId())
            if (u != null) {
                r.last = u.getLast()
                r.relates.addAll(u.getRelates())
                log.info("--- 最近登录的服务器id:" + r.last + " 相关服务器帐号个数:" + r.relates.size())
            }

            r.servers.addAll(getServers(isGM, 1))
            dao.addUser(u.getId(), isNew)

            return r
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
    @ResponseBody
    Reply register(HttpServletRequest request) {
        try {

            String account = request.getParameter("account")
            String password = request.getParameter("password")
//            String idfa = request.getParameter("idfa")
            String version = request.getParameter("version")
            String bundle = request.getParameter("bundle")
            String ip = getRealAddr(request)

            // 检测包名是否合法
//            if (bundle != null && !readonly.getAllBundles().containsKey(bundle.trim())) {
//                return new Reply(ErrCode.未知来源的安装包)
//            }

            account = account.toLowerCase()

            /**
             * 帐号名字检测，非法屏蔽
             */
            if (!checkName(account)) {
                return new Reply(ErrCode.非法的帐号名)
            }

            // 版本比较
            if (!checkVersion(version)) {
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
                user.setIp(ip)
                accountBeanRepository.save(user)
            }

            Reply r = new Reply(ErrCode.成功)
            r.jwt = JwtUtil.generate(user.getId(), user.getAccount(), user.getRole())
            UserBean u = dao.findUser(user.getId())
            if (u != null) {
                r.last = u.getLast()
                r.relates.addAll(u.getRelates())
            }

            r.servers.addAll(getServers(false, 1))
            dao.addUser(u.getId(), true)
            log.info("帐号(" + account + ") 注册成功, ip地址:" + ip)
            return r
        } catch (Exception ex) {
            log.error("注册帐号错误:", ex)
        }

        return new Reply(ErrCode.系统错误)
    }

    @ApiOperation(value = "自动注册帐号")
    @RequestMapping(value = "/register_auto", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "idfa", value = "机器码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "version", value = "客户端版本", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "bundle", value = "客户端包名", required = false, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    Reply register_auto(HttpServletRequest request) {
        try {
            String version = request.getParameter("version")
            String bundle = request.getParameter("bundle")
            String ip = getRealAddr(request)

            String account = "__xcb_" + System.currentTimeMillis()
            String password = account

            // 版本比较
            if (!checkVersion(version)) {
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
                user.setIp(ip)
                accountBeanRepository.save(user)
            }

            Reply r = new Reply(ErrCode.成功)
            r.account = account // 账号密码一致
            r.jwt = JwtUtil.generate(user.getId(), user.getAccount(), user.getRole())
            UserBean u = dao.findUser(user.getId())
            if (u != null) {
                r.last = u.getLast()
                r.relates.addAll(u.getRelates())
            }

            r.servers.addAll(getServers(false, 1))
            dao.addUser(u.getId(), true)
            log.info("帐号(" + account + ") 注册成功, ip地址:" + ip)
            return r
        } catch (Exception ex) {
            log.error("注册帐号错误:", ex)
        }

        return new Reply(ErrCode.系统错误)
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

        String[] fields = version.split("\\.")
        int a = Integer.parseInt(fields[0])
        int b = Integer.parseInt(fields[1])

        if (a < 1) {
            return false
        }
        if (a == 1 && b <= 2) {
            return false
        }

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
    public List<UserRoleStruct> relates = new ArrayList<>()
    /**
     * 最近登录的服务器
     */
    public int last = 0

    public List<ServerBean> servers = new ArrayList<>()

    public String jwt = ""

    public String account = ""

    Reply(ErrCode ec) {
        err = (int) ec.ordinal()
        result = ec.name()
    }

    Reply(ErrCode ec, String msg) {
        err = (int) ec.ordinal()
        result = msg
    }

    Reply(ErrCode ec, int _last, List<UserRoleStruct> _relates) {
        this(ec)
        last = _last
        relates.addAll(_relates)
    }
}

class X7User {
    String guid
    String username
}

class X7Rtn {
    int errorno
    String errormsg
    X7User data
}

