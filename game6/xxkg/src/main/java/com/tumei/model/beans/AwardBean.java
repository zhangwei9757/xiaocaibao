package com.tumei.model.beans;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
public class AwardBean {
	public int id;
	public long count;
	/**
	 * 特殊自增管理id
	 */
	public int hid;

	public AwardBean() { }

	public AwardBean(int _id, long _count, int _hid) {
		id = _id;
		count = _count;
		hid = _hid;
	}

	public AwardBean(int id, long count) {
		this.id = id;
		this.count = count;
	}
}
