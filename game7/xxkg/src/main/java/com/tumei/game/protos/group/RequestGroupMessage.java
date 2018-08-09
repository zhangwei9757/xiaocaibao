package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.common.group.GroupMessage;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会详细信息
 */
@Component
public class RequestGroupMessage extends BaseProtocol {
	public int seq;

	class Return extends BaseProtocol {
		public int seq;
		public List<String> msgs = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		Return rci = new Return();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() <= 0) {
			user.send(rci);
			return;
		}

		rci.msgs = RemoteService.getInstance().askGroupMessage(gb.getGid());

		user.send(rci);
	}
}
