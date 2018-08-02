package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Donatereward")
public class DonaterewardConf {
	@Id
	public String ObjectId;

	// 等级
	public int key;
	public int[] guild1;
	public int[] guild2;
	public int[] guild3;
}
