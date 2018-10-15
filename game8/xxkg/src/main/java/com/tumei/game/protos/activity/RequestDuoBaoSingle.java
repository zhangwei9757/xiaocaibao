package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DbsingleConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求 夺宝 单冲
 */
@Component
public class RequestDuoBaoSingle extends BaseProtocol {
    public int seq;
	// [0, ....]
	public int index;

    class ReturnDuoBaoSingle extends BaseProtocol {
		public int seq;
		public List<AwardBean> awards = new ArrayList<>();
		public String result = "";
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnDuoBaoSingle rl = new ReturnDuoBaoSingle();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		int day = ab.flushDb();
		if (day > 2) {
			rl.result = "本期活动已经结束";
			user.send(rl);
			return;
		}

		List<DbsingleConf> dcs = Readonly.getInstance().getDbSingleConfs();
		DbsingleConf dc = dcs.get(index);

		int c = ab.getDbsingles().get(index);
		if (c >= dc.limit) {
			rl.result = "本期充值活动奖励达到次数上限";
			user.send(rl);
			return;
		}
		++c;

		int count = ab.getDbcharges().get(index);
		if (count < c) {
			rl.result = "充值活动次数未满足，请充值对应的档位";
			user.send(rl);
			return;
		}

		ab.getDbsingles().set(index, c);
		rl.awards.addAll(user.addItems(dc.reward, "夺宝单充"));

        user.send(rl);
    }
}
