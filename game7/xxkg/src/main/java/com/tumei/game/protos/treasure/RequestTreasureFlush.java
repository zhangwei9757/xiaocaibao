package com.tumei.game.protos.treasure;

import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.TreasureBean;
import com.tumei.modelconf.DailytreasureConf;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 刷新宝箱
 *
 */
@Component
public class RequestTreasureFlush extends BaseProtocol {
	public int seq;

	class ReturnTreasureFlush extends BaseProtocol {
		public int seq;

		public String result = "";

		// 扣除的钻石
		public int gem;

		// 刷新后的宝箱状态 1:铜 2:银 3:金
		public int box;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnTreasureFlush rl = new ReturnTreasureFlush();
		rl.seq = seq;

		TreasureBean tb = user.getDao().findTreasure(user.getUid());
		tb.flush();

		int dc = tb.getFlushCount();

		VipConf vc = Readonly.getInstance().findVip(user.getVip());

		if (dc >= (10 + vc.dtnum)) {
			rl.result = "今日刷新次数达到上限";
			user.send(rl);
			return;
		}

		if (dc > 9) {
			DailytreasureConf dtc = Readonly.getInstance().findDailytreasureConf(tb.getCurrent());
			rl.gem = dtc.recost[dc - 10];
		}

		if (rl.gem > 0) {
			PackBean pb = user.getDao().findPack(user.getUid());
			if (!pb.contains(钻石, rl.gem)) {
				rl.result = ErrCode.钻石不足.name();
				user.send(rl);
				return;
			}
			user.payItem(钻石, rl.gem, "刷新神秘宝藏");
		}

		tb.setFlushCount(dc+1);
		tb.changeTreasure(1);
		rl.box = tb.getCurrent();

		user.send(rl);
	}
}
