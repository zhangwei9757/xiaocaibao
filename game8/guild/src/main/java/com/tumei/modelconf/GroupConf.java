package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Guild")
public class GroupConf {
	@Id
	public String ObjectId;

	// 等级
	public int key;
	// 经验
	public int exp;
	// 成员上限
	public int num;

}
