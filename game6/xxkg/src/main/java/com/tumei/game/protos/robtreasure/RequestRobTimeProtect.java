package com.tumei.game.protos.robtreasure;

import com.tumei.common.utils.Defs;
import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.RobBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.tumei.common.utils.Defs.白银免战牌;
import static com.tumei.common.utils.Defs.钻石;
import static com.tumei.common.utils.Defs.黄金免战牌;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestRobTimeProtect extends BaseProtocol {
	public int seq;
	/**
	 * 0: 使用
	 * 1: 购买
	 */
	public int mode;
	/**
	 * 1: 1小时
	 * 2: 8小时
	 */
	public int flag;
	class ReturnRobTimeProtect extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnRobTimeProtect rci = new ReturnRobTimeProtect();
		rci.seq = seq;

		PackBean pb = user.getDao().findPack(user.getUid());

		if (mode == 0) {
			RobBean rb = user.findRob();
			if (rb != null) {
				if (flag == 1) {
					rb.protect(3600);
					user.payItem(白银免战牌, 1, "使用");
				} else {
					rb.protect(3600 * 8);
					user.payItem(黄金免战牌, 1, "使用");
				}
			}
		} else if (mode == 1) {
			if (flag == 1) {
				if (!pb.contains(钻石, Defs.白银免战)) {
					rci.result = ErrCode.钻石不足.name();
				} else {
					user.payItem(钻石, Defs.白银免战, "购买免战");
					user.addItem(白银免战牌, 1, false, "免战");
				}
			} else if (flag == 2) {
				if (!pb.contains(钻石, Defs.黄金免战)) {
					rci.result = ErrCode.钻石不足.name();
				} else {
					user.payItem(钻石, Defs.黄金免战, "购买免战");
					user.addItem(黄金免战牌, 1, false, "免战");
				}
			}
		}

		user.send(rci);
	}
}
