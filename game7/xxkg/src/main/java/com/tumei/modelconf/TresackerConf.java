package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Tresacker")
public class TresackerConf {
	@Id
	public String ObjectId;

	public int key;
	// 抢夺机率
	public int[][] rate;
	// 胜利奖励
	public int[] reward;
}
