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

import static com.tumei.common.utils.Defs.时装精华;
import static com.tumei.common.utils.Defs.金币;
import static com.tumei.common.utils.Defs.高级时装精华;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求充值信息
 */
@Component
public class RequestSkinUp extends BaseProtocol {
	public int seq;
	public int skin;

	class ReturnSkinUp extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnSkinUp rci = new ReturnSkinUp();
		rci.seq = seq;

		long uid = user.getUid();
		HerosBean hsb = user.getDao().findHeros(uid);

		SkinStruct ss = hsb.getSkins().get(skin);
		if (ss == null) {
			rci.result = "指定的时装不存在";
		} else {
			// 1. 读取时装费用表
			MaskcostConf mcc = Readonly.getInstance().findMaskcost(ss.level);

			// 2. 读取时装的品质
			ItemConf ic = Readonly.getInstance().findItem(skin);
			int cost = mcc.cost3;
			int gold = mcc.gold3;
			int costid = 时装精华;
			switch (ic.quality) {
				case 3:
					cost = mcc.cost1;
					gold = mcc.gold1;
					break;
				case 4:
					cost = mcc.cost2;
					gold = mcc.gold2;
					break;
				case 5:
					costid = 高级时装精华;
					break;
			}

			PackBean pb = user.getDao().findPack(user.getUid());
			if (!pb.contains(金币, gold)) {
				rci.result = ErrCode.金币不足.name();
				user.send(rci);
				return;
			}
			if (!pb.contains(costid, cost)) {
				rci.result = "没有足够的是精华材料";
				user.send(rci);
				return;
			}

			user.payItem(金币, gold, "强化时装");
			user.payItem(costid, cost, "强化时装");

			ss.level += 1;
			hsb.checkSkinsuits();
		}

		user.send(rci);
	}
}
