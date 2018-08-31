package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ChargeBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 * 请求月卡信息
 *
 */
@Component
public class RequestCardInfo extends BaseProtocol {
    public int seq;

    class ReturnCardInfo extends BaseProtocol {
		public int seq;
		// 小月卡 剩余秒数
		public int small;
		// 大月卡 ...
		public int big;
		// 年卡 ...
		public int year;
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnCardInfo rl = new ReturnCardInfo();
		rl.seq = seq;

		ChargeBean cb = user.getDao().findCharge(user.getUid());
		cb.checkSendCards();
		long now = System.currentTimeMillis() / 1000;
		rl.small = (int)(cb.getMonth() - now);
		rl.big = (int)(cb.getBigmonth() - now);
		rl.year = (int)(cb.getYear() - now);

        user.send(rl);
    }
}
