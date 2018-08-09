package com.tumei.model.beans;

import com.tumei.common.DaoGame;
import com.tumei.model.RoleBean;

/**
 * Created by Administrator on 2017/3/8 0008.
 *
 * 好友实体
 */
public class FriendBean {
	public long id;
	// 男女
	public int icon;
	// 等级
	public int level;
	// 昵称
	public String name = "";

	public int vip;

	// 战斗力
	public long power;

	// 正式好友: < 0 表示在线，其他是上次登录时间
	// 申请者: 表示申请时间
	public long logTime;

	// 今日是否赠送对方，0未赠送，1已经赠送
	public int send;

	// 对方今日是否赠送,0表示未，1表示赠送未领取，2今日已经领取
	public int recv;

	/**
	 * rmb = 0 预备好友， = 1 正式好友,  = 2 在推荐好友中，表示今日已经给这个人申请过好友了
	 */
	public int mode;

	public FriendBean() {}

	public FriendBean(long id, long power, RoleBean rb) {
		this.id = id;
		this.power = power;
		this.name = rb.getNickname();
		this.icon = rb.getIcon();
		this.level = rb.getLevel();
		this.vip = rb.getVip();

		if (rb.getOnline() == 0) {
			this.logTime = rb.getLogtimeLong();
		} else {
			this.logTime = -1;
		}
	}

	public FriendBean(long _id, long _power) {
		this.id = _id;

		RoleBean rb = DaoGame.getInstance().findRole(id);
		this.name = rb.getNickname();
		this.icon = rb.getIcon();
		this.level = rb.getLevel();
		this.vip = rb.getVip();
		this.power = _power;

		if (rb.getOnline() == 0) {
			this.logTime = rb.getLogtimeLong();
		} else {
			this.logTime = -1;
		}
	}

	public FriendBean(FriendBean fb) {
		this.id = fb.id;
		this.icon = fb.icon;
		this.name = fb.name;
		this.power = fb.power;
		this.vip = fb.vip;
		this.logTime = fb.logTime;
		this.level = fb.level;
	}
}
