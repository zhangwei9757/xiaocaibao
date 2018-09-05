package com.tumei.game.protos.rune;

import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.RuneBean;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 测试充值协议
 */
@Component
public class RequestRuneReset extends BaseProtocol {
	public int seq;
	// 0: 大重置，全体楼层到1
	// 1: 小重置，改名本层
	public int mode;

	class ReturnRuneReset extends BaseProtocol {
		public int seq;
		public String result = "";
		public int gem;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnRuneReset rl = new ReturnRuneReset();
		rl.seq = seq;

		if (mode < 0 || mode > 1) {
			rl.result = "参数错误";
			user.send(rl);
			return;
		}

		RuneBean rb = user.getDao().findRune(user.getUid());
		PackBean pb = user.getDao().findPack(user.getUid());

		int gem = 0;
		if (mode == 0) {
			if (rb.getUsedCount() > 0) {
				gem = (int)(Math.pow(2, (rb.getUsedCount() - 1)) * 100);
				// 符文重置价格 *20
				if (Defs.ISBT) {
					gem *= 20;
				}
				if (!pb.contains(钻石, gem)) {
					rl.result = "参数错误";
					user.send(rl);
					return;
				}
			}
		}

		if (!rb.reset(user.getVip(), mode == 0)) {
			rl.result = "重置次数不足";
			user.send(rl);
			return;
		}

		if (gem > 0) {
			user.payItem(钻石, gem, "重置符文副本");
			rl.gem = gem;
		}

		user.send(rl);
	}
}
