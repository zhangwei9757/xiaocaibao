package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/26
 */
@Document(collection = "Holyexp")
public class HolyexpConf {
	@Id
	public String objectId;
	public int key;
	public int cost1;
}
