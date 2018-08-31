package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.Open7Bean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Created by leon on 2016/12/31.
 * 开服狂欢7天
 */
@Component
public class RequestOpen7 extends BaseProtocol {
	public int seq;

	class ReturnOpen7 extends BaseProtocol {
		public int seq;
		public long open; // 开服时间
		public Open7Bean[] info;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;
		ReturnOpen7 rl = new ReturnOpen7();
		rl.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushOpen7(rb.getCreatetime());

		rl.info = ab.getOpen7();

		Instant instance = Instant.ofEpochMilli(rb.getCreatetime().getTime());
		LocalDateTime ldt = LocalDateTime.ofInstant(instance, ZoneId.systemDefault());
		rl.open = ldt.toEpochSecond(ZoneOffset.ofHours(8));

		user.send(rl);
	}
}
