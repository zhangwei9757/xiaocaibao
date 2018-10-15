package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Dailytreasure")
public class DailytreasureConf {
	@Id
	public String ObjectId;

	public int key;

	public int score;

	public int cost;

	public int rate1;

	public int rate2;

	public int[] num;
	public int[] reward;

	// 手动刷新宝箱的价格
	public int[] recost;
}
