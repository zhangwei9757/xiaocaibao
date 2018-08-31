package com.tumei.dto.boss;

/**
 * Created by Leon on 2018/2/22.
 *
 * 玩家请求boss信息,返回
 *
 */
public class BossDto {
	// boss当前血量
	public long life;
	// boss等级
	public int level;
	// 玩家排名
	public int rank;
	// 玩家所属公会排名
	public int grank;
	// 玩家累计伤害
	public long harm;
	// 玩家单次最高伤害
	public long topharm;
	// 击杀者
	public String killer;
}
