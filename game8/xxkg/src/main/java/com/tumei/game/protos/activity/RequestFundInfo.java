package com.tumei.game.protos.activity;

import com.tumei.game.services.LocalService;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求月卡信息
 *
 */
@Component
public class RequestFundInfo extends BaseProtocol {
    public int seq;

    class ReturnFundInfo extends BaseProtocol {
		public int seq;
		public int buy;
		// 本服务器购买基金的总人数
		public int fund;
		public List<Integer> status = new ArrayList<>();
		public List<Integer> status2 = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnFundInfo rl = new ReturnFundInfo();
		rl.seq = seq;
		rl.fund = LocalService.getInstance().getFundCount();

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		rl.buy = ab.getFund();
		if (rl.buy != 0) {
			ab.flushFunds();
		}

		Arrays.stream(ab.getFundStates()).forEach((i) -> rl.status.add(i));
		Arrays.stream(ab.getFundStates2()).forEach((i) -> rl.status2.add(i));

        user.send(rl);
    }
}
