package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Guildstore")
public class GroupstoreConf {
	@Id
	public String ObjectId;

	public int key;
	public int[] item;
	public int[] price;
	public int limit;
	// 每日是否会重置
	public int clear;
	public int guildlevel;
	public int tab;
}

