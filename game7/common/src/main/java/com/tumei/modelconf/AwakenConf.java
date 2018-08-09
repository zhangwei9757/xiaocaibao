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
	public String objectId;

	public int key;

	/**
	 * [突破等级，英雄等级] 需求
	 */
	public int[] require;
	/**
	 * 升级消耗的觉醒丹
	 */
	public int gradecost;

	public int[][] fwcost1;
	public int[][] fwcost2;
	public int[] levelup1;
	public int[] levelup2;


	@Override
	public String toString() {
		return "AwakenConf{" + "id='" + objectId + '\'' + ", key=" + key + ", require=" + Arrays.toString(require) + ", fwcost1=" + Arrays.toString(fwcost1) + ", fwcost2=" + Arrays.toString(fwcost2) + ", gradecost=" + gradecost + ", fwcost1=" + Arrays.toString(fwcost1) + ", fwcost2=" + Arrays.toString(fwcost2) + ", levelup1=" + Arrays.toString(levelup1) + ", levelup2=" + Arrays.toString(levelup2) + '}';
	}
}
