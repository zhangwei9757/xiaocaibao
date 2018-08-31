package com.tumei.game.protos.misc;

import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.game.protos.notifys.NotifyMessage;
import com.tumei.game.protos.structs.MessageStruct;
import com.tumei.model.RoleBean;
import com.tumei.websocket.SessionUser;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级英雄
 */
@Component
public class RequestSendMessage extends BaseProtocol {
	public int seq;

	public String msg;

	public class ReturnSendMessage extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnSendMessage rci = new ReturnSendMessage();
		rci.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		long say = rb.getSaytime();
		if (say != 0) {
			long now = System.currentTimeMillis();
			if (say > now) {
				rci.result = ErrCode.禁止发言.name();
				user.send(rci);
				return;
			}
		}

		if ((msg.contains("退") && msg.contains("款"))) {
			rci.result = "敏感字消息不能发送";
			user.send(rci);
			return;
		}

		if (!user.judegeProtocolInterval(this, 2)) {
			rci.result = "不能频繁发言!";
			user.send(rci);
			return;
		}

		NotifyMessage nm = new NotifyMessage();

		MessageStruct ms = new MessageStruct();
		ms.id = user.getUid();

		NotifyMessage.push(ms);

		ms.icon = rb.getIcon();
		ms.vip = rb.getVip();
		ms.name = rb.getNickname();
		ms.flag = rb.getGrade();
		ms.msg = msg;
		nm.data.add(ms);

		user.getServer().broadcast(nm);

		user.send(rci);
	}
}
