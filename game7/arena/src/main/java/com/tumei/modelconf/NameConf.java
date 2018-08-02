package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Name")
public class NameConf {
	@Id
	public String ObjectId;

	public int key;
	public String part1;
	public String part2;
	public String part3;
}

