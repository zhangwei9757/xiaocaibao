package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DbspendConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求 夺宝 单冲
 */
@Component
public class RequestDuoBaoSpend extends BaseProtocol {
    public int seq;
	// [0, ....]
	public int index;

    class ReturnDuoBaoSpend extends BaseProtocol {
		public int seq;
		public List<AwardBean> awards = new ArrayList<>();
		public String result = "";
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnDuoBaoSpend rl = new ReturnDuoBaoSpend();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		int day = ab.flushDb();
		if (day > 2) {
			rl.result = "本期活动已经结束";
			user.send(rl);
			return;
		}

		List<DbspendConf> dcs = Readonly.getInstance().getDbSpendConfs();
		DbspendConf dc = dcs.get(index);

		int c = ab.getDbspends().get(index);
		if (c > 0) {
			rl.result = "本期消费活动奖励已经领取";
			user.send(rl);
			return;
		}

		int spend = ab.getDbSpend();
		if (spend < dc.spend) {
			rl.result = "消费活动条件未满足";
			user.send(rl);
			return;
		}

		ab.getDbspends().set(index, 1);
		rl.awards.addAll(user.addItems(dc.reward, "夺宝花费"));

        user.send(rl);
    }
}
