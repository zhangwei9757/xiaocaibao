package com.tumei.dto.arena;

import com.tumei.common.fight.ArtifactStruct;
import com.tumei.common.fight.HeroStruct;
import com.tumei.common.fight.HerosStruct;

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
	public long power;
	public HerosStruct info;
}
