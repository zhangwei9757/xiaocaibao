package com.tumei.game.protos.notifys;

import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 *
 * 通知宝物碎片被掠夺的信息
 *
 */
@Component
public class NotifyRobInfo extends BaseProtocol {
	// 被抢的碎片id
	public int item;

	public NotifyRobInfo() {}
	public NotifyRobInfo(int _item) {
		item = _item;
	}
}
