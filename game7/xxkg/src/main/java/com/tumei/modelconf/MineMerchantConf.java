package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Dealer")
public class MineMerchantConf {
	@Id
	public String objectId;

	public int key;

	public int rflevel;

	public int[] good;

	public int price1;
}

