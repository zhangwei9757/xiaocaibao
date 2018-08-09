package com.tumei.common.utils;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
public class Defs {
	public static final int 白色 = 0;
	public static final int 绿色 = 1;
	public static final int 蓝色 = 2;
	public static final int 紫色 = 3;
	public static final int 橙色 = 4;
	public static final int 红色 = 5;

	public static final int 圣物之魂 = 3;
	public static final int 荣誉勋章 = 4;
	public static final int 勇士币 = 5;
	public static final int 夺宝魂币 = 6;
	public static final int 夺宝图 = 7;
	public static final int 夺宝令 = 8;
	public static final int 夺宝积分 = 9;

	public static final int 钻石 = 10;
	public static final int 金币 = 11;
	public static final int 公会贡献 = 12;
	public static final int 灵魂碎片 = 13;
	public static final int 经验 = 14;
	public static final int 活力 = 15;
	public static final int 刷新令 = 16;
	public static final int 荣誉 = 17;
	public static final int 声望 = 18;
	public static final int 战争印记 = 19;
	public static final int 时装精华 = 20;
	public static final int 高级时装精华 = 21;
	public static final int 公会卡 = 29;

	public static final int 初级精炼石 = 30;
	public static final int 中级精炼石 = 31;
	public static final int 高级精炼石 = 32;
	public static final int 极品精炼石 = 33;

	public static final int 宝物精炼石 = 71;

	public static final int 粗糙的宝物 = 22;
	public static final int 发光的宝物 = 23;

	public static final int 觉醒丹 = 40;
	public static final int 突破石 = 50;
	public static final int 封印石 = 70;

	public static final int 召唤令 = 90;
	public static final int 传奇令 = 91;

	public static final int 白银免战牌 = 92;
	public static final int 黄金免战牌 = 93;

	public static final int 九折夺宝令 = 72;
	public static final int 八折夺宝令 = 73;

	public static final int 神器精华 = 100;
	public static final int 铸造石	= 101;
	public static final int 玛瑙 = 102;
	public static final int 符文水晶 = 103;
	public static final int 先知之眼 = 104;
	public static final int 突破玉 = 150;
	public static final int 圣灵石 = 160;

//	public static final int 单冲活动周期 = 5;
//	public static final int 累冲活动周期 = 7;
//	public static final int 其他活动周期 = 5;

	public static final int 公会等级 = 30;
	public static final int 矿区等级 = 15;
	public static final int 日常副本等级 = 35;
	public static final int 占星台等级 = 40;
	public static final int Boss战等级 = 45;
	public static final int 神秘宝藏等级 = 62;
	public static final int 跨服竞技等级 = 68;
	public static final int 符文副本等级 = 70;

	public static final String 绿色字段 = "<color=#20ff2d>%s</color>";
	public static final String 蓝色字段 = "<color=#1ad2ff>%s</color>";
	public static final String 紫色字段 = "<color=#e274ff>%s</color>";
	public static final String 橙色字段 = "<color=#ffeb3e>%s</color>";
	public static final String 红色字段 = "<color=#ff3f4a>%s</color>";

	public static String getColorString(int grade, String name) {
		String fm = "%s";
		switch (grade) {
			case 1:
				fm = 绿色字段;
				break;
			case 2:
				fm = 蓝色字段;
				break;
			case 3:
				fm = 紫色字段;
				break;
			case 4:
				fm = 橙色字段;
				break;
			case 5:
				fm = 红色字段;
				break;
		}
		return String.format(fm, name);
	}

	/**
	 * 是否英雄id范围
	 * @param id
	 * @return
	 */
	public static boolean isHeroID(int id) {
		return (id >= 50000 && id < 90000) && ((id % 10) == 0);
	}

	public static boolean isLordID(int id) {
		return (id >= 90000 && id < 95000);
	}

	public static boolean isEquipID(int id) {
		return (id >= 10000 && id < 20000) && ((id % 10) == 0);
	}

	public static boolean isRuneID(int id) {
		return (id >= 21000 && id < 30000);
	}

	public static boolean isTreasureID(int id) {
		return (id >= 20000 && id < 21000) && ((id % 10) == 0);
	}

	/**
	 * 宝物碎片
	 * @param id
	 * @return
	 */
	public static boolean isTreasureFragID(int id) {
		return (id >= 20011 && id <= 20524 && (id % 10) != 0);
	}

	public static boolean isClothID(int id) {
		return (id >= 30000 && id < 40000);
	}

	public static boolean isRelic(int id) {
		return (id >= 40010 && id < 45000 && id % 10 == 0);
	}
}
