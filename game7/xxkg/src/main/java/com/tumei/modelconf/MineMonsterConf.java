package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Monsters")
public class MineMonsterConf {
	@Id
	public String objectId;

	public int key;

	public int quality;

	public int at;

	public int[][] drop;

	public int[] num;
}

