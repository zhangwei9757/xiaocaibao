package com.tumei.game.protos.activity;

import com.tumei.common.LocalService;
import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 * 请求月卡信息
 *
 */
@Component
public class RequestFundBuy extends BaseProtocol {
    public int seq;

    class ReturnFundBuy extends BaseProtocol {
		public int seq;
		public String result = "";
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnFundBuy rl = new ReturnFundBuy();
		rl.seq = seq;

		if (user.getVip() < Defs.开服基金购买等级或英雄福利购买等级) {
			rl.result = String.format("VIP%d及以上玩家才能购买基金", Defs.开服基金购买等级或英雄福利购买等级);
			user.send(rl);
			return;
		}

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		if (ab.getFund() != 0) {
			rl.result = "钻石不足";
			user.send(rl);
			return;
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		if (!pb.contains(钻石, Defs.开服基金购买)) {
			rl.result = "钻石不足";
		} else {
			user.payItem(钻石, Defs.开服基金购买, "开服基金");
			ab.setFund(1);
			LocalService.getInstance().incFundCount();
		}

        user.send(rl);
    }
}
