package com.tumei.model.beans.mine;

import com.tumei.game.protos.mine.MineRobInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/7/25 0025.
 */
public class MineAwardBean {
	/**
	 * 矿脉信息
	 */
	public int key = 0;
	/**
	 * 抢劫信息
	 */
	public MineRobInfo info = null;

	/**
	 * 必须领取的奖励
	 */
	public List<Integer> awards = new ArrayList<>();

	public MineAwardBean() { }

}
