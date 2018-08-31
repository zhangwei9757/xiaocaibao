package com.tumei.game.protos.mine;

import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
@Component
public class RequestMineAccelerate extends BaseProtocol {
	public int seq;
	// 矿位置
	public int pos;
	// 矿等级
	public int level;

	public static class Return extends BaseProtocol {
		public int seq;
		public int gem;
		public String result = "";
	}
}
