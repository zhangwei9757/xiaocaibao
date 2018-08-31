package com.tumei.game.protos.store;

import com.tumei.common.DaoService;
import com.tumei.game.GameUser;
import com.tumei.model.StoreBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.SessionUser;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestHeroStoreBuy extends BaseProtocol {
	public int seq;
	/**
	 * index，此key是顺序从0开始,[0,....]
	 */
	public int key;

	public int count;

	class ReturnHeroStoreBuy extends BaseProtocol {
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

		ReturnHeroStoreBuy rci = new ReturnHeroStoreBuy();
		rci.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		StoreBean sb = user.getDao().findStore(user.getUid());

		// 已经购买的记录
		List<StoreStruct> sss = sb.flushHeroStore(rb.getLevel(), false);
		StoreStruct ss = sss.get(key);

		if (ss.limit <= 0) {
			rci.result = "物品已经售罄";
	 	} else {
			PackBean pb = user.getDao().findPack(user.getUid());
			if (!pb.contains(ss.price[0], ss.price[1])) {
				rci.result = "没有足够的货币";
			} else {
				user.payItem(ss.price[0], ss.price[1], "英雄商店");
				rci.awards.addAll(user.addItem(ss.id, ss.count, false, "英雄商店"));
				--ss.limit;
				rci.price = ss.price;
				DaoService.getInstance().findSta(user.getUid()).incHeroStoreBuy(1);
			}
		}


		user.send(rci);
	}
}
