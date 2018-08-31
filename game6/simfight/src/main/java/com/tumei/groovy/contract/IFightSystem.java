package com.tumei.groovy.contract;

import com.tumei.common.fight.*;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Leon on 2017/9/1 0001.
 */
public interface IFightSystem {

	long calcPower(PowerStruct ts);

	void buildTeam(int side, FightStruct fs);
	void buildSceneTeam(int side, SceneFightStruct sfs);
	void buildGroupTeam(int side, GroupFightStruct gfs);

	/**
	 * 使用直接输入英雄的属性，构造队伍
	 * <p>
	 * 1. 副本怪物属性
	 * 2.
	 *
	 * @param side
	 * @param _heroStructs
	 */
	void buildTeamByStruct(int side, List<DirectHeroStruct> _heroStructs);

	/**
	 * 运行战斗
	 */
	int run();

	/**
	 * 获取战斗过程
	 *
	 * @return
	 */
	String getFightData();

	/**
	 * 获取对方剩余的血量
	 * <p>
	 * 有的副本需要渐进的击杀，每次攻击后血量是不会恢复的
	 *
	 * @return
	 */
	List<Long> getRightLifes();


	/**
	 * 策划需要看到参与战斗的人员属性
	 * @return
	 */
	String debugLeft();
}
