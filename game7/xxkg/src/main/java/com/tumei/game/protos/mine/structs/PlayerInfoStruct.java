package com.tumei.game.protos.mine.structs;

/**
 * Created by Leon on 2017/8/1 0001.
 */
public class PlayerInfoStruct {
	public int skin;
	public String name = "";
	public String guild = "";
	public int level;
	public long power;
	// 觉醒等级
	public int grade;

	public PlayerInfoStruct() {}

	public PlayerInfoStruct(int _skin, String _name, String _guild, int _level, long _power, int _grade) {
		this.skin = _skin;
		this.name = _name;
		this.guild = _guild;
		this.level = _level;
		this.power = _power;
		this.grade = _grade;
	}
}
