package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.utils.TimeUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 * 在地图上的矿脉信息
 */
@Document(collection = "Role.boss")
public class BossBean {
	@JsonIgnore
	@Id
	public String objectId;

	@Field("id")
	private Long id;

	/**
	 * 上次更新时间
	 */
	private int day;

	/**
	 * 今日剩余可以挑战的次数
	 */
	private int count = 5;

	/**
	 * 下次可以挑战的时间
	 */
	private long next = 0;

	/**
	 * 激励属性的顺序,[0,5] 顺序激活索引对应的激励增强属性
	 */
	private List<Integer> courage = new ArrayList<>();

	/**
	 * 当前激励到第几个,0标识没有开始激励
	 */
	private int courageIdx = 0;

	/**
	 * 今日挑战的boss的等级
	 */
	private int level;

	public BossBean() {
	}

	public void refresh() {
		int today = TimeUtil.getToday();
		if (today != day) {
			day = today;
			count = 5;

			if (courage.size() < 6) {
				courage.clear();
				for (int i = 0; i < 6; ++i) {
					courage.add(i);
				}
			}

			Collections.shuffle(courage);
			courageIdx = 0;
		}
	}

	/**
	 * 是否可以继续激励
	 * @return
	 */
	public boolean canCourage() {
		return (courageIdx < 6);
	}

	/**
	 * 激励
	 */
	public void doCourage() {
		if (courageIdx >= 6) {
			return;
		}
		++courageIdx;
	}

	public void clearCourage() {
		Collections.shuffle(courage);
		courageIdx = 0;
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

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<Integer> getCourage() {
		return courage;
	}

	public void setCourage(List<Integer> courage) {
		this.courage = courage;
	}

	public int getCourageIdx() {
		return courageIdx;
	}

	public void setCourageIdx(int courageIdx) {
		this.courageIdx = courageIdx;
	}

	public long getNext() {
		return next;
	}

	public void setNext(long next) {
		this.next = next;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
