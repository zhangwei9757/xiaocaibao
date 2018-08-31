package com.tumei.game.protos.activity;

import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.SevenConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求签到今日
 *
 */
@Component
public class RequestSevenToday extends BaseProtocol {
    public int seq;
	/**
	 * 0: 默认登录领取
	 *
	 * 1- n 如果是需要选择的，则mode不能传0给服务器，需要传递选择的第几个
	 */
	public int mode;

	//领取第几天的 [0,6]
	public int index;

    class ReturnSevenToday extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnSevenToday rl = new ReturnSevenToday();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		int[] status = ab.getSevenDays();

		RoleBean rb = user.getDao().findRole(user.getUid());
		int day = TimeUtil.pastDays(rb.getCreatetime());

		int idx = day % 7; // 今天是这个周期的哪一天[0,6]
		if (idx < index) {
			rl.result = "无法领取";
			user.send(rl);
			return;
		}

		if (status[index] != 0) {
			rl.result = "登录奖励已经领取";
			user.send(rl);
			return;
		}

		List<SevenConf> scs = Readonly.getInstance().getSevenConfs();
		int today = (day % scs.size()) - (idx - index);
		SevenConf sc = scs.get(today);

		if (sc.mode == 1) { // 需要选择，
			if (mode <= 0 || mode > sc.reward.length) {
				rl.result = "客户端选择正确奖励";
				user.send(rl);
				return;
			} else {
				rl.awards.addAll(user.addItems(sc.reward[mode - 1], "七日"));
			}
		} else {
			for (int[] items : sc.reward) {
				rl.awards.addAll(user.addItems(items, "七日"));
			}
		}

		status[index] = 1;
        user.send(rl);
    }
}
