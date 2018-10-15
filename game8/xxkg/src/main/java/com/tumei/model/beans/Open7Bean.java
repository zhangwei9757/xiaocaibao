package com.tumei.model.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
public class Open7Bean {
	public int day;
	// 登录奖励是否领取
	public int logon;
	// 超值礼包是否购买
	public int sale;
	// 任务1分解各个进度后对应的状态,-1表示满足未领取，-2表示已经领取
	public List<List<Integer>> task1 = new ArrayList<>();
	// 任务2分解各个进度后对应的状态，-1表示满足未领取，-2表示已经领取
	public List<List<Integer>> task2 = new ArrayList<>();
}
