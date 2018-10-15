package com.tumei.game.protos.skin;

import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.SkinStruct;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.modelconf.ItemConf;
import com.tumei.modelconf.MaskcostConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求充值信息
 */
@Component
public class RequestSkinDown extends BaseProtocol {
	public int seq;
	public int skin;

	class ReturnSkinDown extends BaseProtocol {
		public int seq;
		public String result = "";
		// 返回的金币
		public int gold;
		// 返回的时装精华
		public int cost;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnSkinDown rci = new ReturnSkinDown();
		rci.seq = seq;

		long uid = user.getUid();
		HerosBean hsb = user.getDao().findHeros(uid);

		SkinStruct ss = hsb.getSkins().get(skin);
		if (ss == null) {
			rci.result = "指定的时装不存在";
		} else {
			PackBean pb = user.getDao().findPack(user.getUid());

			if (!pb.contains(钻石, 200)) {
				rci.result = ErrCode.钻石不足.name();
				user.send(rci);
				return;
			}

			user.payItem(钻石, 200, "重生时装");

			int gold = 0;
			int cost = 0;
			ItemConf ic = Readonly.getInstance().findItem(skin);

			for (int i = 1; i < ss.level; ++i) {
				MaskcostConf mcc = Readonly.getInstance().findMaskcost(i);

				switch (ic.quality) {
					case 3:
						cost += mcc.cost1;
						gold += mcc.gold1;
						break;
					case 4:
						cost += mcc.cost2;
						gold += mcc.gold2;
						break;
					case 5:
						cost += mcc.cost3;
						gold += mcc.gold3;
						break;
				}
			}

			user.addItem(金币, gold, false, "分解皮肤");
			if (ic.quality == 红色) {
				user.addItem(高级时装精华, cost, false, "分解皮肤");
			} else {
				user.addItem(时装精华, cost, false, "分解皮肤");
			}

			rci.gold = gold;
			rci.cost = cost;
			ss.level = 1;
			hsb.checkSkinsuits();
		}

		user.send(rci);
	}
}
