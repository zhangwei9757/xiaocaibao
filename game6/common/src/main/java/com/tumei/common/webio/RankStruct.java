package com.tumei.common.webio;

/**
 * Created by Leon on 2017/5/31 0031.
 */
public class RankStruct {
	public long gid;
	public int icon;
	public String name; // 公会名字
	public int value; // 等级排行榜时: 表示总经验，副本排行榜时：表示副本关卡
	public String leader;
	public int count;
	public int limit;
	public int level;

	public RankStruct() {}
	public RankStruct(long _gid, int _value) {
		gid = _gid;
		value = _value;
	}

	public RankStruct(int _icon, String _name, int _value, String _leader, int _count, int _limit) {
		icon = _icon;
		name = _name;
		value = _value;
		leader = _leader;
		count = _count;
		limit = _limit;
	}
}
