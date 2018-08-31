package com.tumei.common.fight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/27 0027.
 */
public class GroupFightResult {
	/**
	 * 胜利方:
	 * 1: 左边
	 * 2: 右边
	 */
	public int win = 1;

	public List<Long> lifes = new ArrayList<>();

	/**
	 * 战斗数据
	 */
	public String data = "";
}
