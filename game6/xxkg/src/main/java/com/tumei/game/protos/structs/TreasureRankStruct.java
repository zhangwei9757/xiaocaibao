package com.tumei.game.protos.structs;

import com.tumei.model.RaidRankBean;
import com.tumei.model.TreasureRankBean;

/**
 * Created by Administrator on 2017/3/6 0006.
 * 神秘宝藏排名
 */
public class TreasureRankStruct {
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
	 * 积分
	 */
	public int score;

	public TreasureRankStruct() {}

	public TreasureRankStruct(long id, int rank, String name) {
		this.id = id;
		this.rank = rank;
		this.name = name;
	}

	public TreasureRankStruct(TreasureRankBean rrb) {
		id = rrb.getId();
		name = rrb.getName();
		rank = rrb.getRank();
		score = rrb.getScore();
	}
}
