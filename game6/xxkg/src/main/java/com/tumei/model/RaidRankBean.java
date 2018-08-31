package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by Administrator on 2017/4/11 0011.
 */
@Document(collection = "Role.RaidRanks")
public class RaidRankBean {
	@JsonIgnore
	@Id
	private String ObjectId;
	@Field("id")
	private Long id;
	/**
	 * 排名
	 */
	private int rank;
	/**
	 * 星数
	 */
	private int star;
	/**
	 * 名字
	 */
	private String name = "";

	public RaidRankBean() { }
	public RaidRankBean(long _id, int _rank, int _star) {
		id = _id;
		rank = _rank;
		star = _star;
	}

	public RaidRankBean(RaidRankBean other) {
		id = other.id;
		rank = other.rank;
		name = other.name;
		star = other.star;
	}

	public String getObjectId() {
		return ObjectId;
	}

	public void setObjectId(String objectId) {
		ObjectId = objectId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getRank() {
		return rank;
	}

	public int fixRank(int i) {
		rank = rank + i;
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}
}
