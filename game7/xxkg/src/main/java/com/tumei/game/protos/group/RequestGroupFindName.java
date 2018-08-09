package com.tumei.game.protos.group;

import com.google.common.base.Strings;
import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.common.group.GroupSimpleStruct;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupFindName extends BaseProtocol {
	public int seq;
	public String name;

	class ReturnGroupFindName extends BaseProtocol {
		public int seq;
		public GroupSimpleStruct group;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;


		ReturnGroupFindName rci = new ReturnGroupFindName();
		rci.seq = seq;
		if (name == null) {
			user.send(rci);
			return;
		}
		name = name.trim();
		if (Strings.isNullOrEmpty(name) || name.length() > 32) {
			user.send(rci);
			return;
		}

		// 远程拉取公会推荐信息
		try {
			rci.group = RemoteService.getInstance().askGroupFindName(name);
		} catch (Exception ex) {

		}

		user.send(rci);
	}
}
