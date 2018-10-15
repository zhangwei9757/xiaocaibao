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
public class RequestGoldtreeInfo extends BaseProtocol {
    public int seq;

    class ReturnGoldtreeInfo extends BaseProtocol {
		public int seq;
		// 今日使用摇钱树次数
		public int used;
		// 当前进度奖励领取最高索引，默认是0，没有领取，最高是4，表示4个奖励均领取
		public int index;
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnGoldtreeInfo rl = new ReturnGoldtreeInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flush();
		rl.used = ab.getGoldTree();
		rl.index = ab.getGoldIndex();
        user.send(rl);
    }
}
