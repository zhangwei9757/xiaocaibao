package com.tumei.dto.arena;

import com.tumei.common.fight.HeroStruct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/11/7 0007.
 *
 * 跨服竞技场 用于显示在界面上的玩家信息
 *
 */
public class ArenaItemDto {
	/**
	 * [0, ...]
	 */
	public int rank;
	public long uid;
	public String name;
	public int level;
	public int icon;
	public int grade;
	// 如果穿戴了时装，这里发送的是时装英雄的id，非时装本身
	public int fashion;
	public long power;
	public List<HeroStruct> formation = new ArrayList<HeroStruct>();
}
