package com.tumei.game.protos.activity;

import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.ChargeBean;
import com.tumei.model.DailySceneBean;
import com.tumei.model.beans.ChargeDayBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by leon on 2016/12/31.
 * 请求签到信息
 *
 */
@Component
public class RequestSignInfo extends BaseProtocol {
    public int seq;

    class ReturnSignInfo extends BaseProtocol {
		public int seq;
		// 已经签到过的天数, 根据配置表30天轮询
		public int signDays;
		// 今日签到的状态 0：未签到 1：有双倍但是未签满 2:全部签到结束
		public int status;
		// 豪华签到是否领取，0未领取
		public int chargeAward;
		// 今日充值金额
		public int todayCharge;
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnSignInfo rl = new ReturnSignInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flush();
		rl.signDays = ab.getSignDays();
		rl.status = ab.getSignState() % 10;
		rl.chargeAward = ab.getSignState() / 10;

		ChargeBean cb = user.getDao().findCharge(user.getUid());
		int today = TimeUtil.getToday();
		Optional<ChargeDayBean> opt = cb.getDayCharges().stream().filter(cdb -> cdb.day == today).findFirst();
		if (opt.isPresent()) {
			rl.todayCharge = opt.get().rmb;
		}

        user.send(rl);
    }
}
