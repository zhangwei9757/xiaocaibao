package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 修改成员权限
 */
@Component
public class RequestGroupModify extends BaseProtocol {
	public int seq;
	public long target;
	/**
	 * 1: 提升
	 * 2: 降低
	 * 3: 踢掉
	 * 4: 弹劾
	 */
	public int mode;

	class ReturnGroupModify extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupModify rci = new ReturnGroupModify();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() <= 0) {
			rci.result = "请先加入一个公会";
			user.send(rci);
			return;
		}

		// 远程拉取公会推荐信息
		try {
			rci.result = RemoteService.getInstance().askGroupModify(gb.getGid(), user.getUid(), target, mode);
		} catch (Exception ex) {
			rci.result = "公会服务维护中";
		}

		user.send(rci);
	}
}
