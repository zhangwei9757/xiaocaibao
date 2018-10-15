package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Chest")
public class ChestConf {
	@Id
	public String ObjectId;

	public int key;
	/**
	 * 0: 自动选择奖励的宝箱
	 * 1: 手动选择奖励的宝箱
	 */
	public int mode;
	/**
	 * 分组机率
	 */
	public int[] rate;

	public int[][] box1;
	public int[][] box2;
	public int[][] box3;
}
