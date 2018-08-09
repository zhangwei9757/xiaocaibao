package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 * 神器进阶
 */
@Document(collection = "Artsummon")
public class ArtsummonConf {
	@Id
	public String id;

	public int key;
	public int team;
	public int[] good;
	public int hev;

}

