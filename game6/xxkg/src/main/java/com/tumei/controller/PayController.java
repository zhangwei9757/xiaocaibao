package com.tumei.controller;

import com.tumei.centermodel.ProductBean;
import com.tumei.centermodel.ProductBeanRepository;
import com.tumei.centermodel.ReceiptBean;
import com.tumei.centermodel.ReceiptBeanRepository;
import com.tumei.common.DaoService;
import com.tumei.common.utils.MD5Util;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/18 0018.
 */
@RestController
public class PayController {
	static final Log log = LogFactory.getLog(PayController.class);

	@Autowired
	private DaoService dao;

	@Autowired
	private GameServer server;

	@Autowired
	private ReceiptBeanRepository receiptBeanRepository;

	@Autowired
	private ProductBeanRepository productBeanRepository;

	class PayResult {
		/**
		 * 1: 成功
		 * 0: 失败
		 */
		public int success;
		public String desc;

		public PayResult() {
		}

		public PayResult(int _success, String _desc) {
			success = _success;
			desc = _desc;
		}
	}

	@ApiOperation(value = "云顶支付接口")
	@RequestMapping(value = "/ydPay", method = RequestMethod.POST)
	public
	@ResponseBody
	PayResult ydPay(HttpServletRequest request) {
		Map<String, String[]> params = request.getParameterMap();
//		String yd_order_id = "";
//		String app_order_id = "";
		String iap_order_id = "";

		int yd_user_id = 0;
		String app_role_id = "";
		String server_id = "";
		int price = 0;
		int pay_time = 0;
//		int create_time;
		String goods_code = "";
		String bundle = "";
		int pay_channel = 0;
		String sign = "";

		List<String> names = new ArrayList<>();

		// 根据传入的参数，确定是订单生成，还是下发代币
		for (String key : params.keySet()) {
			String[] values = params.get(key);
			if (values.length <= 0) {
				continue;
			}

			String val = values[0];
			log.warn("key[" + key + "]  value[" + val + "].");

			switch (key.toLowerCase()) {
				case "app_id":
					names.add(val);
					break;
				case "yd_order_id":
//					yd_order_id = val;
					names.add(val);
					break;
				case "app_order_id":
//					app_order_id = val;
					names.add(val);
					break;
				case "iap_order_id":
					iap_order_id = val;
					names.add(val);
					break;
				case "yd_user_id":
					yd_user_id = Integer.parseInt(val);
					names.add(val);
					break;
				case "app_role_id":
					app_role_id = val;
					names.add(val);
					break;
				case "server_id":
					server_id = val;
					names.add(val);
					break;
				case "coin":
					names.add(val);
					break;
				case "price":
					price = (int)(Float.parseFloat(val) * 100);
					names.add(val);
					break;
				case "pay_time":
					pay_time = Integer.parseInt(val);
					names.add(val);
					break;
				case "goods_code":
					goods_code = val;
					names.add(val);
					break;
				case "extend_data": {
					String[] fields = val.split("-");
					bundle = fields[0];
				}
				break;
				case "pay_channel":
					pay_channel = Integer.parseInt(val);
					names.add(val);
					break;
				case "sign":
					sign = val;
					break;
				default:
					break;
			}
		}

		if (params.containsKey("create_time")) { // 订单生成

		}
		else if (params.containsKey("pay_time")) { // 下发代币
			names.add("pW1qUNSNBVk3O6faiGCy93Wi3uC1l5W5");
			String msg = String.join("", names);
			if (!sign.equalsIgnoreCase(MD5Util.encode(msg))) {
				log.error("比较签名失败, 参数[" + msg + "].");
				return new PayResult(0, "sign error");
			}

			ProductBean productBean = productBeanRepository.findByProduct(goods_code);
			if (productBean == null || productBean.rmb != price) {
				return new PayResult(1, "goods_code incompatible with price");
			}

			ReceiptBean receiptBean = new ReceiptBean();

			receiptBean.tid = iap_order_id;
			receiptBean.uid = Long.parseLong(app_role_id);
			receiptBean.zone = Integer.parseInt(server_id);
			receiptBean.time = new Date();
			receiptBean.ts = pay_time;
			receiptBean.source = "云顶";
			receiptBean.sourceUser = "" + yd_user_id;
			receiptBean.good = goods_code;
			receiptBean.rmb = price;
			receiptBean.channel = pay_channel;
			receiptBean.bundle = bundle;

			// 保存到数据库之后，立刻发送给玩家
			if (charge(receiptBean)) {
				receiptBean.status = 1;
			}

			try {
				receiptBean = receiptBeanRepository.insert(receiptBean);
			} catch (Exception ex) {
				log.error("重复单据[" + receiptBean.tid + "].");
				return new PayResult(1, "duplicated iap transaction id.");
			}
		}

		return new PayResult(1, "ok");
	}


	@ApiOperation(value = "云顶支付接口")
	@RequestMapping(value = "/notifyPay", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query")})
	public String notifyPay(long id) {
		log.warn("+++ 收到玩家(" + id + ")充值提示，检查增加充值.");
		GameUser user = GameServer.getInstance().find(id);
		if (user != null) {
			user.flushCharge();
		}
		return "ok";
	}

	public boolean charge(ReceiptBean receiptBean) {
		GameUser user = server.find(receiptBean.uid);
		if (user != null) {
			user.charge(receiptBean);
			return true;
		}
		return false;
	}
}
