package com.tumei.game.protos.activity;

import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.ChargeBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.ChargeDayBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.SignConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by leon on 2016/12/31.
 * 请求签到今日
 *
 */
@Component
public class RequestSignToday extends BaseProtocol {
    public int seq;
	/**
	 * 0: 默认签到
	 * 1: 豪华签到
	 */
	public int mode;

    class ReturnSignToday extends BaseProtocol {
		public int seq;
		public String result = "";
		// 签到之后的状态,1:签到，有双倍未获取，2：全部签到ok
		public int status;
		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnSignToday rl = new ReturnSignToday();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		int nowState = ab.getSignState();
		List<SignConf> scs = Readonly.getInstance().getSignConfs();
		int idx = ab.getSignDays() % scs.size();
		SignConf sc = scs.get(idx);


		if (mode == 0) {
			if (nowState == 2) {
				rl.result = "今日已经签到";
				user.send(rl);
				return;
			}


			int count = 1;
			if (sc.twice > 0) { // 有双倍，判断vip
				RoleBean rb = user.getDao().findRole(user.getUid());
				if (sc.twice <= rb.getVip()) {
					if (ab.getSignState() == 0) {
						count = 2;
					}
					ab.setSignState(2);
				} else {
					ab.setSignState(1);
				}
			} else {
				ab.setSignState(2);
			}
			// 增加奖励
			rl.awards.addAll(user.addItem(sc.reward1[0], sc.reward1[1] * count, false, "签到"));
			rl.awards.addAll(user.addItem(sc.reward2[0], sc.reward2[1] * count, false, "签到"));
			rl.status = ab.getSignState() % 10;

		} else { // 豪华签到
			if (ab.getSignState() >= 10) {
				rl.result = "今日已经豪华签到";
				user.send(rl);
				return;
			}

			ChargeBean cb = user.getDao().findCharge(user.getUid());
			int today = TimeUtil.getToday();
			int rmb = 0;
			Optional<ChargeDayBean> opt = cb.getDayCharges().stream().filter(cdb -> cdb.day == today).findFirst();
			if (opt.isPresent()) {
				rmb = opt.get().rmb;
			}

			if (rmb >= sc.cost) {
				rl.awards.addAll(user.addItems(sc.reward3, 1, false, "豪华签到"));
				ab.setSignState(10);
			} else {
				rl.result = "今日充值金额不足";
			}
		}
        user.send(rl);
    }
}
