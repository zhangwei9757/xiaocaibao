package com.tumei.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 服务器开服信息表
 *
 */
@Document(collection = "Server.Info")
public class ServerInfoBean {
	@Id
	public String ObjectId;

	public int key;
	// 服务器开服时间
	public Date open;

	// 单冲活动更新日期，避免一天多次更新，或者没有更新， 一般12点更新，但是再之后会做一个检查，启动的时候也会检查
	public int singleChargeUpdateDay;
	public int singleCur;
	public int singleBegin;
	public int singleEnd = -1;

	// 累计充值更新日期
	public int cumChargeUpdateDay;
	public int cumCur;
	public int cumBegin;
	public int cumEnd = -1;

	// 打折活动 半价活动 都是5天 所以放在一起
	public int dcUpdateDay;
	public int dcCur; // 一个管2个配置 都是5天

	public int dcBegin;
	public int dcEnd = -1;
	// 半价兑换
	public int ecBegin;
	public int ecEnd = -1;

	// 有多少人购买了基金
	public int fund;

	public boolean day3 = false;
	public boolean day5 = false;
	public boolean day7 = false;

	// 是否上次limitday的id没有被清除，需要发送奖励进行结算
	public int limitday = 0;
	// 是否上次invadingday的id没有被清除，需要发送奖励进行结算
	public int invadingday = 0;
}
