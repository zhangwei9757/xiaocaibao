package com.tumei.controller.struct;

/**
 * Created by Leon on 2017/5/31 0031.
 */
public class GroupRankStruct {
	public long id; // 公会名字
	public int value; // 等级排行榜时: 表示总经验，副本排行榜时：表示副本关卡

	public GroupRankStruct() {}
	public GroupRankStruct(long _id, int _val) {
		id = _id;
		value = _val;
	}
}
