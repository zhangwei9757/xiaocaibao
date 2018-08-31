package com.tumei.model.structs;

import java.util.List;

/**
 * Created by Leon on 2017/5/10 0010.
 *
 * 群组成员
 */
public class GroupPreRole {
	public long id;
	public String name;
	public int icon;
	// 等级
	public int level;
	/**
	 * 0: 普通成员
	 * 8: 副会长
	 * 9: 会长
	 */
	public int gm;

	// 战力
	public long power;
	// 英雄列表
	public List<Integer> heros;
	// vip
	public int vip;

}
