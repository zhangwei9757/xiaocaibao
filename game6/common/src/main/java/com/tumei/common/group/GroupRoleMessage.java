package com.tumei.common.group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/5/10 0010.
 * <p>
 * 群组成员
 */
public class GroupRoleMessage {
	public long id;
	public String name = "";
	public int icon;
	public int level;
	// 战力
	public long power;
	// 公会权限
	public int gm;
	public long last;

	// 今日贡献
	public int cb;
	// 累计贡献
	public int cbs;
	public int vip;

	// 英雄列表
	public List<Integer> heros = new ArrayList<>();

	public GroupRoleMessage() {}
}
