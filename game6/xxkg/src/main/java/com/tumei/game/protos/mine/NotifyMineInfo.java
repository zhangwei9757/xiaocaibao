package com.tumei.game.protos.mine;

import com.tumei.game.protos.mine.structs.MineInfoStruct;
import com.tumei.game.protos.mine.structs.PlayerBehaveStruct;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * <p>
 * 通知进入视野的物体
 *
 */
@Component
public class NotifyMineInfo extends BaseProtocol {
	public int seq;

	/**
	 * 按照时间顺序添加的各种进入离开的消息
	 */
	public List<MineInfoStruct> protos = new ArrayList<>();

	/**
	 * 玩家动作通知:
	 * key:是玩家的ID
	 * value是行为，定义:
	 * 			-1 		离开
	 * 			0 		进入
	 * 			1-6 	六个方向的移动
	 * 			7		表示瞬移
	 */
	public List<PlayerBehaveStruct> ps = new ArrayList<>();
}
