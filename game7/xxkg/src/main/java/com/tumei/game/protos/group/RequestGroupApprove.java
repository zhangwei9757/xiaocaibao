package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 审核公会申请
 */
@Component
public class RequestGroupApprove extends BaseProtocol {
	public int seq;
	public long target;
	/**
	 * 1: 批准
	 * 2: 拒绝
	 */
	public int mode;

	class ReturnGroupApprove extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnGroupApprove rci = new ReturnGroupApprove();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() <= 0) {
			rci.result = "当前不属于公会成员";
			user.send(rci);
			return;
		}

		// 远程拉取公会信息
		rci.result = RemoteService.getInstance().askGroupApprove(gb.getGid(), user.getUid(), target, mode);

		user.send(rci);
	}
}
