package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Arenarewards")
public class ArenarewardConf {
	@Id
	public String ObjectId;

	public int key;
	public int rank;
	public int[] rankreward;
	/**
	 * 挑战胜利奖励
	 */
	public int[] win;
}

