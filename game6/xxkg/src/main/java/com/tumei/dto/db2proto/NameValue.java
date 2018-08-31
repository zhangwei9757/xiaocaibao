package com.tumei.dto.db2proto;

import lombok.Data;

/**
 * Created by Leon on 2017/12/19.
 */
@Data
public class NameValue {
	private String name;
	private long value;

	private int rank;

	public NameValue() {}

	public NameValue(String name, long value) {
		this.name = name;
		this.value = value;
	}
	public NameValue(String name, long value, int rank) {
		this.name = name;
		this.value = value;
		this.rank = rank;
	}
}
