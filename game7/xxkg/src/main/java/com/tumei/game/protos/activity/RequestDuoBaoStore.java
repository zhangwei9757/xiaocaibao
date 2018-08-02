package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DbstoreConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求 夺宝 单冲
 */
@Component
public class RequestDuoBaoStore extends BaseProtocol {
    public int seq;
	// [0, ....]
	public int index;

    class ReturnDuoBaoStore extends BaseProtocol {
		public int seq;
		public List<AwardBean> awards = new ArrayList<>();
		public String result = "";
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnDuoBaoStore rl = new ReturnDuoBaoStore();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		int day = ab.flushDb();
		if (day > 2) {
			rl.result = "本期活动已经结束";
			user.send(rl);
			return;
		}

		List<DbstoreConf> dcs = Readonly.getInstance().getDbStoreConfs();
		DbstoreConf dc = dcs.get(index);

		int c = ab.getDbstores().get(index);
		if (c > dc.limit) {
			rl.result = "本期商店商品已经达到购买次数上限";
			user.send(rl);
			return;
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		if (!pb.contains(dc.price, 1)) {
			rl.result = "货币不足，无法购买";
			user.send(rl);
			return;
		}
		user.payItem(dc.price, 1, "夺宝商店");

		++c;
		ab.getDbstores().set(index, c);
		rl.awards.addAll(user.addItems(dc.goods, "夺宝商店"));

        user.send(rl);
    }
}
