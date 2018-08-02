package com.tumei.controller.cmd;

/**
 * Created by Leon on 2017/8/23 0023.
 */
public class CmdServerInfo {
	// 启动时间
	public long bootTime;
	// 运行时间
	public long upTime;

	// 线程总数
	public int threadCount;
	// 最大线程数
	public int peekThreadCount;

	// 处理器个数
	public int proccessor;

	public int cpu;

	// 内存(MB)
	public float totalMemory;

	public float maxMemory;

	public float freeMemory;

	// 当前玩家总数
	public int users;

	// 日活跃玩家
	public int dau;

	// 日新增活跃玩家
	public int danu;

	// 日充值
	public long charge;

	// 新增玩家充值
	public long newCharge;

	@Override
	public String toString() {
		return String.format(
		"+++当前玩家:" + users + "\n" +
		"\t今日活跃玩家:" + dau + "\n" +
		"\t今日新增玩家:" + danu + "\n" +
		"\t今日充值:" + charge + "\n" +
		"\t今日新增玩家充值:" + charge + "\n"
		);
	}
}
