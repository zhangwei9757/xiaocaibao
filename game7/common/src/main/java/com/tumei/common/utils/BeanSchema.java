package com.tumei.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/22 0022.
 */
public class BeanSchema {
	/**
	 * 协议名字
	 */
	private String name;
	private List<BeanItemSchema> items = new ArrayList<>();

	public boolean isProtocol;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<BeanItemSchema> getItems() {
		return items;
	}

	public void setItems(List<BeanItemSchema> items) {
		this.items = items;
	}

	public boolean isProtocol() {
		return isProtocol;
	}

	public void setProtocol(boolean protocol) {
		isProtocol = protocol;
	}
}
