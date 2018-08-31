package com.tumei.game.protos.mine.structs;

import com.tumei.model.MineBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/7/26 0026.
 * <p>
 * 个人任务相关的信息 + 矿  定时通知
 */
public class MineInfoStruct {
	/**
	 * 宝箱
	 */
	public List<MineTreasureStruct> treasures = new ArrayList<>();

	/**
	 * 怪物
	 */
	public List<MineMonsterStruct> monsters = new ArrayList<>();

	/**
	 * 商人
	 */
	public List<MineMerchantStruct> merchants = new ArrayList<>();

	/**
	 * 矿
	 */
	public List<MineBean> mines = new ArrayList<>();
}
