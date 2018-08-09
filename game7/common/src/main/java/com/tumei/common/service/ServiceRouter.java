package com.tumei.common.service;

import org.springframework.stereotype.Service;

/**
 * Created by Leon on 2017/11/14 0014.
 */
@Service
public class ServiceRouter {
	/**
	 * 根据uid,获取对应的玩家所在服务器zone
	 * @param uid
	 * @return
	 */
	public int chooseZone(long uid) {
		return (int)(uid % 1000);
	}

	/**
	 * 根据uid获取所在的跨服竞技场的编号
	 * @param uid
	 * @return
	 */
	public int chooseArena(long uid) {
		int zone = chooseZone(uid) - 1;

		return (zone / 20) + 1;
	}
}
