package com.tumei.modelconf.happy;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Soulrank")
public class SoulrankConf {
	@Id
	public String id;

	public int key;
	// 排名奖励1
	public int[] reward1;
	public int limit;
	// 累计奖励2
	public int[] reward2;
	public int limit3;
	// 累计奖励3
	public int[] reward3;
}
