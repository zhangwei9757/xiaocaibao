package com.tumei.game.protos.mine.structs;

/**
 * Created by Leon on 2017/7/31 0031.
 */
public class PlayerBehaveStruct {
	public long id;

	public int pos;

	/**
	 * 玩家动作通知:
	 * key:是玩家的ID
	 * value是行为，定义:
	 * 			-1 		离开
	 * 			0 		进入
	 * 			1-6 	六个方向的移动
	 */
	public int behave;

	public PlayerInfoStruct info;

	public PlayerBehaveStruct() {}

	public PlayerBehaveStruct(long _id, int _behave, int _pos) {
		this.id = _id;
		this.behave = _behave;
		this.pos = _pos;
	}

	/**
	 * 第一次进入视野调用这个，构造详细信息
	 * @param _id
	 * @param _pos
	 */
	public PlayerBehaveStruct(long _id, int _pos, int _skin, String _name, String _guild, int _level, long _power, int _grade) {
		this.id = _id;
		this.behave = 0;
		this.pos = _pos;
		this.info = new PlayerInfoStruct(_skin, _name, _guild, _level, _power, _grade);
	}
}
