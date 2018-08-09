package com.tumei.game.protos.store;

import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.StoreBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.RaidstoreConf;
import com.tumei.websocket.WebSocketUser;
import com.tumei.model.FireRaidBean;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 神装商店购买
 */
@Component
public class RequestRaidStoreBuy extends BaseProtocol {
	public int seq;
	/**
	 * 商品的key字段
	 */
	public int key;

	public int count;

	class ReturnRaidStoreBuy extends BaseProtocol {
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
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnRaidStoreBuy rci = new ReturnRaidStoreBuy();
		rci.seq = seq;

		if (count <= 0) {
			rci.result = "作弊";
			user.send(rci);
			return;
		}

		StoreBean sb = user.getDao().findStore(user.getUid());
		// 已经购买的记录
		Map<Integer, Integer> limits = sb.flushRaidStore();

		List<RaidstoreConf> store = Readonly.getInstance().findRaidStores();
		RaidstoreConf asc = store.get(key - 1);
		FireRaidBean frb = user.getDao().findFireRaid(user.getUid());
		if (asc.star > frb.getPeekStars()) {
			rci.result = "条件不满足";
			user.send(rci);
			return;
		}

		int nowCount = limits.getOrDefault(asc.key, 0);
		if (asc.limit != 0) {
			int left = asc.limit - nowCount;
			if (left < count) {
				rci.result = "该商品购买次数已达上限";
				user.send(rci);
				return;
			}
		}

		// 2. 获取价格
		PackBean pb = user.getDao().findPack(user.getUid());

		if (!pb.contains(asc.price, count)) {
			rci.result = "资源不足";
			user.send(rci);
			return;
		}

		user.payItem(asc.price, count, "神装商店");

		rci.awards.addAll(user.addItem(asc.item[0], asc.item[1] * count, false, "远征商店"));
		rci.price = asc.price;
		limits.put(asc.key, limits.getOrDefault(asc.key, 0) + count);
		user.send(rci);
	}
}
