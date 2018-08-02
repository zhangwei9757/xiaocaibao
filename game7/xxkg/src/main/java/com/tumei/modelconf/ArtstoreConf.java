package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 * 神器进阶
 */
@Document(collection = "Artstore")
public class ArtstoreConf {
	@Id
	public String id;

	public int key;

	public int part; //位置
	public int[] good; // 商品
	public int[] price; // 价格
	public int hev; // 权重
	public int[] limit; // 条件, 一般2个:第一个是战队等级,第二个是vip等级,满足两者其一即可

}

