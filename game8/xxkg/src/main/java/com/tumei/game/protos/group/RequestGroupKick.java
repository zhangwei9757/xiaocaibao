package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupKick extends BaseProtocol {
	public int seq;
	public long target;

	class ReturnGroupKick extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupKick rci = new ReturnGroupKick();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() <= 0) {
			rci.result = "请先加入一个公会";
			user.send(rci);
			return;
		}

		// 远程拉取公会推荐信息
		try {
			rci.result = RemoteService.getInstance().askGroupKick(gb.getGid(), user.getUid(), target);
		} catch (Exception ex) {
			rci.result = "公会服务维护中";
		}

		user.send(rci);
	}
}
