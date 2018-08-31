package com.tumei.game.protos.activity;

import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.Open7Bean;
import com.tumei.modelconf.FirstsevenConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestOpen7Get extends BaseProtocol {
	public int seq;
	// 领取第几日的奖励 [0, ...], 0是第一天
	public int day;
	// 0 登录奖励
	// 1 超值礼包
	// 2 任务1
	// 3 任务2
	public int mode;

	// 任务索引 [0,...
	public int task;
	// 任务对应进度索引 [0,...
	public int progress;

	class ReturnOpen7Get extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;
		ReturnOpen7Get rl = new ReturnOpen7Get();
		rl.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		ActivityBean ab = user.getDao().findActivity(user.getUid());

		if (!ab.flushOpen7(rb.getCreatetime())) {
			rl.result = "开服狂欢活动已经结束";
			user.send(rl);
			return;
		}

		int past = TimeUtil.pastDays(rb.getCreatetime());
		if (past < day) {
			rl.result = "登录天数不满足";
			user.send(rl);
			return;
		}

		FirstsevenConf fc = Readonly.getInstance().findFirstSevenConf(day);
		Open7Bean ob = ab.getOpen7()[day];
		if (mode == 0) {
			if (ob.logon != 0) {
				rl.result = "无法多次领取登录奖励";
				user.send(rl);
				return;
			}

			ob.logon = 1;
			rl.awards.addAll(user.addItems(fc.login, "开服狂欢"));
		}
		else if (mode == 1) { // 超值礼包
			PackBean pb = user.getDao().findPack(user.getUid());
			if (!pb.contains(fc.sale[0], fc.sale[1])) {
				rl.result = "钻石不足，无法购买超值礼包";
				user.send(rl);
				return;
			}
			user.payItem(fc.sale[0], fc.sale[1], "开服狂欢优惠礼包");
			if (ob.sale != 0) {
				rl.result = "无法多次购买超值礼包";
				user.send(rl);
				return;
			}
			ob.sale = 1;

			rl.awards.addAll(user.addItem(fc.sale[2], fc.sale[3], false, "开服狂欢"));
		}
		else if (mode == 2) { // 任务1
			List<Integer> ls = ob.task1.get(task);
			int p = ls.get(progress);
			if (p != -1) {
				rl.result = "任务未完成或已经领取";
				user.send(rl);
				return;
			}
			ls.set(progress, -2);
			int[] rwd = fc.rewards1[task];
			rl.awards.addAll(user.addItem(rwd[progress * 2], rwd[progress * 2 + 1], false, "开服狂欢"));
		}
		else if (mode == 3) { // 任务2
			List<Integer> ls = ob.task2.get(task);
			int p = ls.get(progress);
			if (p != -1) {
				rl.result = "任务未完成或已经领取";
				user.send(rl);
				return;
			}
			ls.set(progress, -2);
			int[] rwd = fc.rewards2[task];
			rl.awards.addAll(user.addItem(rwd[progress * 2], rwd[progress * 2 + 1], false, "开服狂欢"));
		}

		user.send(rl);
	}
}
