package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Battlestore")
public class BattlestoreConf {
	@Id
	public String ObjectId;

	public int key;
	public int[] item;
	public int[] price;
	/**
	 * 限购次数
	 */
	public int limit;
	/**
	 * 每日是否重置
	 */
	public int clear;
}

