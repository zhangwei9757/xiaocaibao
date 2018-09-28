package com.tumei.common.fight;

/**
 * Created by Administrator on 2017/2/20 0020.
 * <p>
 * 请求模拟战斗的时候传入的双方数据
 */
public class FightStruct {
	public HerosStruct left;
	public HerosStruct right;

	// 衰弱比例, 天梯赛中第一次遇到
	public int weak;

	public FightStruct() { }
}
