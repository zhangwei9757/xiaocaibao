package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 英雄升级所需经验
 *
 */
@Document(collection = "Heroup")
public class HeroupConf {
	@Id
	public String ObjectId;

	public int key;
	public int green;
	public int blue;
	public int purple;
	public int orange;
	public int red;
}
