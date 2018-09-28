package com.tumei.common.fight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/27 0027.
 */
public class FightResult {
	/**
	 * 胜利方:
	 * 1: 左边
	 * 2: 右边
	 */
	public int win = 0;

	/**
	 * 战斗数据
	 */
	public String data = "";

	public List<Long> lifes = new ArrayList<>();
}
