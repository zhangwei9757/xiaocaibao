package com.tumei.modelconf.festival;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Crazylist")
public class CrazylistConf {
	@Id
	public String id;

	public int key;
	public int flag; // 节日活动标识
	public String name; //节日活动名字
	public String des;

	public int start;
	public int last;
	public int openlimit; // 开服最低要求
	public int num;

}
