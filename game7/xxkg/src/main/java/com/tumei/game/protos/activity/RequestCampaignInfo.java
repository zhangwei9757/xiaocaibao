package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求 征战竞技
 *
 */
@Component
public class RequestCampaignInfo extends BaseProtocol {
    public int seq;

    class ReturnCampaignInfo extends BaseProtocol {
		public int seq;
		// 本周期 配置表起始点[0,...
		public int begin;
		// 本周起 配置表结束点[0,...
		public int end;
		// 对应的购买次数
		public List<Integer> status = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnCampaignInfo rl = new ReturnCampaignInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushCampaign();
		rl.begin = ab.getCampaignBegin();
		rl.end = ab.getCampaignEnd();
		rl.status = ab.getCampaignStatus();
        user.send(rl);
    }
}
