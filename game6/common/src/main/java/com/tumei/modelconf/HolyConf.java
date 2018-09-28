package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/26
 *
 * 圣物
 *
 */
@Document(collection = "Holy")
public class HolyConf {
	@Id
	public String objectId;

	public int key;
	// 对应的传奇英雄
	public int hero;
	// 基础属性
	public int[] basatt;
	// 强化属性
	public int[] stratt;
	// 升星消耗
	public int[] starup;
	// 技能效果
	public int[][] skilleff;
	// 对应增强的英雄
	public int[] addhero;
	// 炼化消耗的金币
	public int coincost;
	// 对应附魔属性
	public int[][] addatt;
	// 对应附魔属性可以附魔的次数上限
	public int addlimit;
	// 炼化中 普通和钻石的两组概率
	public int[][] aim;
	// 传奇英雄的概率
	public int[] rate;
}
