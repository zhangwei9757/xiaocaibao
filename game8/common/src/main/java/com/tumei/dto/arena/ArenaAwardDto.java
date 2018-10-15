package com.tumei.dto.arena;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/11/7 0007.
 *
 * 跨服竞技场 进入的时候获取的信息
 *
 */
public class ArenaAwardDto {
	/**
	 * 竞技场奖励
	 */
	public int[] awds;

	// 玩家列表
	public List<Long> roles = new ArrayList<>();
}
