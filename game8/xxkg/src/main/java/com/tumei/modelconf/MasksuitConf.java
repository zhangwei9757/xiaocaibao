package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Masksuit")
public class MasksuitConf {
	@Id
	public String ObjectId;

	public int key;
	public String name;
	public int[] cost;
	public int[] basic;
	public int[] stratt;
}
