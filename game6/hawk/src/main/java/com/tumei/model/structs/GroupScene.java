package com.tumei.model.structs;

import com.tumei.common.fight.DirectHeroStruct;
import com.tumei.common.group.GroupSceneRoleStruct;
import com.tumei.common.webio.AwardStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Leon on 2017/5/23 0023.
 */
public class GroupScene {
	/**
	 * 当前公会副本的进度
	 */
	public int scene = 1;

	/**
	 * 血量进度
	 */
	public int[] progress = new int[4];

	/**
	 * 是否首杀
	 */
	public int[] firstKill = new int[4];

	/**
	 * 今日攻打次数与最高伤害列表
	 */
	public Map<Long, GroupSceneRoleStruct> roles = new HashMap<>();

	/**
	 * 公会副本当前章节的奖励
	 * <p>
	 * 最外层一共4个，对应的四个关卡
	 * <p>
	 * 每个关卡对应一个列表，包含了各种奖励个数，每次都随机从这些奖励个数中读取一个
	 */
	public List<List<AwardStruct>> awards = new ArrayList<>();

	/**
	 * 4个关卡的对手信息，因为一天内，这些对手的数值是递减的
	 */
	public List<List<DirectHeroStruct>> peers = new ArrayList<>();

	/**
	 * 每关对应的总血量
	 */
	public List<Long> totals = new ArrayList<>();
}
