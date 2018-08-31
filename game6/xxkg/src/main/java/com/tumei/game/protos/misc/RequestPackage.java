package com.tumei.game.protos.misc;

import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级装备
 */
@Component
public class RequestPackage extends BaseProtocol {
	public int seq;

	class Return extends BaseProtocol {
		public int seq;

		public int gem;
		public long gold;
		public HashMap<Integer, Integer> items = new HashMap<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		Return rtn = new Return();
		rtn.seq = seq;

		PackBean pb = user.getDao().findPack(user.getUid());
		rtn.gem = pb.getGem();
		rtn.gold = pb.getCoin();

		rtn.items.putAll(pb.getItems());
		user.send(rtn);
	}
}
