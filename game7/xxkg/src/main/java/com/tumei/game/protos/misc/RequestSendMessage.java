package com.tumei.game.protos.misc;

import com.tumei.common.DaoGame;
import com.tumei.common.RemoteService;
import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.game.protos.notifys.NotifyMessage;
import com.tumei.dto.MessageDto;
import com.tumei.model.GroupBean;
import com.tumei.model.RoleBean;
import com.tumei.websocket.WebSocketUser;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级英雄
 */
@Component
public class RequestSendMessage extends BaseProtocol {
	public int seq;

	/**
	 * 0: 本服务器内聊天
	 * 1: 全服聊天
	 * 2: 公会聊天
	 */
	public int mode;

	public String msg;

	public class ReturnSendMessage extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnSendMessage rci = new ReturnSendMessage();
		rci.seq = seq;

		if (mode == 1 && user.getVip() < 3) {
			rci.result = "VIP3以上玩家才能进行跨服发言.";
			user.send(rci);
			return;
		}

		RoleBean rb = user.getDao().findRole(user.getUid());
		long say = rb.getSaytime();
		if (say != 0) {
//			long now = System.currentTimeMillis();
//			if (say > now) {
				rci.result = ErrCode.禁止发言.name();
				user.send(rci);
				return;
//			}
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

		MessageDto ms = new MessageDto();
		ms.id = user.getUid();
		ms.icon = rb.getIcon();
		ms.vip = rb.getVip();
		ms.name = rb.getNickname();
		ms.flag = rb.getGrade();
		ms.msg = msg;
		ms.mode = mode;
		ms.zone = GameServer.getInstance().getZone();

		if (mode == 0) { // 普通聊天
			NotifyMessage nm = new NotifyMessage();
			nm.data.add(ms);
			NotifyMessage.push(ms);
			GameServer.getInstance().broadcast(nm);
		} else if (mode == 1) { // 全服聊天
			RemoteService.getInstance().sendAllMessage(ms, 0);
		} else if (mode == 2) { // 公会聊天
			GroupBean gb = DaoGame.getInstance().findGroup(user.getUid());
			RemoteService.getInstance().sendAllMessage(ms, gb.getGid());
		}

		user.send(rci);
	}
}
