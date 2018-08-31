package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Raid")
public class RaidConf {
	@Id
	public String ObjectId;

	public int key;
	/**
	 * 推荐战斗力
	 */
	public long power;
	/**
	 * 挑战奖励1 - 5
	 */
	public int[] reward1;
	public int[] reward2;
	public int[] reward3;
	public int[] reward4;
	public int[] reward5;

	/**
	 * 挂机金币
	 */
	public int gold;
	/**
	 * 挂机经验
	 */
	public int exp;
	/**
	 * 挑战消耗: 挑战一次消耗的能量
	 */
	public int cost;
	/**
	 * 奖励间隔
	 */
	public int cd;
	/**
	 * 关卡宝箱
	 */
	public int[] chest;
	/**
	 * 事件掉落
	 */
	public int[] drop;
	/**
	 * 扫荡出货机率
	 */
	public int addrate;
	/**
	 * 扫荡额外掉落
	 */
	public int[] sweep;
	/**
	 * 关卡代表
	 */
	public int boss;

	/**
	 * 以下为六个站位的守卫:
	 *
	 * details:生命 攻击 物防 法防 暴击 命中 抗暴 闪避 的比例
	 *
	 */
	public int[] guard;
	public int[] details;

	public long hp;
	public int attack;
	public int defence1;
	public int defence2;
	public int crit;
	public int hit;
	public int critoff;
	public int dog;
	public int increase;
	public int reduce;

}
