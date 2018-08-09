package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Riders")
public class DbriderConf {
	@Id
	public String ObjectId;

	public int key;

	public int[][] q1;
	public int[][] q2;
	public int[][] q3;
	public int[][] q4;
	public int rate;
	public int[] num;
}
