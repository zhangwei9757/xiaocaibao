package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Maskcost")
public class MaskcostConf {
	@Id
	public String ObjectId;

	public int key;
	public int cost1;
	public int gold1;
	public int cost2;
	public int gold2;
	public int cost3;
	public int gold3;
}
