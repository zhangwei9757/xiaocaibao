package com.tumei.game.protos.charge;

import com.tumei.centermodel.ReceiptBean;
import com.tumei.common.DaoGame;
import com.tumei.common.utils.HttpUtils;
import com.tumei.common.utils.JsonUtil;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 测试充值协议
 */
@Component
public class RequestCharge extends BaseProtocol {
	public int seq;

	/**
	 * 分
	 */
	public int rmb;

	/**
	 * 单据
	 */
	public String receipt;

	/**
	 * 0: 不需要苹果认证
	 * 1: 是sandbox;
	 * > 1: 非sandbox;
	 */
	public int sandbox;

	class ReturnCharge extends BaseProtocol {
		public int seq;
		/**
		 * 获得多少钻石
		 */
		public String result = "";
	}

	public static class AppStoreDetail {
		public String bid; // bundle_id;
		public String transaction_id; // 唯一单据
		public String item_id;
		public String product_id; // sku

		@Override
		public String toString() {
			return "AppStoreDetail{" + "bid='" + bid + '\'' + ", transaction_id='" + transaction_id + '\'' + ", item_id='" + item_id + '\'' + ", product_id='" + product_id + '\'' + '}';
		}
	}

	public static class AppStoreResp {
		public int status;
		public AppStoreDetail receipt;

		@Override
		public String toString() {
			return "AppStoreResp{" + "status=" + status + ", receipt=" + receipt + '}';
		}
	}

	// 向苹果服务器发送认证请求
	private void verifyReceipt(GameUser user, String receipt, ReturnCharge rc) {
		try {
			String r = new String(org.springframework.security.crypto.codec.Base64.decode(receipt.getBytes()));

			boolean sandbox = false;
			int pos = r.indexOf("environment");
			if (pos != -1) {
				int begin = r.indexOf("\"", pos + 12) + 1;
				int end = r.indexOf("\"", begin);
				if (r.substring(begin, end).equalsIgnoreCase("Sandbox")) {
					sandbox = true;
				}
			}

			String url = "https://sandbox.itunes.apple.com/verifyReceipt";
			if (!sandbox) {
				url = "https://buy.itunes.apple.com/verifyReceipt";
			}

			Map<String, String> tmp = new HashMap<>();
			tmp.put("receipt-data", receipt);
			String data = JsonUtil.getMapper().writeValueAsString(tmp);
			String rtn = HttpUtils.sentPost(url, data);
			AppStoreResp resp = JsonUtil.Unmarshal(rtn, AppStoreResp.class);

			if (resp.status != 0) {
				rc.result = "支付单据错误";
				return;
			}

			user.info("收到验证结果:" + resp);

			int rmb = DaoGame.getInstance().findModeByProduct(resp.receipt.product_id);
			if (rmb == 0) {
				rc.result = "商品(" + resp.receipt.product_id + ") 没有在服务器注册.";
				return;
			}

//			if (!Readonly.getInstance().containsBundle(resp.receipt.bid)) {
//				rc.result = "未经认证的包无法进行内购.";
//				return;
//			}

			ReceiptBean receiptBean = new ReceiptBean();

			receiptBean.tid = resp.receipt.transaction_id;
			receiptBean.uid = user.getUid();
			receiptBean.zone = GameServer.getInstance().getZone();
			receiptBean.time = new Date();
			receiptBean.source = "OpenIAB";
			receiptBean.sourceUser = "";
			receiptBean.good = resp.receipt.product_id;
			receiptBean.rmb = rmb;
			receiptBean.channel = 998;
			receiptBean.bundle = resp.receipt.bid;
			receiptBean.status = 1;
			receiptBean.sandbox = sandbox ? 1 : 0;

			user.charge(receiptBean);

			// 保存到数据库之后，立刻发送给玩家
			if (DaoGame.getInstance().saveReceipt(receiptBean) == -1) {
				rc.result = "重复的苹果单据";
				return;
			}
		} catch (Exception ex) {
			user.error("向苹果服务器认证支付单据错误:" + ex.getMessage());
		}
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnCharge rtn = new ReturnCharge();
		rtn.seq = seq;

		if (sandbox != 0) {
			verifyReceipt(user, receipt, rtn);
			user.send(rtn);
		}
		else if (user.getGmlevel() > 0) {
			ReceiptBean receiptBean = new ReceiptBean();

			receiptBean.tid = "FAKE" + RandomUtil.getRandom() + "_" + System.currentTimeMillis();
			receiptBean.uid = user.getUid();
			receiptBean.zone = GameServer.getInstance().getZone();
			receiptBean.time = new Date();
			receiptBean.source = "FAKE";
			receiptBean.sourceUser = "";
			receiptBean.good = rmb + "";
			receiptBean.rmb = rmb;
			receiptBean.channel = 999;
			receiptBean.bundle = "";
			receiptBean.status = 1;
			receiptBean.sandbox = 1;

			user.charge(receiptBean);

			// 保存到数据库之后，立刻发送给玩家
			if (DaoGame.getInstance().saveReceipt(receiptBean) == -1) {
				rtn.result = "重复的苹果单据";
				user.send(rtn);
				return;
			}
			user.send(rtn);
		} else {
			rtn.result = "错误的支付请求";
			user.send(rtn);
		}
	}
}
