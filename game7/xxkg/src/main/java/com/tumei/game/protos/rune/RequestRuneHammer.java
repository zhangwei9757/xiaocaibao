package com.tumei.game.protos.rune;

import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.RuneBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.金币;
import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 测试充值协议
 */
@Component
public class RequestRuneHammer extends BaseProtocol {
	public int seq;
	// 1,2,3 分别是 5个锤子 10个锤子， 15个个锤子
	public int mode;

	class ReturnRuneHammer extends BaseProtocol {
		public int seq;
		public String result = "";
		public int gem;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnRuneHammer rl = new ReturnRuneHammer();
		rl.seq = seq;

		if (mode < 1 || mode > 3) {
			rl.result = "参数错误";
			user.send(rl);
			return;
		}

		RuneBean rb = user.getDao().findRune(user.getUid());
		PackBean pb = user.getDao().findPack(user.getUid());

		int gem = 0;
		switch (mode) {
			case 1:
				if (!pb.contains(金币, 2000000)) {
					rl.result = "金币不足";
					user.send(rl);
					return;
				}
				rb.setHammer(5);
				user.payItem(金币, 2000000, "选锤子");
				break;
			case 2:
				gem = 50;
				if (!pb.contains(钻石, gem)) {
					rl.result = "钻石不足";
					user.send(rl);
					return;
				}
				rb.setHammer(10);
				user.payItem(钻石, gem, "选锤子");
				break;
			case 3:
				gem = 200;
				if (!pb.contains(钻石, gem)) {
					rl.result = "钻石不足";
					user.send(rl);
					return;
				}
				rb.setHammer(15);
				user.payItem(钻石, gem, "选锤子");
				break;
		}
		rl.gem = gem;
		rb.setHammermode(mode);

		user.send(rl);
	}
}
