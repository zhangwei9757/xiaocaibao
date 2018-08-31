package com.tumei.game.protos.notifys;

import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class NotifySceneEvent extends BaseProtocol {
	/**
	 * 0: 小事件
	 * 1: 大事件
	 */
	public int event;
	/**
	 * 金币
	 */
	public int gold;
	/**
	 * 经验
	 */
	public int exp;
	/**
	 * 宝物
	 */
	public List<AwardBean> rewards = new ArrayList<>();

	/**
	 * 战斗信息
	 */
	public String data = "";
}
