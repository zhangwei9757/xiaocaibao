package com.tumei.game.protos.arena;

import com.tumei.game.GameUser;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.ArenastoreConf;
import com.tumei.websocket.SessionUser;
import com.tumei.model.PackBean;
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
public class RequestArenaBuy extends BaseProtocol {
	public int seq;
	/**
	 * 商品的key字段
	 */
	public int key;

	public int count;

	class ReturnArenaBuy extends BaseProtocol {
		public int seq;
		public String result = "";
		/**
		 * 购买的物品
		 */
		public List<AwardBean> awards = new ArrayList<>();
		/**
		 * 具体的价格
		 */
		public int[] price;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnArenaBuy rci = new ReturnArenaBuy();
		rci.seq = seq;

		if (count <= 0) {
			rci.result = "作弊";
			user.send(rci);
			return;
		}


		RoleBean rb = user.getDao().findRole(user.getUid());
		int level = rb.getLevel();
		int peek = user.getPeekRank();

		Map<Integer, Integer> limits = user.getArenaStoreLimit();

		List<ArenastoreConf> store = Readonly.getInstance().findArenaStores();
		ArenastoreConf asc = store.get(key - 1);
		if (asc.limit != 0) {
			int left = asc.limit - limits.getOrDefault(asc.key, 0);
			if (left < count) {
				rci.result = "该商品购买次数已达上限";
				user.send(rci);
				return;
			}
		}

			if (asc.level > level) {
				rci.result = "等级不满足条件";
				user.send(rci);
				return;
			}

			if (asc.rank != 0 && asc.rank <= peek) {
				rci.result = "排名不满足条件";
				user.send(rci);
				return;
			}



		PackBean pb = user.getDao().findPack(user.getUid());

		for (int i = 0; i < asc.price.length; i += 2) {
			if (!pb.contains(asc.price[i], asc.price[i + 1] * count)) {
				rci.result = "无法购买";
				user.send(rci);
				return;
			}
		}

		for (int i = 0; i < asc.price.length; i += 2) {
			user.payItem(asc.price[i], asc.price[i+1] * count, "竞技场商店购买");
		}

		rci.awards.addAll(user.addItem(asc.item[0], asc.item[1] * count, false, "竞技场商店"));
		if (asc.limit != 0) {
			user.addArenaStoreCount(key, count);
		}
		rci.price = asc.price;

		user.send(rci);
	}
}
