package com.tumei.controller

import com.google.common.base.Strings
import com.tumei.centermodel.*
import com.tumei.common.DaoCenter
import com.tumei.common.RemoteService
import com.tumei.common.utils.HttpUtils
import com.tumei.common.utils.JsonUtil
import com.tumei.common.utils.SignHelper
import com.tumei.common.utils.SignUtils
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.apache.catalina.servlet4preview.http.HttpServletRequest
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.json.simple.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Anysdk的登录回调与支付接口
 * <p>
 * /logon_any: 登录，由anysdk发送来的请求
 * /pay_any: 支付，由anysdk发送来通知
 */
class AibeiController {
    private static final Log log = LogFactory.getLog(AibeiController.class)

    @Autowired
    private DaoCenter daoUtils

    @Autowired
    private AibeiParamRepository aibeiParamRepository

    @Autowired
    private ReceiptBeanRepository receiptBeanRepository

    @Autowired
    private ServerBeanRepository serversBeanRepository

    @Autowired
    @Qualifier(value = "centerTemplate")
    private MongoTemplate mongoTemplate

    class ServerInfo {
        public int server_id;
        public String server_name;
        ServerInfo(int id, String name) {
            server_id = id;
            server_name = name;
        }
    }

    @RequestMapping(value = "/abp/web", method = RequestMethod.GET)
    String webPay(Model model) {
        log.info("支付...");

        List<ServerInfo> servers = new ArrayList<>();
        try {
            List<ServerBean> ssbs = serversBeanRepository.findAll();
            for (def sb : ssbs) {
                servers.add(new ServerInfo(sb.id, sb.name));
            }
        } catch (Exception ex) {
            return "/error"
        }
        String servers_str = JsonUtil.Marshal(servers);

        model.addAttribute("servers", servers_str);

        return "pay";
    }

    @ApiOperation(value = "爱贝H5，通过客户端传递的数据制作单据", notes = "")
    @ApiImplicitParams([
            @ApiImplicitParam(name = "index", value = "爱贝商品序号", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "price", value = "价格", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "order", value = "客户端订单", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "bundle", value = "包名", required = true, dataType = "String", paramType = "query")
    ])
    @RequestMapping(value = "/abp/get", method = RequestMethod.GET)
    @ResponseBody
    String get(HttpServletRequest request) {
        int index = Integer.parseInt(request.getParameter("index"))
        int price = Integer.parseInt(request.getParameter("price"))
        String uid = request.getParameter("uid")
        String order = request.getParameter("order")
        String bundle = request.getParameter("bundle")

        AibeiParam bpb = aibeiParamRepository.findByBundle(bundle)

        if (bpb != null) {
            String tid = Order.CheckSign(bpb, index, order, price, uid, order + "-" + bundle, null)
            if (!Strings.isNullOrEmpty(tid)) {
                String url = Order.H5orPCpay(bpb, tid)
                log.info("返回url:" + url)
                return url
            }
        }
        return null
    }


    @ApiOperation(value = "爱贝H5 支付回调")
    @RequestMapping(value = "/abp/notify", method = RequestMethod.POST)
    @ResponseBody
    String aibeih5(HttpServletRequest request) {
        try {
            String transdata = request.getParameter("transdata")
            String sign = request.getParameter("sign")
            String signtype = request.getParameter("signtype")

            return doProcess(transdata, sign, signtype)
        } catch (Exception e) {
            log.error("支付回调错误:", e)
        }

        return "FAILURE"
    }

    /**
     * 爱贝返回单据处理, 分发物品
     *
     * @param transdata
     * @param sign
     * @param signtype
     * @return
     */
    private String doProcess(String transdata, String sign, String signtype) {
        try {
            log.info("transdata:" + transdata)
            log.info("sign:" + sign)
            log.info("signtype:" + signtype)

            if (signtype != null) {
                Map<String, Object> map = JsonUtil.getMapper().readValue(transdata, Map.class)
                int result = (Integer) map.get("result")
                if (result == 0) {
                    String ext = map.get("cpprivate").toString()
                    String[] fields = ext.split("-")
                    AibeiParam bpb = aibeiParamRepository.findByBundle(fields[2])

                    /*
                     * 调用验签接口
                     *
                     * 主要 目的 确定 收到的数据是我们 发的数据，是没有被非法改动的
                     */
                    if (bpb != null && SignHelper.verify(transdata, sign, bpb.getPlatkey(), 1)) {
                        // 1. 订单号
                        String receipt = map.get("transid").toString()
                        Double money = (Double) map.get("money")
                        long id = Long.parseLong(map.get("appuserid").toString())
                        int waresid = (Integer) map.get("waresid")
//						Integer payType = (Integer)map.get("paytype")

                        int zone = Integer.parseInt(fields[0])

                        ReceiptBean receiptBean = new ReceiptBean()

                        receiptBean.tid = receipt
                        receiptBean.uid = id
                        receiptBean.zone = zone
                        receiptBean.time = new Date()
//            receiptBean.ts = pay_time
                        receiptBean.source = "爱贝"
                        receiptBean.sourceUser = ""
                        receiptBean.good = "" + waresid
                        receiptBean.rmb = (int) (money * 100)
                        receiptBean.channel = 800
                        receiptBean.bundle = fields[2]
                        long accid = receiptBean.uid / 1000
                        receiptBean.accid = (int)accid

                        try {
                            receiptBean = receiptBeanRepository.insert(receiptBean)

                            // 增加统计
                            daoUtils.addCharge(id, receiptBean.rmb)
                        } catch (Exception ex) {
                            log.error("重复单据[" + receiptBean.tid + "],人物id:" + id + " 异常:" + ex.getMessage())
                            return "SUCCESS" // 重复单据不要再发送，返回成功
                        }

                        // 通知具体服务器刷新充值
                        if (!RemoteService.getInstance().notifyPay(receiptBean.zone, receiptBean.uid)) {
                            log.warn("---- 玩家(" + receiptBean.uid + ")的充值没有通知成功，重新登录即可恢复.")
                        }

                        // 针对该玩家记录充值数据
                        mongoTemplate.findAndModify(Query.query(Criteria.where("id").is(accid)),
                                new Update().inc("charge", receiptBean.rmb).inc("chargecount", 1), AccountBean.class)

                        return "SUCCESS"
                    } else {
                        log.error("包名对应的参数不存在，或者签名校验失败, 包名:" + fields[2])
                    }
                }
            }
        } catch (Exception e) {
            log.error("支付回调错误:", e)
        }

        return "FAILURE"
    }
}

