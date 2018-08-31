package com.tumei.game.protos.store;

import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.RoleBean;
import com.tumei.model.StoreBean;
import com.tumei.modelconf.ItemstoreConf;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 */
@Component
public class RequestStore extends BaseProtocol {
	public int seq;

	class ReturnStore extends BaseProtocol {
		public int seq;
		/**
		 * 商店购买
		 */
		public List<StoreStruct> normal = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnStore rci = new ReturnStore();
		rci.seq = seq;

		StoreBean sb = user.getDao().findStore(user.getUid());
		// 已经购买的记录
		Map<Integer, Integer> limits = sb.flushItemStore();

		RoleBean rb = user.getDao().findRole(user.getUid());
		VipConf vc = Readonly.getInstance().findVip(rb.getVip());

		List<ItemstoreConf> store = Readonly.getInstance().findItemStores();
		for (ItemstoreConf asc : store) {
			// 当前这个商品的购买次数
			int nowCount = limits.getOrDefault(asc.key, 0);

			StoreStruct ss = new StoreStruct(asc.key);
			ss.id = asc.item[0];
			ss.count = asc.item[1];

			if (asc.price == 0) {
				ss.price = new int[]{钻石, asc.cost[nowCount]};
			} else {
				ss.price = new int[]{钻石, asc.cost[0]};
			}

			if (asc.limit == 0) {
				ss.limit = -1; // 返回客户端-1表示无限购买，配置中0是无限购买，-1表示读vip表
			} else if (asc.limit < 0) {
				ss.limit = nowCount;
			} else { // 数字表示
				ss.limit = nowCount;
//				ss.limit = asc.limit - nowCount;
			}
			rci.normal.add(ss);
		}

		user.send(rci);
	}
}
