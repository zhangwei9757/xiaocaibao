package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Trerecost")
public class TreasurerefcostConf {
	@Id
	public String ObjectId;

	public int key;
	public int cost1;
	public int cost2;
	public int cost3;
}
