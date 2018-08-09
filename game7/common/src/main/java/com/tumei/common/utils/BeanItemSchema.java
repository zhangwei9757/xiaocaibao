package com.tumei.common.utils;

/**
 * Created by Administrator on 2017/3/22 0022.
 */
public class BeanItemSchema {
	/**
	 * 协议名字
	 */
	private String name;
	private String type;
	private String key;
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
