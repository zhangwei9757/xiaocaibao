package com.tumei.game.protos.activity;

import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.ChargeBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.ChargeDayBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by leon on 2016/12/31.
 * 请求七日登录信息
 *
 */
@Component
public class RequestSevenInfo extends BaseProtocol {
    public int seq;

    class ReturnSevenInfo extends BaseProtocol {
		public int seq;
		// 距离帐号创建的天数, 创建当天是0开始, 在配置表中每七天一个循环，today = (days % 配置长度) 是今天的配置， 本周期第一天floor(today /7)*7
		public int days;
		// 一定是7个数字，0表示那天没有登录
		public List<Integer> status = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnSevenInfo rl = new ReturnSevenInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());

		RoleBean rb = user.getDao().findRole(user.getUid());
		// 刷新
		int day = TimeUtil.pastDays(rb.getCreatetime());
		int round = day / 7;

		int[] status = ab.getSevenDays();

		if (ab.getSevenType() != round) {
			ab.setSevenType(round);
			for (int i = 0; i < 7; ++i) {
				status[i] = 0;
			}
		}

		Arrays.stream(status).forEach(s -> rl.status.add(s));
		rl.days = day;

        user.send(rl);
    }
}
