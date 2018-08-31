package com.tumei.dto.arena;

import com.tumei.common.fight.ArtifactStruct;
import com.tumei.common.fight.HeroStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Leon on 2017/11/7 0007.
 *
 * 跨服竞技场 提交的 玩家信息结构
 *
 */
public class ArenaRoleDto {
	public long uid;
	public String name;
	public int level;
	public int icon;
	public int grade;
	// 如果穿戴了时装，这里发送的是时装英雄的id，非时装本身
	public int fashion;
	public long power;
	public int[] lineups = new int[6];
	public List<ArtifactStruct> arts = new ArrayList<>();
	
	// 阵形
	public List<HeroStruct> formation = new ArrayList<>();
	public Map<Integer, Integer> buffs = new HashMap<Integer, Integer>();
}
