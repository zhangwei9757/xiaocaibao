package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Guildraid")
public class GuildraidConf {
	@Id
	public String ObjectId;

	// 等级
	public int key;
	// 战斗奖励
	public int[] reward1;
	// 击杀奖励
	public int reward2;
	// 公会经验
	public int reward5;
	// 通关奖励
	public int[] reward3;
	// 阵营奖励
	public int[][] reward4;

	public int[] sect1;
	public int[] sect2;
	public int[] sect3;
	public int[] sect4;

	public long hp;
	public int attack;
	public int defence1;
	public int defence2;
	public int crit;
	public int hit;
	public int critoff;
	public int dog;
	public int increase;
	public int reduce;

}
