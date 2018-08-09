package com.tumei.dto.arena;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/11/7 0007.
 *
 * 跨服竞技场 进入的时候获取的信息
 *
 */
public class ArenaInfo {
	/**
	 * 玩家当前的排名 [0, ...]
	 */
	public int rank;

	// 所有的对手信息
	public List<ArenaItemDto> peers = new ArrayList<ArenaItemDto>();
}
