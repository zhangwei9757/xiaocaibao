package com.tumei.controller.struct;

import com.tumei.common.group.GroupRoleMessage;

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

	public GroupPreRole() {}

	public GroupPreRole(GroupRoleMessage grm) {
		id = grm.id;
		name = grm.name;
		icon = grm.icon;
		power = grm.power;
		heros = grm.heros;
		vip = grm.vip;
		level = grm.level;
	}

	/**
	 * 是否会长
	 * @return
	 */
	public boolean isLord() {
		return (gm == 9);
	}
	/**
	 * 是否副会长
	 * @return
	 */
	public boolean isVp() {
		return (gm == 8);
	}
	/**
	 * 是否副会长以上级别
	 * @return
	 */
	public boolean isVpAbove() {
		return (gm >= 8);
	}

	public void setLord() {
		gm = 9;
	}
	public void setVp() {
		gm = 8;
	}
	public void setNormal() {
		gm = 0;
	}

	public GroupRoleMessage createGroupRoleMessage() {
		GroupRoleMessage grm = new GroupRoleMessage();
		grm.id = this.id;
		grm.name = this.name;
		grm.icon = this.icon;
		grm.level = this.level;
		grm.power = this.power;
		grm.heros.addAll(this.heros);
		grm.vip = this.vip;
		return grm;
	}
}
