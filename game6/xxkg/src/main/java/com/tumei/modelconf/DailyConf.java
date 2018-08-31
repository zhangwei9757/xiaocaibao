package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Daily")
public class DailyConf {
	@Id
	public String ObjectId;

	public int key;
	/**
	 * 次数, 要求完成的limit
	 */
	public int time;
	/**
	 * 完成后奖励
	 */
	public int[] reward;
	/**
	 * 完成后得分
	 */
	public int score;

	/**
	 * 积分进度奖励
	 */
	public int[] reward30;
	public int[] reward60;
	public int[] reward90;
	public int[] reward120;
}
