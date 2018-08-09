package com.tumei.game.protos.misc;

import com.tumei.common.DaoGame;
import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.ItemConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.WebSocketUser;
import com.tumei.model.PackBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求合成装备
 */
@Component
public class RequestComposition extends BaseProtocol {
	public int seq;
	/**
	 * 碎片id
	 */
	public int item;

	class ReturnComposition extends BaseProtocol {
		public int seq;
		public String result = "";
		public AwardBean item;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnComposition rci = new ReturnComposition();
		rci.seq = seq;

		ItemConf ic = Readonly.getInstance().findItem(item);

		int need = 0;
		PackBean pb = user.getDao().findPack(user.getUid());

		if (item >= 50000 && item < 90000) { // 英雄合成
			switch (ic.quality) {
				case 2:
					need = 10;
					break;
				case 3:
					need = 40;
					break;
				case 4:
					need = 80;
					break;
				case 5:
					need = 150;
					break;
			}

			if (!pb.contains(item, need)) {
				rci.result = "合成所需碎片不足";
				user.send(rci);
				return;
			}
			user.payItem(item, need, "合成英雄:" + ic.good);

			int newItem = item - (item % 10);
			HeroBean hb = pb.addHero(newItem, "合成英雄:" + ic.good);
			rci.item = new AwardBean(hb.getId(), 1, hb.getHid());

		} else if (item >= 10000 && item < 20000) { // 装备
			switch (ic.quality) {
				case 2:
					need = 20;
					break;
				case 3:
					need = 32;
					break;
				case 4:
					need = 40;
					break;
				case 5:
					need = 60;
					break;
			}


			if (!pb.contains(item, need)) {
				rci.result = "合成所需碎片不足";
				user.send(rci);
				return;
			}
			user.payItem(item, need, "合成装备:" + ic.good);

			int newItem = item - (item % 10);
			EquipBean eb = pb.addEquip(newItem, "合成装备:" + ic.good);
			rci.item = new AwardBean(eb.getId(), 1, eb.getEid());
		} else if (Defs.isRuneID(item)) { // 符文
			switch (ic.quality) {
				case 2:
					need = 20;
					break;
				case 3:
					need = 30;
					break;
				case 4:
					need = 40;
					break;
				case 5:
					need = 60;
					break;
			}

			if (!pb.contains(item, need)) {
				rci.result = "合成所需碎片不足";
				user.send(rci);
				return;
			}
			user.payItem(item, need, "合成符文:" + ic.good);

			int newItem = item - (item % 10);
			user.addItem(newItem, 1, false, "合成符文:" + ic.good);
			rci.item = new AwardBean(newItem, 1, 0);
		} else if (item >= 20000 && item < 21000) { // 宝物
			switch (ic.quality) {
				case 2:
					need = 3;
					break;
				case 3:
					need = 4;
					break;
				case 4:
					need = 5;
					break;
				case 5:
					need = 6;
					break;
			}

			int newItem = item - (item % 10);
			for (int i = 0; i < need; ++i) {
				int ii = newItem + i + 1;
				if (!pb.contains(ii, 1)) {
					rci.result = "合成所需碎片不足";
					user.send(rci);
					return;
				}
			}
			for (int i = 0; i < need; ++i) {
				int ii = newItem + i + 1;
				user.payItem(ii, 1, "合成宝物:" + ic.good);
			}

			List<Integer> items = new ArrayList<>();
			for (int i = 0; i < need; ++i) {
				items.add(newItem + i + 1);
			}

			EquipBean eb = pb.addEquip(newItem, "合成宝物:" + ic.good);
			rci.item = new AwardBean(newItem, 1, eb.getEid());

			user.pushDailyTask(12, 1);
			DaoGame.getInstance().findSta(user.getUid()).incComposeTreasure(ic.quality);
		}

		user.send(rci);
	}
}
