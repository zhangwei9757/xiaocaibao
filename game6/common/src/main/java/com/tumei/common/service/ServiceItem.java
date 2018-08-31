package com.tumei.common.service;

import java.net.URI;

/**
 * Created by Leon on 2017/8/16 0016.
 */
public class ServiceItem {
	// 服务名
	public String name = "";

	// 相当于服务分组
	public String service = "";

	// 上次续期时间
	public long ts;

	public String uri;

	public ServiceItem() {}
	public ServiceItem(String name, String service, String uri, int port) {
		this.name = name;
		this.service = service;
		if (!uri.startsWith("http")) {
			this.uri = String.format("http://%s:%d", uri, port);
		} else {
			this.uri = String.format("%s:%d", uri, port);
		}
		this.ts = System.currentTimeMillis() / 1000 + 10;
	}

	public void refresh(String uri, int port) {
		if (!uri.startsWith("http")) {
			this.uri = String.format("http://%s:%d", uri, port);
		} else {
			this.uri = String.format("%s:%d", uri, port);
		}
		this.ts = System.currentTimeMillis() / 1000 + 10;
	}
}
