package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Dailyraid")
public class DailyraidConf {
	@Id
	public String ObjectId;

	public int key;
	public int mode;
	public int hero;
	public int[] goods;
	public int[] defend;
	public int[] details;
	public long hp;
	public int attack;
	public int defence1;
	public int defence2;
	public int crit;
	public int hit;
	public int critoff;
	public int dog;
	public int increase;
	public int reduce;
}
