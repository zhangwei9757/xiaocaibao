package com.tumei.game.protos.group;

import com.google.common.base.Strings;
import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 创建公会
 */
@Component
public class RequestGroupLeave extends BaseProtocol {
	public int seq;

	class ReturnGroupLeave extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupLeave rci = new ReturnGroupLeave();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() <= 0) {
			user.send(rci);
			return;
		}

		gb.setLeaveDay(new Date(System.currentTimeMillis() + 3600 * 1000 * 4));

		rci.result = RemoteService.getInstance().askGroupLeave(gb.getGid(), user.getUid());
		if (Strings.isNullOrEmpty(rci.result)) {
			gb.setGid(0);
			gb.setName("");
		}

		user.send(rci);
	}
}
