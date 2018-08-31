package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Fuwenraid")
public class FuwenraidConf {
	@Id
	public String ObjectId;

	public int key;

	public int[] rewardrate;
	public int[] spread;
	public int item;
	public int best;
	public int[] bestdetail;
	public int[][] salegood;
	public int[] mosreward;
	public int score;
	public int[] clear;
	public int[][] guard;
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
	public int[][] rankreward;
}
