package com.tumei.model.equips;

/**
 * Created by Leon on 2017/12/8.
 */
public class EquipMat {
	// 装备或者碎片 eid
	public int eid;
	// 如果count == 0 表示eid是一个装备的唯一编号，非id
	// 如果count >= 1 表示eid是一个碎片的id, 没有唯一编号
	public int count;
}