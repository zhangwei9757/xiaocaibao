package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.ChargeBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求月卡信息
 *
 */
@Component
public class RequestVipBagInfo extends BaseProtocol {
    public int seq;

    class ReturnVipBagInfo extends BaseProtocol {
		public int seq;

		public int vipDaily;
		public int[] vipWeeks;
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnVipBagInfo rl = new ReturnVipBagInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flush();

		rl.vipDaily = ab.getVipDailyBag();
		rl.vipWeeks = ab.getVipWeekBags();

        user.send(rl);
    }
}
