package com.tumei.game.protos.store;

import com.tumei.game.GameUser;
import com.tumei.model.StoreBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestGroupStore extends BaseProtocol {
	public int seq;

	class ReturnGroupStore extends BaseProtocol {
		public int seq;

		// key是商品道具id, value是已经购买的次数
		public Map<Integer, Integer> first = new HashMap<>();

		// 第二个商店，界面第四个标签
		// key是商品道具id, value是已经购买的次数
		public Map<Integer, Integer> second = new HashMap<>();

		public long time;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnGroupStore rci = new ReturnGroupStore();
		rci.seq = seq;

		StoreBean sb = user.getDao().findStore(user.getUid());

		sb.flushGroupStore();
		rci.first = sb.getGroupStoreMap();
		rci.second = sb.flushGroupStore2(user.tmpGuildLevel);
		rci.time = 21600 - (System.currentTimeMillis() / 1000 - sb.getLastGroupStore2Flush());


		user.send(rci);
	}
}
