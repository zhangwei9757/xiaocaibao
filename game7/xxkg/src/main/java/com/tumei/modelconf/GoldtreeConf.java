package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 英雄升级所需经验
 *
 */
@Document(collection = "Goldtree")
public class GoldtreeConf {
	@Id
	public String ObjectId;

	public int key;
	public int gold1;
	public int gold2;
	public int gold3;
	public int gold4;
	public int gold5;
	public int gold6;
	public int gold7;
	public int gold8;
	public int gold9;
	public int gold10;
	public int[] cost;
	public int[] reward;
}
