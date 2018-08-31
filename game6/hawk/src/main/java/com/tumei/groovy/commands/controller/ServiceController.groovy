package com.tumei.controller

import com.tumei.centermodel.*
import com.tumei.centermodel.beans.ServerBean
import com.tumei.common.DaoUtils
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.common.utils.MD5Util
import com.tumei.common.utils.QuickEncrypt
import com.tumei.configs.MongoTemplateConfig
import com.tumei.model.RoleBean
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.apache.catalina.servlet4preview.http.HttpServletRequest
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.w3c.dom.Document
import org.xml.sax.InputSource

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Created by Administrator on 2016/12/16 0016.
 *
 * 内网服务访问的接口
 */
class ServiceController {
    private Log log = LogFactory.getLog(this.class)

    @Autowired
    private DaoUtils dao

    @Autowired
    private Readonly readonly

    @Autowired
    private ServersBeanRepository serversBeanRepository;
    @Autowired
    private AccountBeanRepository accountBeanRepository;

    @ApiOperation(value = "增加玩家新角色所在的服务器id")
    @RequestMapping(value = "/service/updateLatestServer", method = RequestMethod.GET)
    @ApiImplicitParams([
        @ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query"),
        @ApiImplicitParam(name = "server", value = "服务器ID", required = true, dataType = "int", paramType = "query"),
        @ApiImplicitParam(name = "level", value = "玩家等级", required = true, dataType = "int", paramType = "query"),
        @ApiImplicitParam(name = "icon", value = "头像", required = true, dataType = "int", paramType = "query"),
        @ApiImplicitParam(name = "vip", value = "vip", required = true, dataType = "int", paramType = "query"),
        @ApiImplicitParam(name = "name", value = "名字", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    String updateLatestServer(HttpServletRequest request) {
        long id = Long.parseLong(request.getParameter("id"))
        int server = Integer.parseInt(request.getParameter("server"))
        int level = Integer.parseInt(request.getParameter("level"))
        int icon = Integer.parseInt(request.getParameter("icon"))
        int vip = Integer.parseInt(request.getParameter("vip"))
        String name = request.getParameter("name")

        UserBean ub = dao.findUser((long)(id/1000))
        if (ub != null) {
            ub.setLast(server)
            ub.update(id, server, level, vip, icon, name)
        }
        return ""
    }

    class ServerInfo {
        public int server_id;
        public String server_name;
        public ServerInfo(int id, String name) {
            server_id = id;
            server_name = name;
        }
    }

    class ServerList {
        public int success;
        public String desc = "";
        public List<ServerInfo> server = new ArrayList<>();
    }

    @ApiOperation(value = "获取服务器列表")
    @RequestMapping(value = "/service/getServerList", method = RequestMethod.POST)
    @ResponseBody ServerList getServerList() {
        ServerList sl = new ServerList();

        try {
            List<ServersBean> ssbs = serversBeanRepository.findAll();
            ServersBean ssb;
            if (ssbs.size() != 0) {
                ssb = ssbs.get(0);
                for (ServerBean sb : ssb.getServers()) {
                    sl.server.add(new ServerInfo(sb.id, sb.name));
                }
            }
            sl.success = 1;
        } catch (Exception ex) {
            sl.desc = ex.getMessage();
        }

        return sl;
    }

    @Autowired
    private MongoTemplateConfig mongoTemplateConfig;

    class UserInfo {
        public int success;
        public String desc = "";
        public int app_id;
        public int user_id;
        public String player_id = "";
        public String player_name = "";
        public int player_rank;
    }

    @ApiOperation(value = "验证玩家")
    @RequestMapping(value = "/service/validateUser", method = RequestMethod.POST)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "server_id", value = "区服id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "user_name", value = "玩家id", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody UserInfo validateUser(HttpServletRequest request) {
        UserInfo ui = new UserInfo();
        try {
            String sid = request.getParameter("server_id").trim();
            long uid = Long.parseLong(request.getParameter("user_name"))
            // 查询存在
            AccountBean ub = accountBeanRepository.findById((uid / 1000) as int);
            if (ub == null) {
                ui.desc = "账号不存在";
            } else {
                MongoTemplate other = mongoTemplateConfig.otherTemplate("x" + sid);
                RoleBean rb = other.findOne(new Query(Criteria.where("id").is(uid)), RoleBean.class);
                if (rb != null) {
                    ui.app_id = 6358;
                    ui.player_id = rb.id.toString();
                    ui.player_name = rb.nickname;
                    ui.player_rank = rb.level;
                    try {
                        String ydc = ub.account.substring(4);
                        ui.user_id = Integer.parseInt(ydc);
                        ui.success = 1;
                    } catch (Exception ee) {
//                            ui.user_id = 1883479;
                        ui.desc = "非云顶账号,没有对应的云顶用户ID,可能cp测试做了映射.";
                    }
                } else {
                    ui.desc = "角色不存在";
                }
            }
        } catch (Exception ex) {
            ui.desc = ex.getMessage();
        }
        return ui;
    }



    class PayResult {
        /**
         * 1: 成功
         * 0: 失败
         */
        public int success
        public String desc

        public PayResult() {
        }

        public PayResult(int _success, String _desc) {
            success = _success
            desc = _desc
        }
    }

    @Autowired
    private ProductBeanRepository productBeanRepository

    @Autowired
    private ReceiptBeanRepository receiptBeanRepository


    @ApiOperation(value = "Quick支付接口")
    @RequestMapping(value = "/service/qPay", method = RequestMethod.POST)
    @ResponseBody String qPay(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap()

        String nt_data = "";
        String sign = "";
        String md5Sign = "";

        String ckey = "42335836186956690852442735037142"

        for (String key : params.keySet()) {
            String[] values = params.get(key)
            if (values.length <= 0) {
                continue
            }

            String val = values[0]
            log.warn("key[" + key + "]  value[" + val + "].")

            switch (key.toLowerCase()) {
                case "nt_data":
                    nt_data = val
                    break
                case "sign":
                    sign = val
                    break;
                case "md5sign":
                    md5Sign = val
                    break;
            }
        }


        try {
            String md5Key = "olawdzyktbfrrjrubxbenpnkxp4jyuzx"
            String md5Rtn = MD5Util.encode(nt_data + sign + md5Key)
            if (!md5Rtn.equalsIgnoreCase(md5Sign)) {
                return "SignError"
            }

            nt_data = QuickEncrypt.decode(nt_data, ckey)

            StringReader sr = new StringReader(nt_data);
            InputSource is = new InputSource(sr);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(is);

            String is_test = document.getElementsByTagName("is_test").item(0).getFirstChild().getNodeValue()
            String channel = document.getElementsByTagName("channel").item(0).getFirstChild().getNodeValue()
            String channel_uid = document.getElementsByTagName("channel_uid").item(0).getFirstChild().getNodeValue()
            String game_order = document.getElementsByTagName("game_order").item(0).getFirstChild().getNodeValue()
            String order_no = document.getElementsByTagName("order_no").item(0).getFirstChild().getNodeValue()
            String pay_time = document.getElementsByTagName("pay_time").item(0).getFirstChild().getNodeValue()
            String amount = document.getElementsByTagName("amount").item(0).getFirstChild().getNodeValue()
            String status = document.getElementsByTagName("status").item(0).getFirstChild().getNodeValue()
            String extras_params = document.getElementsByTagName("extras_params").item(0).getFirstChild().getNodeValue()

            String[] fields = extras_params.split("\\|")

            if (!status.equalsIgnoreCase("0")) {
                return "FAILED"
            } else {
                // 验证签名
                ReceiptBean receiptBean = new ReceiptBean()

                receiptBean.tid = order_no
                receiptBean.uid = Long.parseLong(fields[0])
                receiptBean.zone = Integer.parseInt(fields[1])
                receiptBean.time = new Date()
                receiptBean.source = "Quick"
                receiptBean.sourceUser = channel_uid
                receiptBean.rmb = Float.parseFloat(amount) * 100
                receiptBean.channel = "1"
//                receiptBean.bundle =

                try {
                    receiptBean = receiptBeanRepository.insert(receiptBean)
                } catch (Exception ex) {
                    log.error("重复单据[" + receiptBean.tid + "], reason:" + ex.message)
                    return "SUCCESS"
                }

                dao.addCharge(receiptBean.rmb)
                // 通知具体服务器刷新充值
                if (!RemoteService.getInstance().notifyPay(receiptBean.zone, receiptBean.uid)) {
                    log.warn("---- 玩家(" + receiptBean.uid + ")的充值没有通知成功，重新登录即可恢复.")
                }
            }
        } catch (Exception ex) {
//            log.error("quick支付错误:", ex)
            return "FAILED:" + ex.getMessage()
        }

        return "SUCCESS"
    }

    @ApiOperation(value = "云顶支付接口")
    @RequestMapping(value = "/service/ydPay", method = RequestMethod.POST)
    @ResponseBody
    PayResult ydPay(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap()
//		String yd_order_id = ""
//		String app_order_id = ""
        String iap_order_id = ""

        int yd_user_id = 0
        String app_role_id = ""
        String server_id = ""
        int price = 0
        int pay_time = 0
//		int create_time
        String goods_code = ""
        String bundle = ""
        int pay_channel = 0
        String sign = ""

        List<String> names = new ArrayList<>()

        // 根据传入的参数，确定是订单生成，还是下发代币
        for (String key : params.keySet()) {
            String[] values = params.get(key)
            if (values.length <= 0) {
                continue
            }

            String val = values[0]
            log.warn("key[" + key + "]  value[" + val + "].")

            switch (key.toLowerCase()) {
                case "app_id":
                    names.add(val)
                    break
                case "yd_order_id":
//					yd_order_id = val
                    names.add(val)
                    break
                case "app_order_id":
//					app_order_id = val
                    names.add(val)
                    break
                case "iap_order_id":
                    iap_order_id = val
                    names.add(val)
                    break
                case "yd_user_id":
                    yd_user_id = Integer.parseInt(val)
                    names.add(val)
                    break
                case "app_role_id":
                    app_role_id = val
                    names.add(val)
                    break
                case "server_id":
                    server_id = val
                    names.add(val)
                    break
                case "coin":
                    names.add(val)
                    break
                case "price":
                    price = (int)(Float.parseFloat(val) * 100)
                    names.add(val)
                    break
                case "pay_time":
                    pay_time = Integer.parseInt(val)
                    names.add(val)
                    break
                case "goods_code":
                    goods_code = val
                    names.add(val)
                    break
                case "extend_data":
                    String[] fields = val.split("-")
                    bundle = fields[0]
                    break
                case "pay_channel":
                    pay_channel = Integer.parseInt(val)
                    names.add(val)
                    break
                case "sign":
                    sign = val
                    break
                default:
                    break
            }
        }

        if (params.containsKey("create_time")) { // 订单生成

        }
        else if (params.containsKey("pay_time")) { // 下发代币
            BundleBean bb = readonly.getAllBundles().getOrDefault(bundle.trim(), null)
            if (bb != null) {
                names.add(bb.pay)
            }

            String msg = String.join("", names)
            if (!sign.equalsIgnoreCase(MD5Util.encode(msg))) {
                log.error("比较签名失败, 参数[" + msg + "].")
                return new PayResult(0, "sign error")
            }

//            ProductBean productBean = productBeanRepository.findByProduct(goods_code)
//            if (productBean == null || productBean.rmb != price) {
//                return new PayResult(1, "goods_code incompatible with price")
//            }

            ReceiptBean receiptBean = new ReceiptBean()

            receiptBean.tid = iap_order_id
            receiptBean.uid = Long.parseLong(app_role_id)
            receiptBean.zone = Integer.parseInt(server_id)
            receiptBean.time = new Date()
//            receiptBean.ts = pay_time
            receiptBean.source = "云顶"
            receiptBean.sourceUser = "" + yd_user_id
            receiptBean.good = goods_code
            receiptBean.rmb = price
            receiptBean.channel = pay_channel
            receiptBean.bundle = bundle

            try {
                receiptBean = receiptBeanRepository.insert(receiptBean)
            } catch (Exception ex) {
                log.error("重复单据[" + receiptBean.tid + "], reason:" + ex.message)
                return new PayResult(1, "duplicated iap transaction id.")
            }

            dao.addCharge(receiptBean.rmb)
            // 通知具体服务器刷新充值
            if (!RemoteService.getInstance().notifyPay(receiptBean.zone, receiptBean.uid)) {
                log.warn("---- 玩家(" + receiptBean.uid + ")的充值没有通知成功，重新登录即可恢复.")
            }
        }

        return new PayResult(1, "ok")
    }

    @ApiOperation(value = "云顶网页支付接口")
    @RequestMapping(value = "/service/ydWebPayNo", method = RequestMethod.POST)
    @ResponseBody
    PayResult ydWebPayNo(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap()

        String server_id = params.getOrDefault("server_id", "1")
        String app_role_id = params.getOrDefault("app_role_id", "1")
        PayResult pr = new PayResult()
        pr.success = 1
        pr.desc = server_id + app_role_id + (System.currentTimeMillis()/1000 as int)
        return pr
    }

    @ApiOperation(value = "云顶网页支付接口")
    @RequestMapping(value = "/service/ydWebPay", method = RequestMethod.POST)
    @ResponseBody
    PayResult ydWebPay(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap()
        String iap_order_id = ""

        int yd_user_id = 0
        String app_role_id = ""
        String server_id = ""
        int price = 0
        String goods_code = ""
        String bundle = "web"
        int pay_channel = 0
        String sign = ""

        List<String> names = new ArrayList<>()

        // 根据传入的参数，确定是订单生成，还是下发代币
        for (String key : params.keySet()) {
            String[] values = params.get(key)
            if (values.length <= 0) {
                continue
            }

            String val = values[0]
            log.warn("key[" + key + "]  value[" + val + "].")

            switch (key.toLowerCase()) {
                case "app_id":
                    names.add(val)
                    break
                case "yd_order_id":
//					yd_order_id = val
                    names.add(val)
                    break
                case "app_order_id":
//					app_order_id = val
                    names.add(val)
                    break
                case "iap_order_id":
                    iap_order_id = val
                    names.add(val)
                    break
                case "yd_user_id":
                    yd_user_id = Integer.parseInt(val)
                    names.add(val)
                    break
                case "app_role_id":
                    app_role_id = val
                    names.add(val)
                    break
                case "server_id":
                    server_id = val
                    names.add(val)
                    break
                case "coin":
                    names.add(val)
                    break
                case "price":
                    price = (int)(Float.parseFloat(val) * 100)
                    names.add(val)
                    break
                case "pay_time":
                    names.add(val)
                    break
                case "goods_code":
                    goods_code = val
                    names.add(val)
                    break
                case "pay_channel":
                    pay_channel = Integer.parseInt(val)
                    names.add(val)
                    break
                case "sign":
                    sign = val
                    break
                default:
                    break
            }
        }

        if (params.containsKey("pay_time")) { // 下发代币
//            BundleBean bb = readonly.getAllBundles().getOrDefault(bundle.trim(), null)
//            if (bb != null) {
//                names.add(bb.pay)
//            }
            names.add("uZ0kg1Hn5EZ4my0qWqUvpZ0G6J06Mro5")

            String msg = String.join("", names)
            if (!sign.equalsIgnoreCase(MD5Util.encode(msg))) {
                log.error("比较签名失败, 参数[" + msg + "].")
                return new PayResult(0, "sign error")
            }

//            ProductBean productBean = productBeanRepository.findByProduct(goods_code)
//            if (productBean == null || productBean.rmb != price) {
//                return new PayResult(1, "goods_code incompatible with price")
//            }

            ReceiptBean receiptBean = new ReceiptBean()

            receiptBean.tid = iap_order_id
            receiptBean.uid = Long.parseLong(app_role_id)
            receiptBean.zone = Integer.parseInt(server_id)
            receiptBean.time = new Date()
            receiptBean.source = "云顶网页"
            receiptBean.sourceUser = "" + yd_user_id
            receiptBean.good = goods_code
            receiptBean.rmb = price
            receiptBean.channel = pay_channel
            receiptBean.bundle = bundle
            receiptBean.buff = readonly.getWebBuff();

            try {
                receiptBean = receiptBeanRepository.insert(receiptBean)
            } catch (Exception ex) {
                log.error("重复单据[" + receiptBean.tid + "], reason:" + ex.message)
                return new PayResult(1, "duplicated iap transaction id.")
            }

            dao.addCharge(receiptBean.rmb)
            // 通知具体服务器刷新充值
            if (!RemoteService.getInstance().notifyPay(receiptBean.zone, receiptBean.uid)) {
                log.warn("---- 玩家(" + receiptBean.uid + ")的充值没有通知成功，重新登录即可恢复.")
            }
        }

        return new PayResult(1, "ok")
    }
}

