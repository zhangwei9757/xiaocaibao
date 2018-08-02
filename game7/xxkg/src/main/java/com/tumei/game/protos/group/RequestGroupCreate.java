package com.tumei.game.protos.group;

import com.google.common.base.Strings;
import com.tumei.common.RemoteService;
import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.common.group.GroupReturn;
import com.tumei.common.group.GroupRoleMessage;
import com.tumei.model.GroupBean;
import com.tumei.model.PackBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.公会卡;
import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 创建公会
 */
@Component
public class RequestGroupCreate extends BaseProtocol {
	public int seq;

	public String name;
	public int icon;
	// 审核方式
	public int approval;

	class ReturnGroupCreate extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupCreate rci = new ReturnGroupCreate();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			rci.result = "已在公会中的成员不能创建新的公会";
			user.send(rci);
			return;
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		if (!pb.contains(公会卡, 1)) {
			rci.result = ErrCode.钻石不足.name();
			user.send(rci);
			return;
		}

		GroupRoleMessage grm = user.createGroupRole();
		GroupReturn rtn = RemoteService.getInstance().askGroupCreate(grm, name, icon, approval);
		if (Strings.isNullOrEmpty(rtn.result)) {
			gb.setGid(rtn.group);
			user.payItem(公会卡, 1, "创建公会");
		}

		rci.result = rtn.result;
		user.send(rci);
	}
}
