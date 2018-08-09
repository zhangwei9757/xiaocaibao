package com.tumei.dto.arena;

/**
 * Created by Leon on 2017/11/15 0015.
 */
public class ArenaFightResult {
	public String reason = "";

	public boolean win;

	// 是否打破原来的最高记录
	public boolean br;

	// rank, 新的名次
	public int rank;

	// 上升的名次
	public int up;

	// 战斗记录
	public String data = "";
}
