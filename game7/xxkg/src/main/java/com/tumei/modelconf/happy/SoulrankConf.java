package com.tumei.modelconf.happy;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Soulrank")
public class SoulrankConf {
	@Id
	public String id;

	public int key;
	public int[] reward1;
	public int limit;
	public int[] reward2;
}
