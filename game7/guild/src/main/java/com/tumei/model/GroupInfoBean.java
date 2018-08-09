package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.controller.struct.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

/**
 * Created by leon on 2016/11/5.
 *
 * 全局的公会信息:
 * 1. 公会副本排行 前50
 * 2. 公会等级排行 前50
 *
 */
@Document(collection = "GroupInfo")
public class GroupInfoBean {
	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private Long id;

	// 公会等级排行榜
	public List<GroupRankStruct> levelRanks = new ArrayList<>();
	// 公会副本排行榜
	public List<GroupRankStruct> sceneRanks = new ArrayList<>();

	public GroupInfoBean() {
		id = 1L;
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

	public List<GroupRankStruct> getLevelRanks() {
		return levelRanks;
	}

	public void setLevelRanks(List<GroupRankStruct> levelRanks) {
		this.levelRanks = levelRanks;
	}

	public List<GroupRankStruct> getSceneRanks() {
		return sceneRanks;
	}

	public void setSceneRanks(List<GroupRankStruct> sceneRanks) {
		this.sceneRanks = sceneRanks;
	}
}
