package com.tumei.game.protos.group;

import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupSceneOver extends BaseProtocol {
	public int seq;

	class ReturnGroupSceneOver extends BaseProtocol {
		public int seq;
		public Set<Integer> status = new HashSet<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupSceneOver rl = new ReturnGroupSceneOver();
		rl.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			Set<Integer> sa = gb.getSceneAward();
			sa.forEach(i -> rl.status.add(i));
		}

		user.send(rl);
	}
}
