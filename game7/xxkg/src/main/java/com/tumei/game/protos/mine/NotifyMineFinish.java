package com.tumei.game.protos.mine;

import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * <p>
 * 通知矿脉结束
 */
@Component
public class NotifyMineFinish extends BaseProtocol {
	/**
	 * 矿的配置id
	 */
	public int key;

	/**
	 * 被打随机的位置
	 */
	public int rebirth;

	/**
	 * 如果是被抢夺这个信息才会有
	 */
	public MineRobInfo info;

	public List<Integer> awards = new ArrayList<>();

	public NotifyMineFinish() { }
}
