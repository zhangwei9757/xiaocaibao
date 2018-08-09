package com.tumei.game.protos.structs;

import com.tumei.model.beans.AwardBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
public class OfflineAwardStruct {
	public int scene;
	/**
	 * 间隔时间，单位秒
	 */
	public int period;
	/**
	 * 获得的金币
	 */
	public long gold;
	/**
	 * 获得的经验
	 */
	public int exp;
	/**
	 * 触发战斗事件多少次
	 */
	public int events;
	/**
	 * 触发Boss大事件次数
	 */
	public int boss;
	/**
	 * 时间段的道具奖励
	 */
	public List<AwardBean> awards = new ArrayList<>();
}
