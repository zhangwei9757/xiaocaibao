package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Lineup")
public class LineupConf {
	@Id
	public String ObjectId;

	public int key;

	public int[] lineatt;
}
