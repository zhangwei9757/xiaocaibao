package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by Administrator on 2017/4/11 0011.
 */
@Document(collection = "Role.TreasureRankService")
public class TreasureRankBean {
	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private Long id;
	/**
	 * 排名
	 */
	private int rank;
	/**
	 * 积分
	 */
	private int score;
	/**
	 * 名字
	 */
	private String name = "";

	public TreasureRankBean() { }

	public TreasureRankBean(long _id, int _rank, int _score) {
		id = _id;
		rank = _rank;
		score = _score;
	}

	public TreasureRankBean(TreasureRankBean other) {
		id = other.id;
		rank = other.rank;
		name = other.name;
		score = other.score;
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

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
}
