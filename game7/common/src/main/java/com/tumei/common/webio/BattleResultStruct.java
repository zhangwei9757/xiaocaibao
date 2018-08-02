package com.tumei.common.webio;

/**
 * Created by Administrator on 2017/2/20 0020.
 * <p>
 * 战斗中真人数据
 */
public class BattleResultStruct {
	public String result = "";
	// 具体战斗数据
	public String data = "";
	// 是否击杀 1:击杀，0：未击杀 2:首次击杀
	public int kill;
	// 随机贡献
	public int rCon;
	// 造成的伤害
	public long harm;
}
