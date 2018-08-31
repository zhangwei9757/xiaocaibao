package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Equipment")
public class EquipConf {
	@Id
	public String ObjectId;

	public int key;

	/**
	 * 基础属性
	 */
	public int[] base;

	/**
	 * 强化属性
	 */
	public int[] str;

	/**
	 * 精炼属性
	 */
	public int[] refine;

	/**
	 * 精炼奖励属性
	 */
	public int[][] bonus;

	/**
	 * 对应的套装
	 */
	public int[] suit;

	/**
	 * 套装属性加成
	 *
	 * 参考突破效果表
	 */
	public int[] suitadd;

	/**
	 * 觉醒新增属性
	 */
	public int[][] wakenadd;
}
