package com.tumei.game.protos.notifys;

import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class NotifyUserInfo extends BaseProtocol {
	public int spirit;
	public int energy;

	public int gem;
	public long gold;

	public int level;
	public long exp;

	public long ts;
//	public HashMap<Integer, Integer> items = new HashMap<>();
}
