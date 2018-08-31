package com.tumei.game.protos.mine.structs;

import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;

/**
 * Created by Leon on 2017/7/25 0025.
 */
public class MineMonsterStruct {
	public int pos;

	/**
	 * 战斗开始前确定一个主英雄，真正战斗的时候确定其他信息
	 */
	public int hero;

	public int level;

	public MineMonsterStruct() {}

	public MineMonsterStruct(int pos, int level) {
		this.pos = pos;
		this.level = level;
		this.hero = Readonly.getInstance().randHero();
	}
}
