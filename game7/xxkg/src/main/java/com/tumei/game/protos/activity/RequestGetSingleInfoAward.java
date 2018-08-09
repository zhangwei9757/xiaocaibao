package com.tumei.game.protos.activity;

import com.tumei.game.services.LocalService;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.SingleStruct;
import com.tumei.model.ActivityBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.SinglerechargeConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求单冲信息
 *
 */
@Component
public class RequestGetSingleInfoAward extends BaseProtocol {
    public int seq;

	// Info返回的begin和end之间的索引
	public int index;

    class ReturnGetSingleInfoAward extends BaseProtocol {
		public int seq;
		public String result = "";

		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnGetSingleInfoAward rl = new ReturnGetSingleInfoAward();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushSingle();

		List<SingleStruct> st = ab.getSingleChargeAwards();
		SingleStruct ss = st.get(index); // 已经领取的次数

		// 查看当前对应时间段中的，充值记录是否大于st中已经领取的个数，大于才能继续领取
		SinglerechargeConf sc = Readonly.getInstance().getSingleConfs().get(index + LocalService.getInstance().getSingleBeginIdx());
		// 充值记录从最后一天开始看起，如果最后一天是今天，则倒数
		if (ss.count <= ss.used) {
			rl.result = "需要充值才能领取奖励";
		} else if (ss.used >= sc.limit) {
			rl.result = "已经达到本次活动的上限";
		} else {
			ss.used += 1;
			if (ss.rewards == null) {
				rl.awards.addAll(user.addItems(sc.reward, "单充"));
			} else {
				rl.awards.addAll(user.addItems(ss.rewards, "单充"));
			}
		}

        user.send(rl);
    }
}
