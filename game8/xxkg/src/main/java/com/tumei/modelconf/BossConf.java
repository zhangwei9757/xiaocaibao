package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Boss")
public class BossConf {
	@Id
	public String objectId;

	// 等级
	public int key;
	// 攻击奖励
	public int reward;
	// 击杀奖励
	public int[] reward3;

	// 鼓舞
	public int[] upatt;
	// 价格
	public int upcost;

	// 实际战斗角色
	public int[] guard;
	// 角色的属性
	public int[] details;
	// hp
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
