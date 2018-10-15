package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 英雄升级所需经验
 *
 */
@Document(collection = "Herobreak")
public class HerobreakConf {
	@Id
	public String ObjectId;

	public int key;
	/**
	 * 所需等级
	 */
	public int level;
	/**
	 * 金币
	 */
	public int cost1;
	/**
	 * 突破石
	 */
	public int cost2;
	/**
	 * 所需英雄
	 */
	public int cost3;

	/**
	 * 领主消耗金币
	 */
	public int cost4;

	/**
	 * 领主消耗突破石
	 */
	public int cost5;

	/**
	 * 英雄消耗突破玉
	 */
	public int cost6;

	/**
	 * 领主消耗突破玉
	 */
	public int cost7;
}
