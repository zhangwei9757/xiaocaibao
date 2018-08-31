package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.common.group.GroupSimpleStruct;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupFindId extends BaseProtocol {
	public int seq;
	public long target;

	class ReturnGroupFindId extends BaseProtocol {
		public int seq;
		public GroupSimpleStruct group;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupFindId rci = new ReturnGroupFindId();
		rci.seq = seq;

		// 远程拉取公会推荐信息
		try {
			rci.group = RemoteService.getInstance().askGroupFindId(target);
		} catch (Exception ex) {
		}

		user.send(rci);
	}
}
