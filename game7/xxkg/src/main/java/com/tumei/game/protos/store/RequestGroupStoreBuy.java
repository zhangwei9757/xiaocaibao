package com.tumei.game.protos.store;

import com.tumei.common.Readonly;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.model.StoreBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.*;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestGroupStoreBuy extends BaseProtocol {
	public int seq;
	/**
	 * 商品的key字段
	 */
	public int key;
	public int count;
	/**
	 * 0: 是购买的前三个tab页面
	 * 1：是第四个页面
	 */
	public int mode;

	class ReturnGroupStoreBuy extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
		public int[] price = new int[2];
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnGroupStoreBuy rci = new ReturnGroupStoreBuy();
		rci.seq = seq;

		if (count <= 0) {
			rci.result = "作弊";
			user.send(rci);
			return;
		}

		StoreBean sb = user.getDao().findStore(user.getUid());
		sb.flushItemStore();

		RoleBean rb = user.getDao().findRole(user.getUid());
//		VipConf vc = Readonly.getInstance().findVip(rb.getVip());

		if (mode == 0) {
			List<GroupstoreConf> store = Readonly.getInstance().getGroupstoreConfs();
			GroupstoreConf asc = store.get(key - 1);
			Map<Integer, Integer> limits = sb.getGroupStoreMap();
			int nowCount = limits.getOrDefault(asc.key, 0);

			int left = 0;
			if (asc.limit == 0) {
				left = -1; // 返回客户端-1表示无限购买，配置中0是无限购买，-1表示读vip表
			} else { // 数字表示
				left = asc.limit - nowCount;
			}

			// 2. 获取价格
			rci.price = Arrays.copyOf(asc.price, asc.price.length);
			rci.price[1] = rci.price[1] * count;

			if (left < count) {
				rci.result = "该商品购买次数已达上限";
				user.send(rci);
				return;
			}

			if (user.guildLevel < asc.guildlevel) {
				rci.result = "公会等级未达到要求,无法购买";
				user.send(rci);
				return;
			}

			PackBean pb = user.getDao().findPack(user.getUid());

			if (!pb.contains(rci.price, 1)) {
				rci.result = "没有足够的货币购买商品";
				user.send(rci);
				return;
			}
			user.payItem(rci.price, 1, "公会商店购买");

			rci.awards.addAll(user.addItem(asc.item[0], asc.item[1] * count, false, "公会商店购买"));

			limits.put(asc.key, limits.getOrDefault(asc.key, 0) + count);
		} else {
			List<Groupstore2Conf> store = Readonly.getInstance().getGroupstore2Confs();
			Groupstore2Conf asc = store.get(key - 1);
			Map<Integer, Integer> limits = sb.getGroupStore2Map();
			int nowCount = limits.getOrDefault(asc.key, 1);

			if (nowCount > 0 || count != 1) {
				rci.result = "该商品购买次数已达上限";
				user.send(rci);
				return;
			}

			if (user.guildLevel < asc.guildlevel) {
				rci.result = "公会等级未达到要求,无法购买";
				user.send(rci);
				return;
			}

			// 2. 获取价格
			rci.price = new int[] {钻石, asc.price1};

			PackBean pb = user.getDao().findPack(user.getUid());

			if (!pb.contains(rci.price[0], rci.price[1])) {
				rci.result = "没有足够的货币购买商品";
				user.send(rci);
				return;
			}
			user.payItem(rci.price[0], rci.price[1], "公会商店购买");
			rci.awards.addAll(user.addItem(asc.goods[0], asc.goods[1], false, "公会商店购买"));
			limits.put(asc.key, limits.getOrDefault(asc.key, 0) + count);
		}

		user.send(rci);
	}
}
