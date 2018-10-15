package com.tumei.model;

import com.tumei.dto.boss.BossRoleDto;

/**
 * Created by Leon on 2017/11/6 0006.
 */
public class BossRoleBean {
	private long id;

	private String name;

	private int level;

	/**
	 * 1-50, 0:标识没有排名
	 */
	private int rank;

	// 战斗力
	private long power;

	private long gid;

	private String guild;

	/**
	 * 总伤害
	 */
	private long harm;

	/**
	 * 最高伤害
	 */
	private long topharm;

	public BossRoleBean() {}

	public BossRoleBean(long uid, String name, long gid, String guild) {
		this.id = uid;
		this.name = name;
		this.gid = gid;
		this.guild = guild;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getPower() {
		return power;
	}

	public void setPower(long power) {
		this.power = power;
	}

	public long getGid() {
		return gid;
	}

	public void setGid(long gid) {
		this.gid = gid;
	}

	public String getGuild() {
		return guild;
	}

	public void setGuild(String guild) {
		this.guild = guild;
	}

	public long getHarm() {
		return harm;
	}

	public void setHarm(long harm) {
		this.harm = harm;
	}

	public BossRoleDto createDto() {
		BossRoleDto dto = new BossRoleDto();
		dto.uid = this.id;
		dto.guild = this.guild;
		dto.harm = this.harm;
		dto.name = this.name;
		return dto;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getTopharm() {
		return topharm;
	}

	public void setTopharm(long topharm) {
		this.topharm = topharm;
	}
}

