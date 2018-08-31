package com.tumei.game.protos.mine;

import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
@Component
public class RequestMineMove extends BaseProtocol {
	public int seq;

	/**
	 * 1-6 方向
	 *
	 * 7 瞬移
	 */
	public int dir;

	/**
	 * 坐标
	 */
	public int pos;

	public static class Return extends BaseProtocol {
		public int seq;
		public String result = "";
		public int energy;
		public int pos;
	}
}
