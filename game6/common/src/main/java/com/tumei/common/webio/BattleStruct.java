package com.tumei.common.webio;

import com.tumei.common.fight.ArtifactStruct;
import com.tumei.common.fight.HeroStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/20 0020.
 * <p>
 * 战斗中真人数据, 公会副本使用
 */
public class BattleStruct {
	public int skin;
	public List<HeroStruct> roles = new ArrayList<>();
	public Map<Integer, Integer> buffs = new HashMap<>();
	public int[] lineups = new int[6];
	public List<ArtifactStruct> arts = new ArrayList<>();
}
