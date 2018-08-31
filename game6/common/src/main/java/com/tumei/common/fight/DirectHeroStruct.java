package com.tumei.common.fight;

/**
 * Created by Administrator on 2017/2/20 0020.
 *
 * 直接复制英雄属性， 战斗输入英雄单元
 *
 */
public class DirectHeroStruct {
	/**
	 * 英雄id
	 */
	public int hero;
	/**
	 * 生命
	 */
	public long life;
	/**
	 * 攻击
	 */
	public int attack;
	/**
	 * 物理防御
	 */
	public int def;
	/**
	 * 法防
	 */
	public int mdef;
	/**
	 * 暴击
	 */
	public int critical;
	/**
	 * 抗暴
	 */
	public int antiCrit;
	/**
	 * 命中
	 */
	public int aim;
	/**
	 * 闪避
	 */
	public int dodge;
	/**
	 * 增伤: 增加自己的输出伤害
	 */
	public int enHarm;
	/**
	 * 减伤: 易伤的方面
	 */
	public int overHarm;

	public DirectHeroStruct() {}

}
