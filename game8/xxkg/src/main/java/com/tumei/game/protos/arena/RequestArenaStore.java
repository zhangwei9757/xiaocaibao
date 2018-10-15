package com.tumei.game.protos.arena;

import com.tumei.game.GameUser;
import com.tumei.modelconf.ArenastoreConf;
import com.tumei.websocket.WebSocketUser;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.RoleBean;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestArenaStore extends BaseProtocol {
	public int seq;

	class ReturnArenaStore extends BaseProtocol {
		public int seq;
		/**
		 * 商店购买
		 */
		public List<StoreStruct> normal = new ArrayList<>();
		/**
		 * 奖励购买
		 */
		public List<StoreStruct> advanced = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnArenaStore rci = new ReturnArenaStore();
		rci.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		int level = rb.getLevel();
		int peek = user.getPeekRank();

		Map<Integer, Integer> limits = user.getArenaStoreLimit();

		List<ArenastoreConf> store = Readonly.getInstance().findArenaStores();
		for (ArenastoreConf asc : store) {
			if (asc.level > level) {
				continue;
			}

			if (asc.rank != 0 && asc.rank <= peek) {
				continue;
			}

			StoreStruct ss = new StoreStruct(asc.key);
			ss.id = asc.item[0];
			ss.count = asc.item[1];
			ss.price = asc.price;
			ss.limit = asc.limit - limits.getOrDefault(asc.key, 0);

			if (asc.tab == 1) { // 普通
				rci.normal.add(ss);
			} else if (asc.tab == 2) { // 高级
				rci.advanced.add(ss);
			}
		}

		user.send(rci);
	}
}
