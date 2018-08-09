package com.tumei.game.protos.daily;

import com.tumei.game.GameUser;
import com.tumei.model.DailyTaskBean;
import com.tumei.websocket.WebSocketUser;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestDailyTasks extends BaseProtocol {
    public int seq;

    class ReturnDailyTasks extends BaseProtocol {
		public int seq;

		public DailyTaskBean daily;
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnDailyTasks rl = new ReturnDailyTasks();
		rl.seq = seq;
		rl.daily = user.flushDailyTask();

        user.send(rl);
    }
}
