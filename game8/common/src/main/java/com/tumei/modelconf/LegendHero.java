package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 圣物
 *
 */
@Document(collection = "Legendhero")
public class LegendHero {
	@Id
	public String objectId;

	public int key;
	// 攻击模式
	public int mode;
	// 基础属性
	public int[] basatt;
	// 升星属性
	public int[] basattup;
	// 战斗技能效果
	public int[] bateff;
	// 觉醒属性
	public int[] wakatt;
	// 觉醒效果
	public int[] wakeff;

	@Override
	public String toString() {
		return "LegendHero{" + "key=" + key + ", mode=" + mode + ", basatt=" + Arrays.toString(basatt) + ", basattup=" + Arrays.toString(basattup) + ", bateff=" + Arrays.toString(bateff) + ", wakatt=" + Arrays.toString(wakatt) + '}';
	}
}
