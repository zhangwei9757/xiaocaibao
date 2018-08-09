package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Stars")
public class StarConf {
	@Id
	public String ObjectId;

	public int key;

	public int limit;
	public int[][] hour6;
	public int[][] hour8;
	public int[][] hour10;
	public int[][] cost;
	public int speedup;
}
