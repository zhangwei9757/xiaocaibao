package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Firestore")
public class RaidstoreConf {
	@Id
	public String ObjectId;

	public int key;
	public int[] item;
	public int[] price;
	public int limit;
	public int clear;
	public int star;
	public int tab;
}

