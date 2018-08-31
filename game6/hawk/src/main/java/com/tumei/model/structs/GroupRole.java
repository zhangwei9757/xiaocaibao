package com.tumei.model.structs;

import com.tumei.common.group.GroupRoleMessage;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Created by Leon on 2017/5/10 0010.
 * <p>
 * 群组成员
 */
public class GroupRole {
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
	/**
	 * 今日贡献
	 */
	public int cb;

	/**
	 * 累计贡献
	 */
	public int cbs;

	/**
	 * 今日副本攻击次数
	 */
	public int sceneCount;

	/**
	 * 今日副本的攻击总伤害
	 */
	public int sceneHarm;

	/**
	 * 最近登录时间
	 */
	public LocalDateTime last;

	// 英雄列表
	public List<Integer> heros;

	public int vip;

}
