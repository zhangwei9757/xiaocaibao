package com.tumei.game.protos.mine.structs;

import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.modelconf.MineChestConf;

/**
 * Created by Leon on 2017/7/25 0025.
 */
public class MineTreasureStruct {
	/**
	 * 坐标
	 */
	public int pos;

	public int chest;

	public int level;

	public MineTreasureStruct() {}

	public MineTreasureStruct(int pos, int level) {
		this.pos = pos;
		this.level = level;
		MineChestConf mcc = Readonly.getInstance().getMineChestConfs().get(level - 1);
		this.chest = RandomUtil.getInArray(mcc.chest);
	}
}
