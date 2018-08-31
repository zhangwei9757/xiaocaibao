package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "All.ranks")
public class AllRankBean {
	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private Long id;

	// 四组对应的符文副本积分排名
	private int group1;
	private int group2;
	private int group3;
	private int group4;

	private int[] ranks = new int[4];

	public AllRankBean() {}
	public AllRankBean(long _id) { id = _id; }

	public void flush(int[] _ranks) {
		group1 = 0;
		group2 = 0;
		group3 = 0;
		group4 = 0;
		ranks = _ranks;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getGroup1() {
		return group1;
	}

	public void setGroup1(int group1) {
		this.group1 = group1;
	}

	public int getGroup2() {
		return group2;
	}

	public void setGroup2(int group2) {
		this.group2 = group2;
	}

	public int getGroup3() {
		return group3;
	}

	public void setGroup3(int group3) {
		this.group3 = group3;
	}

	public int getGroup4() {
		return group4;
	}

	public void setGroup4(int group4) {
		this.group4 = group4;
	}

	public int submit(int index, int _score) {
		switch (index) {
			case 1:
				group1 += _score;
				return group1;
			case 2:
				group2 += _score;
				return group2;
			case 3:
				group3 += _score;
				return group3;
			case 4:
				group4 += _score;
				return group4;
		}
		return 0;
	}

	public int[] getRanks() {
		return ranks;
	}

	public void setRanks(int[] ranks) {
		this.ranks = ranks;
	}
}
