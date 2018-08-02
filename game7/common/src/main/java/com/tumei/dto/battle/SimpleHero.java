package com.tumei.dto.battle;

/**
 * Created by Administrator on 2017/3/2 0002.
 */
public class SimpleHero {
	public int id;
	public long maxlife;
	public int anger;
	/**
	 * 新增时装字段，领主可以进行变身
	 */
	public int skin;
	/**
	 * 英雄等级
	 */
	public int grade;

	/**
	 * 0:没有武装 > 0 武装
	 */
	public int wuz;

	public SimpleHero(int _id, long _life, int _anger, int _skin, int _grade, int _wuz) {
		id = _id;
		maxlife = _life;
		anger = _anger;
		skin = _skin;
		grade = _grade;
		wuz = _wuz;
	}
}
