package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.ScoreRewardConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 *
 *
 */
@Component
public class RequestGetPowerInfoAward extends BaseProtocol {
    public int seq;

	public int index;

    class ReturnGetPowerInfoAward extends BaseProtocol {
		public int seq;
		public String result = "";

		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnGetPowerInfoAward rl = new ReturnGetPowerInfoAward();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushPower(user);

		List<Integer> st = ab.getPowerStatus();
		int ss = st.get(index); // -1表示满足条件了
		if (ss != 1) {
			rl.result = "条件不满足，无法领取";
			user.send(rl);
			return;
		}

		Readonly r = Readonly.getInstance();
		ScoreRewardConf cc = r.getScoreReward().get(index);

		st.set(index, 2);
		rl.awards.addAll(user.addItems(cc.reward, "战力"));

        user.send(rl);
    }
}
