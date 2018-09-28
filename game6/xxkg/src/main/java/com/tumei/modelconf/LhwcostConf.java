package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/26
 */
@Document(collection = "Lhwcost")
public class LhwcostConf {
	@Id
	public String objectId;

	public int key;

	// 圣物碎片数量
	public int cost1;
	// 血晶石数量
	public int cost2;
	// 对应的卷轴数量
	public int cost3;
}
