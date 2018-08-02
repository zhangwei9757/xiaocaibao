package com.tumei.game.protos.notifys;

import com.tumei.model.beans.guildbag.GuildbagStruct;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Administrator on 2018/07/31.
 */
@Component
public class NotifyGuildbag extends BaseProtocol {
	/**
	 * 未开启的所有红包
	 **/
	public List<GuildbagStruct> waitOpen;
	/**
	 * 开启后可领取的所有红包
	 **/
	public List<GuildbagStruct> waitReceive;
}
