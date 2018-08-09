package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Stateup")
public class StateupConf {
	@Id
	public String objectId;

	public int key;
	/**
	 * 所需境界石
	 */
	public int cost;
	/**
	 * 每次点击消耗
	 */
	public int click;
	/**
	 * 直升机率
	 */
	public int[] chance;
	/**
	 * 基础属性
	 */
	public int[] bonusatt;
}
