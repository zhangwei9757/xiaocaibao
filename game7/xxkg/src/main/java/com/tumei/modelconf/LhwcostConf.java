package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
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
