package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.utils.TimeUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 日常资源副本
 */
@Document(collection = "Role.DailyScene")
public class DailySceneBean {
	@JsonIgnore
	@Id
	private String ObjectId;

	@JsonIgnore
	@Field("id")
	private Long id;

	/**
	 * 当前已经挑战过的类型
	 *
	 * key: [1,6]
	 * value: 1是已经挑战, 没有值表示未挑战
	 */
	private HashMap<Integer, Integer> scenes = new HashMap<>();

	@JsonIgnore
	private int lastFlushDay;

	public DailySceneBean() {}

	public DailySceneBean(long id) {
		this.id = id;
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

	public HashMap<Integer, Integer> getScenes() {
		return scenes;
	}

	public void setScenes(HashMap<Integer, Integer> scenes) {
		this.scenes = scenes;
	}

	public int getLastFlushDay() {
		return lastFlushDay;
	}

	public void setLastFlushDay(int lastFlushDay) {
		this.lastFlushDay = lastFlushDay;
	}

	/**
	 * 根据vip等级重新更新当前的任务状态
	 */
	public void flush() {
		int today = TimeUtil.getToday();
		if (today != lastFlushDay) {
			scenes.clear();
			lastFlushDay = today;
		}
	}
}
