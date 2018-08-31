package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Heroes")
public class HeroConf {
	@Id
	public String ObjectId;

	public int key;

	/**
	 * 英雄名字
	 */
	public String name;

	/**
	 * 品质
	 */
	public int quality;

	/**
	 * 所属阵营，0表示无阵营
	 */
	public int sect;

	/**
	 * 英雄类型:
	 * 1 物理攻击
	 * 2 法术攻击
	 * 3 防御型
	 * 4 辅助型
	 */
	public int type;
	/**
	 * 含义顺序: 生命 攻击力 物理防御 法术防御
	 */
	public int[] growfight;

	/**
	 * 属性成长
	 * 含义顺序: 生命 攻击力 物理防御 法术防御
	 *
	 * 第一纬度表示英雄在 +0，+1，+2。。。。+5 。。。 +12
	 */
	public int[][] attup;

	/**
	 * 分解得到灵魂碎片的数量
	 */
	public int[] soul;

	/**
	 * 是否加入图鉴
	 */
	public int pokedex;
}
