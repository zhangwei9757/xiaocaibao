package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Awaken")
public class AwakenConf {
	@Id
	public String ObjectId;

	public int key;

	/**
	 * [突破等级，英雄等级] 需求
	 */
	public int[] require;

	public int[][] fwcost1;
	public int[][] fwcost2;
	/**
	 * 升级消耗的觉醒丹
	 */
	public int gradecost;
	/**
	 * 等级提升效果
	 */
	public int[] levelup;

	@Override
	public String toString() {
		return "AwakenConf{" + "ObjectId='" + ObjectId + '\'' + ", key=" + key + ", fwcost1=" + Arrays.toString(fwcost1) + ", fwcost2=" + Arrays.toString(fwcost2) + ", levelup=" + Arrays.toString(levelup) + '}';
	}
}
