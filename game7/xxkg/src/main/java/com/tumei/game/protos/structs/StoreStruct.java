package com.tumei.game.protos.structs;

/**
 * Created by Administrator on 2017/3/6 0006.
 */
public class StoreStruct {
	/**
	 * 标识符
	 */
	public int key;
	/**
	 * 物品id
	 */
	public int id;
	public int count;
	/**
	 * 还可购买次数
	 */
	public int limit;
	/**
	 * 价格 [id,count,id2,count2...]
	 */
	public int[] price;
	/**
	 * 0: 可以买
	 * 1: 不可以买
	 */
	public int disable;

	/**
	 * 已经购买的个数
	 */
	public int used;

	public StoreStruct() {}
	public StoreStruct(int _key) {
		key = _key;
	}
}
