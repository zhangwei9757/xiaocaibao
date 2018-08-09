package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 * 神器部件升星
 */
@Document(collection = "Artpartstup")
public class ArtpartstupConf {
	@Id
	public String id;

	public int key;

	public int attup;
	public int attupall;
	public int cost1;
	public int rate1;
	public int cost2;
	public int rate2;
	public int cost3;
	public int rate3;
	public int cost4;
	public int rate4;
	public int cost5;
	public int rate5;
}

