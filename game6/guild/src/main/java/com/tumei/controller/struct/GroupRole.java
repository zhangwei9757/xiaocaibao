package com.tumei.controller.struct;

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

	public GroupRole() {
	}

	// 创建，自动加入
	public GroupRole(GroupRoleMessage grm) {
		id = grm.id;
		name = grm.name;
		icon = grm.icon;
		power = grm.power;
		heros = grm.heros;
		level = grm.level;
		vip = grm.vip;
	}

	// 预备役加入
	public GroupRole(GroupPreRole grm) {
		id = grm.id;
		name = grm.name;
		icon = grm.icon;
		power = grm.power;
		heros = grm.heros;
		level = grm.level;
		vip = grm.vip;
	}

	/**
	 * 玩家登录报告，更新公会中玩家的资料
	 *
	 * @param grm message when logon
	 */
	public void logon(GroupRoleMessage grm) {
		this.name = grm.name;
		this.icon = grm.icon;
		this.level = grm.level;
		this.power = grm.power;
		this.heros = grm.heros;
		this.vip = grm.vip;
		this.last = LocalDateTime.now();
	}

	/**
	 * 是否会长
	 *
	 * @return
	 */
	public boolean isLord() {
		return (gm == 9);
	}

	/**
	 * 是否副会长
	 *
	 * @return
	 */
	public boolean isVp() {
		return (gm == 8);
	}

	/**
	 * 是否副会长以上级别
	 *
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

	/**
	 * 每日刷新
	 */
	public void flush() {
		cb = 0;
		sceneCount = 0;
		sceneHarm = 0;
	}

	/**
	 * 用于通知给其他服务器，构建结构
	 */
	public GroupRoleMessage createGroupRoleMessage() {
		GroupRoleMessage grm = new GroupRoleMessage();
		grm.id = this.id;
		grm.name = this.name;
		grm.icon = this.icon;
		grm.level = this.level;
		grm.power = this.power;
		grm.gm = this.gm;
		if (this.last != null) {
			grm.last = this.last.toEpochSecond(ZoneOffset.ofHours(8));
		}
		grm.cb = this.cb;
		grm.cbs = this.cbs;
		if (this.heros != null) {
			grm.heros.addAll(this.heros);
		}
		grm.vip = this.vip;
		return grm;
	}

}
