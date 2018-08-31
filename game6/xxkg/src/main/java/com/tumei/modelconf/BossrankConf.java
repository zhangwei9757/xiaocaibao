package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Bossrank")
public class BossrankConf {
	@Id
	public String objectId;

	// 排名的起始[1...]
	public int key;

	public int rank;

	// 单人伤害奖励
	public int[] reward1;
	// 公会伤害奖励
	public int[] reward2;
	// 击杀奖励
	public int[] reward3;
}
