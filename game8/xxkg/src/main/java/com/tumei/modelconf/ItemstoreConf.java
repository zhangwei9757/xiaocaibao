package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Itemstore")
public class ItemstoreConf {
	@Id
	public String ObjectId;

	public int key;
	public int[] item;
	public int price;
	public int[] cost;
	public int limit;
	public int clear;
}

