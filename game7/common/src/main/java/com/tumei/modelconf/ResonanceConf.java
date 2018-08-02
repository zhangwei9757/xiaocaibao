package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Resonance")
public class ResonanceConf {
	@Id
	public String objectId;

	public int key;

	/**
	 * 装备共鸣
	 */
	public int[][] equstr;

	/**
	 * 精炼共鸣
	 */
	public int[][] equref;

	/**
	 * 宝物强化共鸣
	 */
	public int[][] trestr;

	/**
	 * 宝物精炼共鸣
	 */
	public int[][] treref;
}
