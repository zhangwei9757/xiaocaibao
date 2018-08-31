package com.tumei.game.protos.treasure;

import com.tumei.GameConfig;
import com.tumei.common.Readonly;
import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.TreasureBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DailytreasureConf;
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
public class RequestTreasureDouble extends BaseProtocol {
	public int seq;

	class ReturnTreasureDouble extends BaseProtocol {
		public int seq;

		public String result = "";

		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnTreasureDouble rl = new ReturnTreasureDouble();
		rl.seq = seq;

		TreasureBean tb = user.getDao().findTreasure(user.getUid());
		tb.flush();

		if (tb.getAwards() == null) {
			rl.result = "没有双倍可以领取";
			user.send(rl);
			return;
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		if (!pb.contains(钻石, tb.getDoubleBox())) {
			rl.result = ErrCode.钻石不足.name();
			user.send(rl);
			return;
		}

		user.payItem(钻石, tb.getDoubleBox(), "双倍神秘");

		for (AwardBean ab : tb.getAwards()) {
			user.addItem(ab.id, ab.count, false, "双倍神秘宝藏");
		}

		rl.awards = tb.getAwards();
		tb.setAwards(null);
		tb.setDoubleBox(0);

		user.send(rl);
	}
}
