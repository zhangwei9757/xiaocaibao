package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 * 神器进阶
 */
@Document(collection = "Artadvanced")
public class ArtadvancedConf {
	@Id
	public String id;

	public int key;

	public int cost1a;
	public int cost1b;
	public int cost1c;
	public int cost2a;
	public int cost2b;
	public int cost2c;
	public int cost3a;
	public int cost3b;
	public int cost3c;
	public int cost4a;
	public int cost4b;
	public int cost4c;
	public int cost5a;
	public int cost5b;
	public int cost5c;
}

