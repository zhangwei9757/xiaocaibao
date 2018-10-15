package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DbscoreConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.夺宝积分;

/**
 * Created by leon on 2016/12/31.
 * 请求 夺宝 单冲
 */
@Component
public class RequestDuoBaoScore extends BaseProtocol {
    public int seq;
	// [0, ....]
	public int index;

    class ReturnDuoBaoScore extends BaseProtocol {
		public int seq;
		public List<AwardBean> awards = new ArrayList<>();
		public String result = "";
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnDuoBaoScore rl = new ReturnDuoBaoScore();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		int day = ab.flushDb();
		if (day > 2) {
			rl.result = "本期活动已经结束";
			user.send(rl);
			return;
		}

		List<DbscoreConf> dcs = Readonly.getInstance().getDbScoreConfs();
		DbscoreConf dc = dcs.get(index);

		int c = ab.getDbscores().get(index);
		if (c > 0) {
			rl.result = "本期积分活动奖励已经领取";
			user.send(rl);
			return;
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		int score = pb.getItemCount(夺宝积分);

		if (score < dc.score) {
			rl.result = "积分活动条件未满足";
			user.send(rl);
			return;
		}

		ab.getDbscores().set(index, 1);
		rl.awards.addAll(user.addItems(dc.reward, "夺宝积分奖励"));

        user.send(rl);
    }
}
