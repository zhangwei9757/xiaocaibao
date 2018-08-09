package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 七日登录表
 */
@Document(collection = "Sevens")
public class SevenConf {
	@Id
	public String ObjectId;

	public int key;
	public int[][] reward;
	public int type;
	public int mode;
}
