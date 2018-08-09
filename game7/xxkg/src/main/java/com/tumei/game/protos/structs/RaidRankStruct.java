package com.tumei.game.protos.structs;

import com.tumei.model.RaidRankBean;

/**
 * Created by Administrator on 2017/3/6 0006.
 * 远征排名
 */
public class RaidRankStruct {
	public Long id;
	/**
	 * 排名
	 * [1,...]
	 */
	public int rank;
	/**
	 * 名字
	 */
	public String name = "";
	/**
	 * 星数
	 */
	public int star;

	public RaidRankStruct() {}
	public RaidRankStruct(long id, int rank, String name) {
		this.id = id;
		this.rank = rank;
		this.name = name;
	}
	public RaidRankStruct(RaidRankBean rrb) {
		id = rrb.getId();
		name = rrb.getName();
		star = rrb.getStar();
		rank = rrb.getRank();
	}
}
