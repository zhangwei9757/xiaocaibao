package com.tumei.game.protos.store;

import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.model.StoreBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.ItemstoreConf;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import com.tumei.common.utils.ErrCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestStoreBuy extends BaseProtocol {
	public int seq;
	/**
	 * 商品的key字段
	 */
	public int key;

	public int count;

	class ReturnStoreBuy extends BaseProtocol {
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

		ReturnStoreBuy rci = new ReturnStoreBuy();
		rci.seq = seq;

		if (count <= 0) {
			rci.result = "作弊";
			user.send(rci);
			return;
		}

		StoreBean sb = user.getDao().findStore(user.getUid());
		// 已经购买的记录
		Map<Integer, Integer> limits = sb.flushItemStore();

		RoleBean rb = user.getDao().findRole(user.getUid());
		VipConf vc = Readonly.getInstance().findVip(rb.getVip());

		List<ItemstoreConf> store = Readonly.getInstance().findItemStores();
		ItemstoreConf asc = store.get(key - 1);

		int nowCount = limits.getOrDefault(asc.key, 0);
		int left = 0;
		if (asc.limit == 0) {
			left = 99999; // 返回客户端-1表示无限购买，配置中0是无限购买，-1表示读vip表
		}
		else if (asc.limit < 0) {
			// vip决定
			switch (asc.key) {
				case 1:
					left = vc.drink - nowCount;
					break;
				case 2:
					left = vc.redbox - nowCount;
					break;
				case 3:
					left = vc.orangebox - nowCount;
					break;
				case 4:
					left = vc.trebox - nowCount;
					break;
			}
		}
		else { // 数字表示
			left = asc.limit - nowCount;
		}

		if (left < count) {
			rci.result = "该商品购买次数已达上限";
			user.send(rci);
			return;
		}

		// 2. 获取价格
		int price = 0;
		for (int i = 0; i < count; ++i) {
			if (asc.price == 0) {
				price += asc.cost[nowCount + i];
			}
			else {
				price += asc.cost[0];
			}
		}

		PackBean pb = user.getDao().findPack(user.getUid());

		if (!pb.contains(钻石, price)) {
			rci.result = ErrCode.钻石不足.name();
			user.send(rci);
			return;
		}

		user.payItem(钻石, price, "道具商店购买");

		rci.awards.addAll(user.addItem(asc.item[0], asc.item[1] * count, false, "道具商店"));
		rci.price = new int[]{price};
		limits.put(asc.key, limits.getOrDefault(asc.key, 0) + count);

		if (asc.key == 1) {// 购买活力药剂 日常任务
			user.pushDailyTask(10, count);
		}

		user.send(rci);
	}
}
