package com.tumei.game.protos.rune;

import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.RuneBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 符文副本的打折商店
 */
@Component
public class RequestRuneStore extends BaseProtocol {
	public int seq;

	class ReturnRuneStore extends BaseProtocol {
		public int seq;
		public List<StoreStruct> items = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnRuneStore rl = new ReturnRuneStore();
		rl.seq = seq;

		RuneBean rb = user.getDao().findRune(user.getUid());
		rb.getStores().forEach(ss -> {
			rl.items.add(ss);
		});

		user.send(rl);
	}
}
