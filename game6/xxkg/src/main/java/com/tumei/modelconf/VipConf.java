package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Vip")
public class VipConf {
	@Id
	public String ObjectId;

	public int key;
	/**
	 * 充值
	 */
	public int num;
	/**
	 * 累计
	 */
	public int total;
	/**
	 * 装备强化暴击2倍概率
	 */
	public int critrate;
	/**
	 * 3倍暴击概率
	 */
	public int criteff;
	/**
	 * 挂机加速可购买次数
	 */
	public int speedup;
	/**
	 * 下线后累计可挂机奖励时间
	 */
	public int time;
	/**
	 * 英雄商店刷新次数
	 */
	public int herostore;
	/**
	 * 可购买活力药剂次数
	 */
	public int drink;
	/**
	 * 可购买橙色装备宝盒次数
	 */
	public int orangebox;
	/**
	 * 可购买红色装备宝盒次数
	 */
	public int redbox;
	/**
	 * 可购买橙色宝物宝盒次数
	 */
	public int trebox;
	/**
	 * 可购买公会副本挑战次数
	 */
	public int guildraid;
	/**
	 * 可摇钱次数
	 */
	public int tree;
	/**
	 * 可摇钱最高倍率
	 */
	public int[] treebonus;
	/**
	 * 符文副本可重置次数
	 */
	public int fuwenre;
	/**
	 * 符文副本可改名次数
	 */
	public int fuwenag;
	/**
	 * 英雄仓库容量增加
	 */
	public int heroadd;
	/**
	 * 装备仓库容量增加
	 */
	public int equadd;
	/**
	 * 副本扫荡奖励倍率
	 */
	public int sceneadd;
	/**
	 * 远征扫荡
	 */
	public int edsweep;
	/**
	 * 远征重置次数
	 */
	public int edre;
	/**
	 * 竞技场5连挑战
	 */
	public int arenaplus;
	/**
	 * 符文副本额外奖励
	 */
	public int fuwenbonus;
	/**
	 * 每日礼包
	 */
	public int[] dailybag;
	/**
	 * 每周礼包
	 */
	public int[] weekbag;
	/**
	 * vip礼包
	 */
	public int[] vipbag;
	/**
	 * 每日炼化次数
	 */
	public int dailyglory;
	/**
	 * vip礼包价格
	 */
	public int cost;

	/**
	 * 神秘宝藏额外刷新宝箱次数
	 */
	public int dtnum;

	/**
	 * 神秘宝藏双倍领取
	 */
	public int dtdouble;

	/**
	 * 矿区攻打玩家次数
	 */
	public int mattnum;

	/**
	 * 玩家延长矿脉的次数
	 */
	public int mineadd;

	/**
	 * 天梯赛每日攻打次数上限
	 */
	public int trtime;

	/**
	 * 神器召唤次数
	 */
	public int artcall;

	/**
	 * 神器商店刷新次数
	 */
	public int artrefresh;

	/**
	 * 战争学院可以同时进行的数量
	 */
	public int wardev;
}
