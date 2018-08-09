package com.tumei.modelconf.festival;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Fesconsumption")
public class FestivalSpendConf {
	@Id
	public String id;

	public int key;

	public int spend;
	public int[] reward;
}
