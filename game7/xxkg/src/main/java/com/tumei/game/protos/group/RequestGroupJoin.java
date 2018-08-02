package com.tumei.game.protos.group;

import com.google.common.base.Strings;
import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.common.group.GroupRoleMessage;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 创建公会
 */
@Component
public class RequestGroupJoin extends BaseProtocol {
	public int seq;

	public long gid;

	class ReturnGroupJoin extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupJoin rci = new ReturnGroupJoin();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			rci.result = "已在公会中的成员不能加入公会";
			user.send(rci);
			return;
		}

		if (gb.getLeaveDay() != null) {
			if (System.currentTimeMillis() < gb.getLeaveDay().getTime()) {
				rci.result = "离开公会后4小时后才能加入";
				user.send(rci);
				return;
			}
		}

		GroupRoleMessage grm = user.createGroupRole();

		String rtn = RemoteService.getInstance().askGroupJoin(grm, gid);
		if (Strings.isNullOrEmpty(rtn)) {
			gb.setGid(gid);
			gb.setLeaveDay(new Date(System.currentTimeMillis() + 3600 * 1000 * 4));
		} else if (rtn.startsWith("other")){
			long gid = Long.parseLong(rtn.substring(6));
			gb.setGid(gid);
			rtn = "玩家已经在其他公会，请刷新公会界面";
		}

		rci.result = rtn;
		user.send(rci);
	}
}
