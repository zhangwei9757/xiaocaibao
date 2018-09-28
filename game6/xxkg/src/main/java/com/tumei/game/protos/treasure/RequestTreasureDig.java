package com.tumei.game.protos.treasure;

import com.tumei.GameConfig;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.TreasureBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DailytreasureConf;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 锤子敲 9个 地洞
 *
 */
@Component
public class RequestTreasureDig extends BaseProtocol {
	public int seq;

	class ReturnTreasureDig extends BaseProtocol {
		public int seq;

		public String result = "";

		// 扣除的钻石
		public int gem;

		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnTreasureDig rl = new ReturnTreasureDig();
		rl.seq = seq;

		TreasureBean tb = user.getDao().findTreasure(user.getUid());
		tb.flush();

		int dc = tb.getDigCount();

		if (dc >= GameConfig.getInstance().getTreasure_dig_limit()) {
			rl.result = "今日挖掘次数达到上限";
			user.send(rl);
			return;
		}

		DailytreasureConf dtc = Readonly.getInstance().findDailytreasureConf(tb.getCurrent());
		if (dc > 2) {
			rl.gem = dtc.cost;
		}

		if (rl.gem > 0) {
			PackBean pb = user.getDao().findPack(user.getUid());
			if (!pb.contains(钻石, rl.gem)) {
				rl.result = ErrCode.钻石不足.name();
				user.send(rl);
				return;
			}
			user.payItem(钻石, rl.gem, "挖掘神秘宝藏");
		}

		rl.awards.addAll(user.addItems(tb.dig(), true, "神秘宝藏"));
		VipConf vc = Readonly.getInstance().findVip(user.getVip());
		if (vc.dtdouble != 0) {
			tb.setAwards(rl.awards);
			tb.setDoubleBox(dtc.cost);
		}

		user.send(rl);
	}
}
