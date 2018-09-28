package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Heroskills")
public class HeroSkillConf {
	@Id
	public String objectId;

	/**
	 * 英雄ID
	 */
	public int key;

	/**
	 * 技能名字
	 */
	public String attack1;
	public String attack2;
	public String attack3;
	public String attack4;

	/**
	 * 普攻效果
	 */
	public int[][] attack1eff;

	/**
	 * 怒气技能效果
	 */
	public int[][] attack2eff;

	/**
	 * 组合技效果
	 */
	public int[][] attack3eff;

	/**
	 * 组合技条件
	 */
	public int[] cost;

	/**
	 * 超级组合技效果
	 */
	public int[][] attack4eff;

	/**
	 * 天命升级效果
	 */
	public int[] skillup;

	/**
	 * 最高突破等级
	 */
	public int topbreak;

	/**
	 * 突破效果
	 * +1 到 +N  没有突破是不要计算的
	 */
	public int[][] breakeff;

	/**
	 * 觉醒满级后的突破效果
	 */
	public int[][] breakeff2;

	/**
	 * 缘分 1 条件
	 */
	public int[] gk1cost;
	/**
	 * 缘分 1 效果 被动
	 */
	public int[] gk1eff;

	/**
	 * 缘分 1 条件
	 */
	public int[] gk2cost;
	/**
	 * 缘分 1 效果 被动
	 */
	public int[] gk2eff;

	/**
	 * 缘分 1 条件
	 */
	public int[] gk3cost;
	/**
	 * 缘分 1 效果 被动
	 */
	public int[] gk3eff;

	/**
	 * 缘分 1 条件
	 */
	public int[] gk4cost;
	/**
	 * 缘分 1 效果 被动
	 */
	public int[] gk4eff;

	/**
	 * 缘分 1 条件
	 */
	public int gk5cost;
	/**
	 * 缘分 1 效果 被动
	 */
	public int[] gk5eff;

	/**
	 * 缘分 1 条件
	 */
	public int gk6cost;
	/**
	 * 缘分 1 效果 被动
	 */
	public int[] gk6eff;

	/**
	 * 缘分 1 条件
	 */
	public int gk7cost;
	/**
	 * 缘分 1 效果 被动
	 */
	public int[] gk7eff;

	/**
	 * 缘分 1 条件
	 */
	public int gk8cost;
	/**
	 * 缘分 1 效果 被动
	 */
	public int[] gk8eff;
	/**
	 * 武装技能效果
	 */
	public int[] skillzeff;
}
