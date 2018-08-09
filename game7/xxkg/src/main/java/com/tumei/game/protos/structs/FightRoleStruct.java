package com.tumei.game.protos.structs;

/**
 * Created by Leon on 2017/7/19 0019.
 */
public class FightRoleStruct {
	// 真实英雄id;
	public int hero;
	/**
	 * 1. 对于领主来说 是他的时装对应的英雄id
	 * 2. 对于其他来说 只要skin不等于0，表示他是进阶的，客户端需要检查是否需要替换骨骼
	 *
	 */
	public int skin;

	public FightRoleStruct() {}
	public FightRoleStruct(int _hero, int _skin) {
		hero = _hero;
		skin = _skin;
	}
}
