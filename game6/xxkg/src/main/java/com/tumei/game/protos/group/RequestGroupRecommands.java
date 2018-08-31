package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.common.group.GroupSimpleStruct;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupRecommands extends BaseProtocol {
	public int seq;

	class ReturnGroupRecommands extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<GroupSimpleStruct> groups;
		// 剩余可加入的时间
		public int wait;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupRecommands rci = new ReturnGroupRecommands();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			user.send(rci);
			return;
		}

		Date now = new Date();
		Date last = gb.getLeaveDay();
		if (last != null) {
			rci.wait = (int)((last.getTime() - now.getTime())/1000);
		}

		// 远程拉取公会推荐信息
		try {
			rci.groups = RemoteService.getInstance().askGroupRecommands(user.getServer().getZone());
		} catch (Exception ex) {
			rci.result = "公会服务维护中";
		}

		user.send(rci);
	}
}
