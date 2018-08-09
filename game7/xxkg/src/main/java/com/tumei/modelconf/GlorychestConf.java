package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Glorychest")
public class GlorychestConf {
	@Id
	public String objectId;

	public int key;

	public int rate1;
	public int rate2;
	public int[] rewards;

}
