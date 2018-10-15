package com.tumei.game.protos.activity;

import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 * 请求月卡信息
 *
 */
@Component
public class RequestForheroBuy extends BaseProtocol {
    public int seq;

    class ReturnForheroBuy extends BaseProtocol {
		public int seq;
		public String reuslt = "";
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnForheroBuy rl = new ReturnForheroBuy();
		rl.seq = seq;

		if (user.getVip() < 3) {
			rl.reuslt = "VIP3及以上等级的玩家才能购买神将福利";
			user.send(rl);
			return;
		}

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		if (ab.getHeroFuli() != 0) {
			rl.reuslt = "已经购买过神将福利";
			user.send(rl);
			return;
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		if (!pb.contains(钻石, 888)) {
			rl.reuslt = ErrCode.钻石不足.name();
			user.send(rl);
			return;
		}

		user.payItem(钻石, 888, "购买神将福利");
		ab.setHeroFuli(1);

        user.send(rl);
	}
}
