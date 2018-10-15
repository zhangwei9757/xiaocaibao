package com.tumei.modelconf.festival;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Festotal")
public class FestivalCumConf {
	@Id
	public String id;

	public int key;

	public int cost;

	public int[] reward;
}
