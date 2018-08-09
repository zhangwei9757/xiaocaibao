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
	public String _id;

	public int key;
	public long green;
	public long blue;
	public long purple;
	public long orange;
	public long red;
}
