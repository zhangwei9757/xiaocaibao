package com.tumei.common.utils;

import java.lang.management.ManagementFactory;

/**
 * Created by Leon on 2017/8/23 0023.
 */
public class SystemUtil {

	/**
	 * 获取进程id
	 * @return
	 */
	public static long getPid() {
		try {
			String name = ManagementFactory.getRuntimeMXBean().getName();
			String pid = name.split("@")[0];
			return Long.parseLong(pid);
		} catch (Exception ex) {
			return -1;
		}
	}

	public static float byteToMb(long m) {
		return ((float)m / 1024f / 1024f);
	}


}
