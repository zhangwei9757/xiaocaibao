package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.common.webio.AwardStruct;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupSceneAwards extends BaseProtocol {
	public int seq;
	// 阵营[1,4]
	public int index;

	class ReturnGroupSceneAwards extends BaseProtocol {
		public int seq;
		public List<AwardStruct> awards;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupSceneAwards rl = new ReturnGroupSceneAwards();
		rl.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			try {
				// 远程控制副本进度和当前副本关卡
				List<AwardStruct> gss = RemoteService.getInstance().askGroupSceneAwards(gb.getGid(), user.getUid(), index);
				if (gss != null) {
					rl.awards = gss;
				}
			} catch (Exception ex) {

			}
		}

		user.send(rl);
	}
}
