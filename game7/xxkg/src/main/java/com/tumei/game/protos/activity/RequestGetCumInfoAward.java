package com.tumei.game.protos.activity;

import com.tumei.game.services.LocalService;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.CumrechargeConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 *
 *
 */
@Component
public class RequestGetCumInfoAward extends BaseProtocol {
    public int seq;

	public int index;

    class ReturnGetCumInfoAward extends BaseProtocol {
		public int seq;
		public String result = "";

		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnGetCumInfoAward rl = new ReturnGetCumInfoAward();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushCum();

		List<Integer> st = ab.getCumChargeAwards();
		int ss = st.get(index); // 已经领取的次数
		if (ss != -1) {
			rl.result = "条件不满足，无法购买";
			user.send(rl);
			return;
		}

		Readonly r = Readonly.getInstance();
		CumrechargeConf cc = r.getCumConfs().get(index + LocalService.getInstance().getCumBeginIdx());

		st.set(index, -2);
		rl.awards.addAll(user.addItems(cc.reward, "累充"));

        user.send(rl);
    }
}
