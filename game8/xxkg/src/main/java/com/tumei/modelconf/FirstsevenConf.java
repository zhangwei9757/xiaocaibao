package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Firstseven")
public class FirstsevenConf {
	@Id
	public String ObjectId;

	public int key;
	public int[] login;
	public int[] sale;
	public int[][] task1;
	public int[][] rewards1;
	public int[][] task2;
	public int[][] rewards2;
}
