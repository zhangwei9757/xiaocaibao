package com.tumei.model.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
public class ChargeDayBean {
	/**
	 * 20170203
	 */
	public int day;
	/**
	 * 单位 分
	 */
	public int rmb;

	public List<Integer> rmbs = new ArrayList<>();

	public ChargeDayBean() {}
	public ChargeDayBean(int _day, int _rmb) {
		day = _day;
		rmb = _rmb;
		if (rmb > 0) {
			rmbs.add(rmb);
		}
	}
}
