package com.tumei.game.protos.group;

import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.model.PackBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupSceneBuyCount extends BaseProtocol {
	public int seq;

	class ReturnGroupSceneBuyCount extends BaseProtocol {
		public int seq;
		public String result = "";
		public int gem;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupSceneBuyCount rl = new ReturnGroupSceneBuyCount();
		rl.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			// 本地控制攻击次数和恢复时间
			gb.flush(user.getVip());

			VipConf vc = Readonly.getInstance().findVip(user.getVip());
			if (vc.guildraid <= gb.getBuyCount()) {
				rl.result = "今日公会副本购买次数达到上限";
				user.send(rl);
				return;
			}

			rl.gem = 30 * (1 + gb.getBuyCount());

			PackBean pb = user.getDao().findPack(user.getUid());
			if (!pb.contains(钻石, rl.gem)) {
				rl.result = "钻石不足";
				user.send(rl);
				return;
			}

			gb.setSceneCount(1 + gb.getSceneCount());
			user.payItem(钻石, rl.gem, "购买公会副本战斗次数");
			gb.setBuyCount(gb.getBuyCount() + 1);
		}

		user.send(rl);
	}
}
