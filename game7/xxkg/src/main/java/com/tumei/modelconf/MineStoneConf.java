package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Mine")
public class MineStoneConf {
	@Id
	public String objectId;

	public int key;

	/**
	 * 持续时间
	 */
	public int time;

	/**
	 * 对应延长的时间和价格
	 */
	public int[][] add;

	/**
	 * 加速一个小时的价格
	 */
	public int cost;

	public int cd1;
	public int[] good1;

	public int cd2;
	public int[] good2;

	public int cd3;
	public int[] good3;
}

