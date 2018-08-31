package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.common.webio.AwardStruct;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupGetSceneAwards extends BaseProtocol {
	public int seq;
	// 阵营[1,4]
	public int index;

	class ReturnGroupGetSceneAwards extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupGetSceneAwards rl = new ReturnGroupGetSceneAwards();
		rl.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			try {
				if (gb.getFetch()[index - 1] == 1) {
					rl.result = "奖励已经领取";
				} else {
					// 远程控制副本进度和当前副本关卡
					AwardStruct gss = RemoteService.getInstance().askGroupGetSceneAwards(gb.getGid(), user.getUid(), index);
					if (gss != null) {
						gb.getFetch()[index - 1] = 1;
						rl.awards.addAll(user.addItem(gss.id, gss.count, false, "公会阵营奖励"));
					} else {
						rl.result = "无法获取本章副本对应阵营的奖励";
					}
				}
			} catch (Exception ex) {
				rl.result = "无法获取本章副本对应阵营的奖励";
			}
		}

		user.send(rl);
	}
}
