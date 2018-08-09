package com.tumei.game.protos.notifys;

import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 *
 * 游戏客户端顶部的消息
 *
 */
@Component
public class NotifyServerMessage extends BaseProtocol {
	public List<String> msg = new ArrayList<>() ;
}
