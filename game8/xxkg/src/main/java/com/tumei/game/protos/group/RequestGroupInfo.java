package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.common.group.GroupMessage;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会详细信息
 */
@Component
public class RequestGroupInfo extends BaseProtocol {
	public int seq;

	class ReturnGroupInfo extends BaseProtocol {
		public int seq;
		public String result = "";
		public GroupMessage info;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnGroupInfo rci = new ReturnGroupInfo();
		rci.seq = seq;

		if (user.getLevel() < Defs.公会等级) {
			rci.result = "领主等级不足";
			user.send(rci);
			return;
		}

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() <= 0) {
			user.send(rci);
			return;
		}

		// 远程拉取公会信息
		try {
			rci.info = RemoteService.getInstance().askGroupInfo(gb.getGid(), user.getUid());
			if (rci.info.gid == 0) { // 特别表示该成员不在此公会中
				gb.setGid(0);
				gb.setName("");
				rci.info = null;
				rci.result = "您已经不在该公会中，请重新加入公会";
			}
			else {
				gb.setGid(rci.info.gid); // 可能返回的不是当前表示gid
				gb.setName(rci.info.name);
				user.guildLevel = rci.info.level;
			}
		} catch (Exception ex) {
			rci.result = "公会服务维护中";
		}

		user.send(rci);
	}
}
