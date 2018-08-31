package com.tumei.game.protos.mine;

import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
@Component
public class RequestMineLook extends BaseProtocol {
	public int seq;
	public int pos;

	public static class Return extends BaseProtocol {
		public int seq;
		public String result = "";
	}
}
