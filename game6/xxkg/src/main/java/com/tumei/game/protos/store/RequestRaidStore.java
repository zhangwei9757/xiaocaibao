package com.tumei.game.protos.store;

import com.tumei.game.GameUser;
import com.tumei.model.StoreBean;
import com.tumei.modelconf.RaidstoreConf;
import com.tumei.websocket.SessionUser;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.FireRaidBean;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 神装商店
 */
@Component
public class RequestRaidStore extends BaseProtocol {
	public int seq;

	class ReturnRaidStore extends BaseProtocol {
		public int seq;
		/**
		 * 对应4个tab页的商品
		 */
		public List<StoreStruct> tab1 = new ArrayList<>();
		public List<StoreStruct> tab2 = new ArrayList<>();
		public List<StoreStruct> tab3 = new ArrayList<>();
		public List<StoreStruct> tab4 = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnRaidStore rci = new ReturnRaidStore();
		rci.seq = seq;

		FireRaidBean frb = user.getDao().findFireRaid(user.getUid());
		StoreBean sb = user.getDao().findStore(user.getUid());
		// 已经购买的记录
		Map<Integer, Integer> limits = sb.flushRaidStore();

		List<RaidstoreConf> store = Readonly.getInstance().findRaidStores();
		for (RaidstoreConf asc : store) {
			// 当前这个商品的购买次数
			int nowCount = limits.getOrDefault(asc.key, 0);

			StoreStruct ss = new StoreStruct(asc.key);
			ss.id = asc.item[0];
			ss.count = asc.item[1];

			if (asc.star > frb.getPeekStars()) {
				ss.disable = 1;
			}

			ss.price = asc.price;

			if (asc.limit == 0) {
				ss.limit = -1; // 返回客户端-1表示无限购买，配置中0是无限购买，-1表示读vip表
			} else { // 数字表示
				ss.limit = asc.limit - nowCount;
			}
			switch (asc.tab) {
				case 1:
					rci.tab1.add(ss);
					break;
				case 2:
					rci.tab2.add(ss);
					break;
				case 3:
					rci.tab3.add(ss);
					break;
				case 4:
					rci.tab4.add(ss);
					break;
			}
		}

		user.send(rci);
	}
}
