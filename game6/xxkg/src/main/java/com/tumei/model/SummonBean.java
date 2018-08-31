package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.game.GameUser;
import com.tumei.common.utils.TimeUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 召唤英雄相关的记录
 *
 */
@Document(collection = "Role.Summon")
public class SummonBean {

	public SummonBean() {}

	public SummonBean(long _id) {
		id = _id;
	}

	@JsonIgnore
	@Id
	private String ObjectId;
	@Field("id")
	private Long id;
	/**
	 * 刷新日期
	 */
	private int flushDay;
	/**
	 * 普通召唤 上次使用免费的时间
	 */
	private long lastSmallFree;

	/**
	 * 普通召唤剩余的免费次数
	 */
	private int smallFreeCount;
	/**
	 * 进行普通召唤的总次数
	 */
	private int smallCount;
	/**
	 * 中等召唤的上次使用时间, 24小时可以免费一次
	 */
	private long lastMiddleFree;
	/**
	 * 今日使用中等召唤时，未使用传奇卡的次数
	 */
	private int todayCount;
	/**
	 * 进行中等召唤的总次数
	 */
	private int middleCount;

	/**
	 * 今日的高级抽奖是否有免费
	 */
	private int advanceFreeCount;

	/**
	 * 进行高级召唤的总次数
	 */
	private int advanceCount;

	/**
	 * 高级英雄池的索引[0,3]
	 */
	private int advanceIndex;

	/**
	 * 幸运值, 到1000后不再增加，同时使用1000点可以兑换任意1个红色英雄
	 */
	private int lucky;

	/**
	 * 通过幸运兑换的次数
	 */
	private int luckyCount;

	/**
	 * 刷新进入的英雄召唤
	 */
	public void flushSummon(GameUser user) {
		int today = TimeUtil.getToday();
		if (today > flushDay) {
			flushDay = today;
			lastSmallFree = 0;
			smallFreeCount = 3;
			todayCount = 0;
			advanceFreeCount = 1;
			advanceIndex = (advanceIndex + 1) % 4;
		}
	}

	/**
	 * 增加幸运值,不能超过1000
	 *
	 * @param _lucky
	 */
	public void addLucky(int _lucky) {
		lucky += _lucky;
		if (lucky > 1000) {
			lucky = 1000;
		}
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

	public int getFlushDay() {
		return flushDay;
	}

	public void setFlushDay(int flushDay) {
		this.flushDay = flushDay;
	}

	public int getSmallCount() {
		return smallCount;
	}

	public void setSmallCount(int smallCount) {
		this.smallCount = smallCount;
	}

	public long getLastMiddleFree() {
		return lastMiddleFree;
	}

	public void setLastMiddleFree(long lastMiddleFree) {
		this.lastMiddleFree = lastMiddleFree;
	}

	public int getTodayCount() {
		return todayCount;
	}

	public void setTodayCount(int todayCount) {
		this.todayCount = todayCount;
	}

	public int getMiddleCount() {
		return middleCount;
	}

	public void setMiddleCount(int middleCount) {
		this.middleCount = middleCount;
	}

	public int getAdvanceFreeCount() {
		return advanceFreeCount;
	}

	public void setAdvanceFreeCount(int advanceFreeCount) {
		this.advanceFreeCount = advanceFreeCount;
	}

	public int getAdvanceCount() {
		return advanceCount;
	}

	public void setAdvanceCount(int advanceCount) {
		this.advanceCount = advanceCount;
	}

	public int getAdvanceIndex() {
		return advanceIndex;
	}

	public void setAdvanceIndex(int advanceIndex) {
		this.advanceIndex = advanceIndex;
	}

	public int getLucky() {
		return lucky;
	}

	public void setLucky(int lucky) {
		this.lucky = lucky;
	}

	public long getLastSmallFree() {
		return lastSmallFree;
	}

	public void setLastSmallFree(long lastSmallFree) {
		this.lastSmallFree = lastSmallFree;
	}

	public int getSmallFreeCount() {
		return smallFreeCount;
	}

	public void setSmallFreeCount(int smallFreeCount) {
		this.smallFreeCount = smallFreeCount;
	}

	public int getLuckyCount() {
		return luckyCount;
	}

	public void setLuckyCount(int luckyCount) {
		this.luckyCount = luckyCount;
	}
}
