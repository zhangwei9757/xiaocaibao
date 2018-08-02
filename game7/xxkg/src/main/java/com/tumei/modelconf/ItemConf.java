package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Items")
public class ItemConf {
	@Id
	public String ObjectId;

	public int key;

	public String good;
	/**
	 * 品质
	 */
	public int quality;
	/**
	 * 是否碎片
	 */
	public int pis;
	/**
	 * 是否可出售
	 */
	public int sell;
	/**
	 * 价格
	 */
	public int[] price;
}
