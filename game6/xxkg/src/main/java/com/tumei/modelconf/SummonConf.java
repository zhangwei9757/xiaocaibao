package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Summon")
public class SummonConf {
	@Id
	public String ObjectId;

	public int quality;
	public int summon1;
	public int summon2;
	public int summon3;
	public int[][] sh3;
}
