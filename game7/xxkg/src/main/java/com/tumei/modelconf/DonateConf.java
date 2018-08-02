package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Donate")
public class DonateConf {
	@Id
	public String ObjectId;

	// 等级
	public int key;
	public int[] cost;
	public int[] reward;

}
