package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.ChargeBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.FirstrechargeConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求签到今日
 *
 */
@Component
public class RequestGetFirstChargeAward extends BaseProtocol {
    public int seq;

    class ReturnGetFirstChargeAward extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnGetFirstChargeAward rl = new ReturnGetFirstChargeAward();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		if (ab.getFirstAward() != 0) {
			rl.result = "首充奖励已经领取";
			user.send(rl);
			return;
		}

		ChargeBean rb = user.getDao().findCharge(user.getUid());
		if (rb.getTotal() <= 0) {
			rl.result = "没有查询到充值记录";
			user.send(rl);
			return;
		}

		ab.setFirstAward(1);
		FirstrechargeConf fc = Readonly.getInstance().getFirstrechargeConf();
		rl.awards.addAll(user.addItems(fc.reward, "首充"));

        user.send(rl);
    }
}
