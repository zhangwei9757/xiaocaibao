package com.tumei.controller.user;

/**
 * Created by Leon on 2017/8/28 0028.
 */
public class BasicUserInfo {
	public long id;
	public String name;
	public int vip;
	public int vipExp;
	public int level;
	public long exp;

	// 是否在线
	public boolean online;

	public int gem;
	public long coin;

	// 总充值记录
	public int charge;

	public long fr; // 登录禁止截止日期
	public long fs; // 禁言日期
}
