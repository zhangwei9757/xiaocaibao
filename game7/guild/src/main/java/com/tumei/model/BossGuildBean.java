package com.tumei.model;

import com.tumei.dto.boss.BossGuildDto;

/**
 * Created by Leon on 2017/11/6 0006.
 */
public class BossGuildBean {
	private long id;

	private int rank;

	private String name;

	/**
	 * 总伤害
	 */
	private long harm;

	public BossGuildBean() {
	}

	public BossGuildBean(long gid, String name) {
		this.id = gid;
		this.name = name;
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

	public long getHarm() {
		return harm;
	}

	public void setHarm(long harm) {
		this.harm = harm;
	}

	public BossGuildDto createDto() {
		BossGuildDto dto = new BossGuildDto();
		dto.gid = this.id;
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
}

