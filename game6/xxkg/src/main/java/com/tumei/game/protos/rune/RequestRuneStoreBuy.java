package com.tumei.game.protos.rune;

import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.PackBean;
import com.tumei.model.RuneBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 符文副本的打折商店
 */
@Component
public class RequestRuneStoreBuy extends BaseProtocol {
	public int seq;
	// [1，]
	public int index;

	class ReturnRuneStoreBuy extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnRuneStoreBuy rl = new ReturnRuneStoreBuy();
		rl.seq = seq;

		RuneBean rb = user.getDao().findRune(user.getUid());
		StoreStruct ss = rb.getStores().get(index - 1);
		if (ss.limit <= 0) {
			rl.result = "已经购买";
			user.send(rl);
			return;
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		if (!pb.contains(ss.price[0], ss.price[1])) {
			rl.result = ErrCode.钻石不足.name();
			user.send(rl);
			return;
		}

		ss.limit -= 1;
		user.payItem(ss.price[0], ss.price[1], "符文折扣购买");

		rl.awards.addAll(user.addItem(ss.id, ss.count, false, "符文买"));

		user.send(rl);
	}
}
