package com.tumei.game.protos.charge;

import com.tumei.game.GameUser;
import com.tumei.model.ChargeBean;
import com.tumei.websocket.WebSocketUser;
import com.tumei.game.protos.structs.ChargeStruct;
import com.tumei.model.RoleBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求充值信息
 */
@Component
public class RequestChargeInfo extends BaseProtocol {
	public int seq;

	class ReturnChargeInfo extends BaseProtocol {
		public int seq;
		public String result = "";
		public int vip;
		public int vipexp;

		/**
		 * 充值内容
		 */
		public List<ChargeStruct> charges = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnChargeInfo rci = new ReturnChargeInfo();
		rci.seq = seq;

		long uid = user.getUid();
		RoleBean rb = user.getDao().findRole(uid);
		rci.vip = rb.getVip();
		rci.vipexp = rb.getVipexp();

		ChargeBean cb = user.getDao().findCharge(uid);

		int[] li = new int[]{600, 3000, 6800, 19800, 32800, 64800};

		for (int i = 0; i < li.length; ++i) {
			int rmb = li[i];
			int gem = rmb / 10;
			int extra = 0;

			float r = 1.0f;
			if (cb.isFirstCharge(rmb)) {
				r = 2.0f;
				extra += gem;
			}

			ChargeStruct cs = new ChargeStruct("", rmb/100, gem, extra, r);
			rci.charges.add(cs);
		}

		// 月卡
		{
			ChargeStruct cs = new ChargeStruct("月卡", 25, 250, 100, 1);
			cs.ts = cb.getMonth();
			rci.charges.add(cs);
		}

		// 大月卡
		{
			ChargeStruct cs = new ChargeStruct("大月卡", 50, 500, 200, 1);
			cs.ts = cb.getBigmonth();
			rci.charges.add(cs);
		}

		// 年卡
		{
			ChargeStruct cs = new ChargeStruct("年卡", 98, 500, 150, 1);
			cs.ts = cb.getYear();
			rci.charges.add(cs);
		}

		user.send(rci);
	}
}
