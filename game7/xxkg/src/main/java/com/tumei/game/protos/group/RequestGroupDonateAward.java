package com.tumei.game.protos.group;

import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DonaterewardConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 公会捐献
 */
@Component
public class RequestGroupDonateAward extends BaseProtocol {
	public int seq;

	class ReturnGroupDonateAward extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupDonateAward rci = new ReturnGroupDonateAward();
		rci.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() <= 0) {
			rci.result = "请先加入一个公会";
			user.send(rci);
			return;
		}

		gb.flush(user.getVip());

		int[] marks = gb.getMarks();
		int fd = -1;
		for (int i = 0; i < 4; ++i) {
			if (marks[i] == 0) {
				fd = i + 1;
				break;
			}
		}

		if (fd == -1) {
			rci.result = "全部奖励已经领取";
			user.send(rci);
			return;
		}

		// 根据DonateInfo协议获取的公会等级与公会进度，进行计算奖励
		DonaterewardConf drc = Readonly.getInstance().findDonatereward(fd);

		int[] awards;
		if (gb.getLevel() < 3) {
			awards = drc.guild1;
		} else if (gb.getLevel() < 6) {
			awards = drc.guild2;
		} else {
			awards = drc.guild3;
		}

		if (gb.getProgress() < awards[0]) {
			rci.result = "当前公会等级所需的进度未达成";
			user.send(rci);
			return;
		}

		marks[fd - 1] = 1;
		rci.awards.addAll(user.addItem(awards[1], awards[2], false, "捐献奖励"));
		user.send(rci);
	}
}
