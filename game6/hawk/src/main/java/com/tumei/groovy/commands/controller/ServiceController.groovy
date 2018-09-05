package com.tumei.controller

import com.tumei.centermodel.AccountBean
import com.tumei.centermodel.ReceiptBean
import com.tumei.centermodel.ReceiptBeanRepository
import com.tumei.centermodel.UserBean
import com.tumei.common.DaoUtils
import com.tumei.common.RemoteService
import com.tumei.common.utils.MD5Util
import com.tumei.common.utils.QuickEncrypt
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

/**
 * Created by Administrator on 2016/12/16 0016.
 *
 * 内网服务访问的接口
 */
class ServiceController {
    private static final Log log = LogFactory.getLog(this.class)

    private static final String quick_ckey = "45658799209587791571919162719543"

    private static final String quick_md5 = "a0vznqv0uvzjdjdlpoujxqq83dkwdjln"

    @Autowired
    private DaoUtils dao


    @Autowired
    @Qualifier(value = "centerTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    private ReceiptBeanRepository receiptBeanRepository


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
                receiptBean.rmb = (int) (Float.parseFloat(amount) * 100)
                receiptBean.channel = 1
                receiptBean.bundle = fields[2]
                receiptBean.sandbox = Integer.parseInt(is_test)

                accid = (long) (receiptBean.uid / 1000)
                receiptBean.accid = (int) accid
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

