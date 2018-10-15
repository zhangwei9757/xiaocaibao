package com.tumei.game.protos.daily;

import com.tumei.game.GameUser;
import com.tumei.model.DailyTaskBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.TaskItemBean;
import com.tumei.modelconf.DailyConf;
import com.tumei.websocket.WebSocketUser;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestDailyAward extends BaseProtocol {
	public int seq;

	/**
	 * 0: 日常任务宝箱
	 * 1: 积分进度宝箱
	 */
	public int mode;

	/**
	 * mode为0时，发送日常任务id[1,...]
	 * mode为1时，可选择[1,4]
	 */
	public int tid;

	class ReturnDailyAward extends BaseProtocol {
		public int seq;
		public String result = "";
		public int score;
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;
		ReturnDailyAward rl = new ReturnDailyAward();
		rl.seq = seq;

		DailyTaskBean dtb = user.flushDailyTask();

		if (mode == 0) {
			TaskItemBean tib = dtb.getTasks().get(tid - 1);
			if (tib.status != 1) {
				rl.result = "日常任务状态不是<已完成>";
				user.send(rl);
				return;
			}

			tib.status = 2;
			DailyConf dc = Readonly.getInstance().findDailyTask(tib.tid);
			if (dc != null) {
				rl.awards.addAll(user.addItems(dc.reward, "日常任务"));
				rl.score = dc.score;
				dtb.setScore(dtb.getScore() + dc.score);
			}
		}
		else if (mode == 1) {
			DailyConf dc = Readonly.getInstance().findDailyTask(1);

			if (tid <= dtb.getScoreAwardProgress()) {
				rl.result = "已经领取过这个积分奖励";
				user.send(rl);
				return;
			}

			int[] awds = null;
			switch (tid) {
				case 1:
					if (dtb.getScore() >= 30) {
						awds = dc.reward30;
					}
					break;
				case 2:
					if (dtb.getScore() >= 60) {
						awds = dc.reward60;
					}
					break;
				case 3:
					if (dtb.getScore() >= 90) {
						awds = dc.reward90;
					}
					break;

				case 4:
					if (dtb.getScore() >= 120) {
						awds = dc.reward120;
					}
					break;
			}

			if (awds == null) {
				rl.result = "积分不足";
				user.send(rl);
				return;
			}

			dtb.setScoreAwardProgress(tid);
			rl.awards.addAll(user.addItems(awds, "日常积分"));
		}


		user.send(rl);
	}
}
