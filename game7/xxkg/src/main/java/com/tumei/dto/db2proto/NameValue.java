package com.tumei.dto.db2proto;

import com.tumei.common.webio.AwardStruct;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/12/19.
 */
@Data
public class NameValue {
	private long uid;
	private String name;
	private long value;

	private int rank;

	private List<AwardStruct> awards = new ArrayList<>();

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
	public NameValue(long uid, String name, long value, int rank) {
		this.uid = uid;
		this.name = name;
		this.value = value;
		this.rank = rank;
	}

	public NameValue(long uid, String name, long value, int rank, List<AwardStruct> awards) {
		this.uid = uid;
		this.name = name;
		this.value = value;
		this.rank = rank;
		this.awards = awards;
	}
}
