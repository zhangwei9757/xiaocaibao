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
 * 请求 战力奖励
 *
 */
@Component
public class RequestPowerInfo extends BaseProtocol {
    public int seq;

    class ReturnPowerInfo extends BaseProtocol {
		public int seq;
		// 默认为0，战力达标是1, 领取是2
		public List<Integer> status = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnPowerInfo rl = new ReturnPowerInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushPower(user);
		rl.status = ab.getPowerStatus();
        user.send(rl);
    }
}
