package com.tumei.controller

import com.google.common.base.Strings
import com.tumei.centermodel.*
import com.tumei.common.DaoCenter
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.common.service.ServiceRouter
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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.w3c.dom.Document
import org.xml.sax.InputSource

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.ParseException

/**
 * Created by Administrator on 2016/12/16 0016.
 *
 * 内网服务访问的接口
 */
class ServiceController {
    private static final Log log = LogFactory.getLog(this.class)

    // 7 days 最大的回调间歇
    private static final long MAX_CALLBACK_AGE_MILLIS = 7 * 24 * 60 * 60 * 1000

    // 广告奖励token ios专用
    private static final String ad_token_ios = "egWPpv4dgD+Q3P+6/+0dk1hjtaYhKNs+FMtRP3b3Epg="

    // 广告奖励token android专用
    private static final String ad_token_android = "pBFvStU02ud54nm04AVG3psxNAA4cWY1ZN/N6f/lyJ0="

    private static final String quick_ckey = "45658799209587791571919162719543"

    private static final String quick_md5 = "a0vznqv0uvzjdjdlpoujxqq83dkwdjln"

    @Autowired
    private DaoCenter dao

    @Autowired
    private Readonly readonly

    @Autowired
    private AccountBeanRepository accountBeanRepository
    @Autowired
    @Qualifier(value = "centerTemplate")
    private MongoTemplate mongoTemplate

    @Autowired
    private RemoteService remoteService

    @Autowired
    private ServiceRouter serviceRouter

    protected long getTransactionMillis(String tid) throws ParseException {
        final int transactionMillisIndex = tid.lastIndexOf(":") + 1
        try {
            if (transactionMillisIndex > 0 && transactionMillisIndex < tid.length()) {
                return Long.parseLong(tid.substring(transactionMillisIndex))
            } else {
                throw new ParseException("No timestamp in transaction ID", transactionMillisIndex)
            }
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid transaction ID timestamp", transactionMillisIndex)
        }
    }


    /**
     * 是否在回调窗口内
     * @param tid
     * @return
     */
    protected boolean isRecentTransaction(String tid) {
        boolean isRecent = false
        try {
            final long minCallbackAgeMillis = System.currentTimeMillis() - MAX_CALLBACK_AGE_MILLIS
            final long transactionMillis = getTransactionMillis(tid)
            isRecent = (transactionMillis > minCallbackAgeMillis)
        } catch (ParseException exception) {

        }
        return isRecent
    }

    protected boolean isDigestValid(String transactionId, String secretKey, String verificationDigest) throws NoSuchAlgorithmException {
        if (Strings.isNullOrEmpty(secretKey)) {
            return true
        }

        return createSecurityDigest(transactionId, secretKey) == verificationDigest;
    }

    protected boolean isNewTransaction(String transactionId) {
        // TODO if transactionId is new, store the transactionId with its associated transactionMillis
        // For %etxid%, extract unique event ID from etxid, which is the string before ":", and use it to identify unique ad events

        return true;
    }

    protected String createSecurityDigest(String transactionId, String secretKey) throws NoSuchAlgorithmException {
        final String verificationString = secretKey + ":" + transactionId;
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return toHexString(
                messageDigest.digest(
                        messageDigest.digest(
                                verificationString.getBytes(Charset.forName("US-ASCII")))));
    }

    protected String toHexString(byte[] bytes) {
        final StringBuffer hexStringBuffer = new StringBuffer();
        for (final byte byt : bytes) {
            hexStringBuffer.append(
                    Integer.toString((byt & 0xff) + 0x100, 16)
                            .substring(1));
        }
        return hexStringBuffer.toString();
    }

    @ApiOperation(value = "广告奖励")
    @RequestMapping(value = "/service/adsreward", method = RequestMethod.GET)
    @ApiImplicitParams([
            @ApiImplicitParam(name = "uid", value = "玩家id,mode: 1是ios,2是安卓", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "tid", value = "交易id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "digest", value = "验证token", required = true, dataType = "String", paramType = "query"),
    ])
    @ResponseBody
    String adsReward(HttpServletRequest request) {
        String tag = request.getParameter("uid")
        String tid = request.getParameter("tid")
        String digest = request.getParameter("digest")
        log.info(String.format("tag:%s, tid:%s, digest:%s.", tag, tid, digest))

        String[] fields = tag.split(",")
        long uid = Long.parseLong(fields[0])
        String mode = fields[1]

        String token = ad_token_ios
        if (mode == "2") {
            token = ad_token_android
        }

        if (isRecentTransaction(tid) && isDigestValid(tid, token, digest) && isNewTransaction(tid)) {
            // 发送奖励
            remoteService.sendAwardMail(uid, "广告奖励", "观看视频广告奖励", "1,100000,2,10")
        }

        return ""
    }

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

        ServerInfo(int id, String name) {
            server_id = id;
            server_name = name;
        }
    }

    class ServerList {
        public int success;
        public String desc = "";
        public List<ServerInfo> server = new ArrayList<>();
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
            AccountBean ub = accountBeanRepository.findById((uid / 1000) as long);
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

    @Autowired
    private ReceiptBeanRepository receiptBeanRepository

    @ApiOperation(value = "Quick支付接口")
    @RequestMapping(value = "/service/qPay", method = RequestMethod.POST)
    @ResponseBody String qPay(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap()

        int rmb = 0
        long accid = 0

        String nt_data = ""
        String sign = ""
        String md5Sign = ""


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
            String md5Rtn = MD5Util.encode(nt_data + sign + quick_md5)
            if (!md5Rtn.equalsIgnoreCase(md5Sign)) {
                return "SignError"
            }

            nt_data = QuickEncrypt.decode(nt_data, quick_ckey)

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
                receiptBean.source = "quick"
                receiptBean.sourceUser = channel_uid
                receiptBean.rmb = Float.parseFloat(amount) * 100
                receiptBean.channel = 1
                receiptBean.bundle = fields[2]
                receiptBean.sandbox = Integer.parseInt(is_test)

                accid = receiptBean.uid / 1000
                receiptBean.accid = accid
                rmb = receiptBean.rmb

                try {
                    receiptBean = receiptBeanRepository.insert(receiptBean)
                } catch (Exception ex) {
                    log.error("重复单据[" + receiptBean.tid + "], reason:" + ex.message)
                    return "SUCCESS"
                }

                dao.addCharge(receiptBean.uid, receiptBean.rmb)
                // 通知具体服务器刷新充值
                if (!RemoteService.getInstance().notifyPay(receiptBean.zone, receiptBean.uid)) {
                    log.warn("---- 玩家(" + receiptBean.uid + ")的充值没有通知成功，重新登录即可恢复.")
                }
            }
        } catch (Exception ex) {
//            log.error("quick支付错误:", ex)
            return "FAILED:" + ex.getMessage()
        }

        // 针对该玩家记录充值数据
        mongoTemplate.findAndModify(Query.query(Criteria.where("id").is(accid)),
                new Update().inc("charge", rmb).inc("chargecount", 1), AccountBean.class)

        return "SUCCESS"
    }
}