class Order {
    private static Log logger = LogFactory.getLog(this.class)
    /**
     * 组装请求参数
     *
     * @param waresid 商品编号
     * @param price 商品价格
     * @param cporderid 商户订单号
     * @param appuserid 用户编号
     * @param cpprivateinfo 商户私有信息
     * @param notifyurl 支付结果通知地址
     * @return 返回组装好的用于post的请求数据
     * .................
     */
    static String ReqData(AibeiParam bpb, int waresid, String cporderid, float price, String appuserid, String cpprivateinfo, String notifyurl) {

        String json
        json = "appid:"
        json += bpb.getAppid()
        json += " userid:"
        json += appuserid
        json += " waresid:"
        json += waresid
        json += "cporderid:"
        json += cporderid
        logger.debug("json=" + json)

        JSONObject jsonObject = new JSONObject()
        jsonObject.put("appid", bpb.getAppid())
        jsonObject.put("waresid", waresid)
        jsonObject.put("cporderid", cporderid)
        jsonObject.put("currency", "RMB")
        jsonObject.put("appuserid", appuserid)

        /*
         * 当使用的是 开放价格策略的时候 price的值是 程序自己 设定的价格，使用其他的计费策略的时候
         * price 不用传值
         * */
        jsonObject.put("price", price)

        if (!Strings.isNullOrEmpty(cpprivateinfo)) {
            jsonObject.put("cpprivateinfo", cpprivateinfo)
        }

        if (!Strings.isNullOrEmpty(notifyurl)) {
            /*
             * 如果此处不传同步地址，则是以后台传的为准。
             * */
            jsonObject.put("notifyurl", notifyurl)
        }

        String content = jsonObject.toString();// 组装成 json格式数据
        // 调用签名函数      重点注意： 请一定要阅读  sdk 包中的爱贝AndroidSDK3.4.4\03-接入必看-服务端接口说明及范例\爱贝服务端接入指南及示例0311\IApppayCpSyncForJava \接入必看.txt
        String sign = SignHelper.sign(content, bpb.getPrivatekey(), 1)
        String data = "transdata=" + content + "&sign=" + sign + "&signtype=RSA";// 组装请求参数
        logger.debug("请求数据:" + data)
        return data
    }

    // 数据验签
    static String CheckSign(AibeiParam bpb, int waresid, String cporderid, float price, String appuserid, String cpprivateinfo, String notifyurl) {

        String reqData = ReqData(bpb, waresid, cporderid, price, appuserid, cpprivateinfo, notifyurl)

        String respData = HttpUtils.sentPost("http://ipay.iapppay.com:9999/payapi/order", reqData, "UTF-8"); // 请求验证服务端
        logger.debug("响应数据：" + respData)

        Map<String, String> resultMap = SignUtils.getParmters(respData)
        String transdata = resultMap.get("transdata")
        String signtype = resultMap.get("signtype"); // "RSA"

        if (signtype != null) {
            try {
                if (SignHelper.verify(transdata, resultMap.get("sign"), bpb.getPlatkey(), 1)) {

                    Map<String, String> map = JsonUtil.getMapper().readValue(resultMap.get("transdata"), Map.class)
                    return map.get("transid")
                }
            } catch (IOException ioe) {
                ioe.printStackTrace()
            }
        }
        return null
    }

    //当客户端上使用H5 的时候下面的示例代码可以有所帮助。

    static String H5orPCpay(AibeiParam bpb, String transid) {
//        String pcurl = "https://web.iapppay.com/pc/exbegpay?"
        String h5url = "https://web.iapppay.com/pay/oldgateway?"
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("transid", transid)
        jsonObject.put("redirecturl", "")
        jsonObject.put("cpurl", "")
        String content = jsonObject.toString()
        String sign = SignHelper.sign(content, bpb.getPrivatekey(), 1)
        String data = "transdata=" + URLEncoder.encode(content) + "&sign=" + URLEncoder.encode(sign) + "&signtype=RSA"
        String url = h5url + data

        logger.info("可以直接在浏览器中访问该链接:" + url)
        return url
    }

//	//	可以右键运行查看效果
//	public static void main(String[] argv) {
//		String transid = CheckSign(1, "cp定义订单号", 0.1f, "玩家帐号id", "cp透传", "强制通知地址")
//		if (Strings.isNullOrEmpty(transid)) {
//			logger.error("下单失败，无法获取单据号!")
//			return
//		}
//
//		H5orPCpay(transid)
//	}

}


