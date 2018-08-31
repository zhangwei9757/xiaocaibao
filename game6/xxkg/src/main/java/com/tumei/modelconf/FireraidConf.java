package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Fireraid")
public class FireraidConf {
	@Id
	public String ObjectId;

	public int key;
	public int chapter;
	public int stage;
	public int condition;
	public int[][] buff;
	public int[][] krewards;
	public int[][] crewards;
	public int chief;
	/**
	 * 以下为六个站位的守卫:
	 *
	 * details:生命 攻击 物防 法防 暴击 命中 抗暴 闪避 的比例
	 *
	 */
	public int[] guard;
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
