package com.tumei.game.protos.heros;

import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.modelconf.LineupConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.金币;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求当前阵形加成
 */
@Component
public class RequestLineup extends BaseProtocol {
	public int seq;
	/**
	 * 站位[0,5]
	 */
	public int index;

	class ReturnLineup extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnLineup rl = new ReturnLineup();
		rl.seq = seq;

		if (index >= 0 && index <= 5) {
			HerosBean hsb = user.getDao().findHeros(user.getUid());
			int[] lu = hsb.getLineups();
			int next = lu[index] + 1;
			LineupConf lc = Readonly.getInstance().findLineup(index);
			if (next >= (lc.cost1.length)) {
				rl.result = "阵位已经达到最高等级";
			} else {
				PackBean pb = user.getDao().findPack(user.getUid());
				if (!pb.contains(lc.cost1[0], lc.cost1[next])) {
					rl.result = "所需材料不足";
				} else {
					if (!pb.contains(金币, lc.cost2[0] * (next))) {
						rl.result = ErrCode.金币不足.name();
					} else {
						lu[index] = next;
					}
					user.payItem(lc.cost1[0], lc.cost1[next], "阵形");
					user.payItem(金币, lc.cost2[0] * next, "阵形");
				}
			}
		} else {
			rl.result = ErrCode.未知参数.name();
		}

		user.send(rl);
	}
}
