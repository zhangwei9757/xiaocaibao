package com.tumei.model.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by Administrator on 2017/4/1 0001.
 */
public class TaskItemBean {
	/**
	 * 任务id
	 */
	public int tid;
	/**
	 * 当前进度
	 */
	public int progress;
	/**
	 * 当前需要的最大值
	 */
	public int limit;

	/**
	 * 0: 未完成
	 * 1: 已完成
	 * 2: 已领取
	 */
	public int status;
}
