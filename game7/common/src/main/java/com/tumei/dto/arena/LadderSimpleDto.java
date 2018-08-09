package com.tumei.dto.arena;

/**
 * Created by Leon on 2017/11/7 0007.
 *
 * 跨服竞技场 用于显示在界面上的玩家信息
 *
 */
public class LadderSimpleDto {
	public long uid;
	public String name;
	public int icon;
	public int grade;
	// 如果穿戴了时装，这里发送的是时装英雄的id，非时装本身
	public int fashion;
	public long power;
	// 进入该层的时间，客户端计算是否开始衰弱，以及衰弱多少，是否在保护期
	public long time;
}
