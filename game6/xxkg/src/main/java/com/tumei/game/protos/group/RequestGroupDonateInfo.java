package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 公会捐献
 */
@Component
public class RequestGroupDonateInfo extends BaseProtocol {
	public int seq;

	class ReturnGroupDonateInfo extends BaseProtocol {
		public int seq;
		public int level;
		// 当前进度
		public int progress;
		// 自己剩下的捐献次数
		public int mode;
		// 4个刻度是否领取了奖励，1为领取，0为未领取
		public int[] awards;
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupDonateInfo rci = new ReturnGroupDonateInfo();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() <= 0) {
			rci.result = "请先加入一个公会";
			user.send(rci);
			return;
		}

		gb.flush(user.getVip());

		rci.mode = gb.getDonate();
		rci.awards = gb.getMarks();

		// 远程拉取公会推荐信息
		try {
			int rtn = RemoteService.getInstance().askGroupDonateProgress(gb.getGid(), user.getUid());
			if (rtn < 0) {
				rci.result = "你已经被踢出公会";
			} else {
				// 该调用返回的是   level * 10000 + progress;
				rci.progress = rtn % 10000;
				gb.setLevel(rtn / 10000);
				rci.level = gb.getLevel();
				gb.setProgress(rci.progress);
			}
		} catch (Exception ex) {
			rci.result = "公会服务维护中";
		}

		user.send(rci);
	}
}
