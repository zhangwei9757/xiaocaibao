package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.GoldtreeConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 地精宝藏 摇钱树
 *
 */
@Component
public class RequestGetGoldtreeAward extends BaseProtocol {
    public int seq;

    class ReturnGetGoldtreeAward extends BaseProtocol {
		public int seq;
		public String result = "";

		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnGetGoldtreeAward rl = new ReturnGetGoldtreeAward();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flush();

		int idx = ab.getGoldIndex();
		if (idx == 4) {
			rl.result = "没有奖励";
			user.send(rl);
			return;
		}
		++idx;
		int iii = idx * 5;
		if (ab.getGoldTree() < iii) {
			rl.result = "摇钱树使用次数不足";
			user.send(rl);
			return;
		}

		ab.setGoldIndex(idx);
		GoldtreeConf gc = Readonly.getInstance().findGoldtree(1);
		int i = (idx-1) * 2;
		rl.awards.addAll(user.addItem(gc.reward[i], gc.reward[i+1], false, "摇钱树奖励"));

        user.send(rl);
    }
}
