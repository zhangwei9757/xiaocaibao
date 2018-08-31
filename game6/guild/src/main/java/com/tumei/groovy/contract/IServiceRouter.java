package com.tumei.groovy.contract;

/**
 * Created by Leon on 2017/11/14 0014.
 */
public interface IServiceRouter {

	// 根据uid判断玩家属于哪个区，初始是通过%1000得到的
	int chooseZone(long uid);

	// 根据uid判断玩家属于那个跨服竞技场
	int chooseArena(long uid);
}
