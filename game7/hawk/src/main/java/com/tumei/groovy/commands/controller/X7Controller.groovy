package com.tumei.controller

import com.tumei.centermodel.AccountBean
import com.tumei.centermodel.ReceiptBean
import com.tumei.centermodel.ReceiptBeanRepository
import com.tumei.centermodel.ServerBeanRepository
import com.tumei.common.DaoCenter
import com.tumei.common.RemoteService
import com.tumei.common.utils.SignHelper
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
import org.springframework.util.Base64Utils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

/**
 * 小七服务器订单生成，处理支付通知
 *
 */
class X7Controller {
    private static final Log log = LogFactory.getLog(X7Controller.class)

    private static final String PUBKEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC5H1knaFD2JR0LJfRhik+E+eKBUlTcGIQPsacmCCppw0E/qoynVVWPauxCfNEss143GJRELixSDdrASC2WsbVKV+UmrMEHax6uF7avp35cexLNnNhQ9GVit8l+NMOnoBbsOTLlsNwUTjFf47i7IBM+tnrCbOsEGbXDrtPHUXtezQIDAQAB"

//    private static final String PUBKEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+zCNgOlIjhbsEhrGN7De2uYcfpwNmmbS6HYYI5KljuYNua4v7ZsQx5gTnJCZ+aaBqAIRxM+5glXeBHIwJTKLRvCxC6aD5Mz5cbbvIOrEghyozjNbM6G718DvyxD5+vQ5c0df6IbJHIZ+AezHPdiOJJjC+tfMF3HdX+Ng/VT80LwIDAQAB";

    private static final String APPKEY = "e1a91ca0a00a53eb91b263008134d593"

    @Autowired
    private DaoCenter daoUtils

    @Autowired
    private ReceiptBeanRepository receiptBeanRepository

    @Autowired
    @Qualifier(value = "centerTemplate")
    private MongoTemplate mongoTemplate



    private String buildHttpQueryNoEncode(Map<String, String> data) throws UnsupportedEncodingException {
        String builder = new String();
        for (Map.Entry<String, String> pair : data.entrySet()) {
            builder += pair.getKey()+ "=" + pair.getValue() + "&";
        }
        return builder.substring(0, builder.length() - 1);
    }

    private Map<String, String> decodeHttpQueryNoDecode(String httpQuery) throws UnsupportedEncodingException
    {
        Map<String, String> map = new TreeMap<String, String>();

        for(String s: httpQuery.split("&")) {
            String []pair = s.split("=");
            map.put(pair[0], pair[1]);
        }
        return map;
    }

    @ApiOperation(value = "小7接收支付的接口", notes = "")
    @RequestMapping(value = "/abp/x7notify", method = RequestMethod.POST)
    @ResponseBody
    String x7notify(HttpServletRequest request) {
        List<String> keys = request.getParameterNames().toList()
        Arrays.sort(keys);

        Map<String, String> map = new TreeMap<String, String>();
        String tempStr = "";
        for(String key : keys){
            String val = request.getParameter(key)
            log.info("[" + key + "]: " + val)
//            val = URLDecoder.decode(val,"UTF-8")
            map.put(key, val)
            log.info("[" + key + "]: " + val)
        }

        String sign = map.get("sign_data");
        map.remove("sign_data");

        try
        {
            String sourceStr = buildHttpQueryNoEncode(map);
            //验签
            if (sign == null || !SignHelper.verify(sourceStr, sign, PUBKEY, 2)) {
                return "failed:sign_data_verify_failed"
            }
            //解密
            String decryptData = new String(SignHelper.decrypt(Base64Utils.decode(map.get("encryp_data").getBytes()), PUBKEY));
            /*************************************************************
             * 下面这里将会返回是一个包含game_orderid、guid、pay_price的双列集合
             * {game_orderid=xxxx, guid=xxxx, pay_price=xxxxx}
             *************************************************************/
            Map<String, String> decryptMap = decodeHttpQueryNoDecode(decryptData);

            String game_orderid = decryptMap.getOrDefault("game_orderid", null)
            String guid = decryptMap.getOrDefault("guid", null)

            double pay_price = Double.parseDouble(decryptMap.getOrDefault("pay_price", "0"))
            int zone = Integer.parseInt(map.getOrDefault("game_area", "0"))
            long uid = Long.parseLong(map.getOrDefault("game_role_id", "0"))
            String receipt = map.getOrDefault("xiao7_goid", null)

            String bundle = map.getOrDefault("extends_info_data", "")

            /******************************************************
             * 这里需要判断是否存在game_orderid、pay_price、guid三个值。
             ******************************************************/
            if (game_orderid == null || pay_price <= 0 || guid == null || receipt == null) {
                return "encryp_data_decrypt_failed"
            }
            /*********************************************************************
             * 对比一下解出来的订单号与传递过来的订单号是否一致。这里同时要比较一下当前订单号是否是属于当前小7渠道。
             ********************************************************************/
            if(game_orderid != map.get("game_orderid")){
                return "failed:game_orderid error"
            }

            ReceiptBean receiptBean = new ReceiptBean()
            receiptBean.tid = receipt
            receiptBean.uid = uid
            receiptBean.zone = zone
            receiptBean.time = new Date()
            receiptBean.source = "x7"
            receiptBean.sourceUser = guid
            receiptBean.good = ""
            receiptBean.rmb = (int)(pay_price * 100)
            receiptBean.channel = 810
            receiptBean.bundle = bundle

            long accid = uid / 1000
            receiptBean.accid = accid

            try {
                receiptBean = receiptBeanRepository.insert(receiptBean)
            } catch (Exception ex) {
                log.error("重复单据:" + receiptBean.tid + ", id:" + receiptBean.uid)
                return "success" // 重复的没有错误，直接提示成功，不要再发了
            }

            if (!RemoteService.instance.notifyPay(zone, uid)) {
                log.warn("--- 玩家(" + uid + ") 的充值通知失败，重新登录后可自动刷新")
            }

            mongoTemplate.findAndModify(Query.query(Criteria.where("id").is(accid)),
            new Update().inc("charge", receiptBean.rmb).inc("chargecount", 1), AccountBean.class)
            return "success"
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "failed:order error"
    }
}


