package com.tumei.game.protos.misc;

import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.modelconf.ItemConf;
import com.tumei.websocket.WebSocketUser;
import com.tumei.common.Readonly;
import com.tumei.common.utils.ErrCode;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级装备
 */
@Component
public class RequestSellItem extends BaseProtocol {
	public int seq;
	/**
	 * 物品
	 */
	public List<Integer> items;

	/**
	 * 1: 英雄出售
	 * 2: 装备，宝物出售
	 * 3: 其他物品
	 */
	public int mode;

	class ReturnSellItem extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnSellItem rci = new ReturnSellItem();
		rci.seq = seq;

		for (int item : items) {
			ItemConf ic = Readonly.getInstance().findItem(item);
			if (ic.sell <= 0) {
				rci.result = ErrCode.不能出售.name();
				user.send(rci);
				return;
			}

			PackBean pb = user.getDao().findPack(user.getUid());
			// 英雄
			if (mode == 1) {
				if (!pb.getHeros().containsKey(item)) {
					rci.result = ErrCode.英雄不存在.name();
				}
				else {
					pb.getHeros().remove(item);
					user.addItem(ic.price[0], ic.price[1], false, "出售");
				}
			}
			else if (mode == 2) { // 装备
				if (!pb.getEquips().containsKey(item)) {
					rci.result = ErrCode.装备不存在.name();
				}
				else {
					pb.payEquip(item, "出售");
					user.addItem(ic.price[0], ic.price[1], false, "出售");
				}
			}
			else {
				// 个数0表示有多少卖多少
				long count = user.payItem(item, Integer.MAX_VALUE, "出售");
				if (count > 0) {
					user.addItem(ic.price[0], ic.price[1] * count, false, "出售");
				}
				else {
					rci.result = "没有这个物品，无法出售";
				}
			}
		}

		user.send(rci);
	}
}
