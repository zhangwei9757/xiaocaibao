package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/26
 */
@Document(collection = "Leheup")
public class LeheupConf {
	@Id
	public String objectId;

	public int key;

	// 金币
	public int cost1;
	// 圣物碎片
	public int cost2;
	// 圣灵石
	public int cost3;
	// 钻石消耗
	public int cost4;
	// 升级几率
	public int rate;
}
