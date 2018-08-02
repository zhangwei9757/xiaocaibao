package com.tumei.common.service;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Leon on 2017/8/16 0016.
 */
public class ServiceItems {
	private Log logger = LogFactory.getLog(ServiceItems.class);

	public String service;

	// 对应服务的所有实例
	public ConcurrentHashMap<String, ServiceItem> items = new ConcurrentHashMap<>();

	public ServiceItems() {}
	public ServiceItems(String service) {
		this.service = service;
	}

	public void refresh(String meta, String uri, int port) {
		ServiceItem si = items.getOrDefault(meta, null);
		if (si != null) {
			si.refresh(uri, port);
		} else {
			si = new ServiceItem(meta, service, uri, port);
			items.put(meta, si);
			logger.warn("+++++ 服务实例[" + meta + "] 上线，隶属于服务名[" + service + "], 对应地址:" + uri);
		}

	}

	/**
	 * 更新删除所有超时的结点
	 */
	public void update() {
		long now = System.currentTimeMillis() / 1000;
		List<String> dels = new ArrayList<>();
		items.forEach((k, v) -> {
			if (v.ts < now) {
				dels.add(k);
			}
		});

		for (String k : dels) {
			logger.warn("--- 服务实例[" + k + "] 注销，隶属于服务名[" + service + "].");
			items.remove(k);
		}
	}
}
