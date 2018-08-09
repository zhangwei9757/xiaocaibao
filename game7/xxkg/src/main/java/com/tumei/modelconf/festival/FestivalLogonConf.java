package com.tumei.modelconf.festival;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Feslogin")
public class FestivalLogonConf {
	@Id
	public String id;

	public int key;

	public int[] rewards;
}
