package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.game.services.TreasureRankService;
import com.tumei.modelconf.DailytreasureConf;
import org.bouncycastle.asn1.dvcs.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 日期做键值的统计:
 *
 * 每日活跃玩家人数，活跃玩家id，当日充值，
 *
 */
@Document(collection = "DataSta")
public class DataStaBean {
	@JsonIgnore
	@Id
	private String objectId;

	@Field("id")
	private int date;

	private int oldCharge; // 今日老玩家充值
	private int newCharge; // 今日新玩家充值

	private HashSet<Long> olds = new HashSet<>(); // 旧的活跃人数

	private HashSet<Long> news = new HashSet<>(); // 今日新玩家人数

	public DataStaBean() {}

	public DataStaBean(int date) {
		this.date = date;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public synchronized int getOldCharge() {
		return oldCharge;
	}

	public synchronized void setOldCharge(int oldCharge) {
		this.oldCharge = oldCharge;
	}

	public synchronized int getNewCharge() {
		return newCharge;
	}

	public synchronized void setNewCharge(int newCharge) {
		this.newCharge = newCharge;
	}

	public synchronized HashSet<Long> getOlds() {
		return olds;
	}

	public synchronized void setOlds(HashSet<Long> olds) {
		this.olds = olds;
	}

	public synchronized HashSet<Long> getNews() {
		return news;
	}

	public synchronized void setNews(HashSet<Long> news) {
		this.news = news;
	}

	public synchronized void addOldUser(long _id) {
		olds.add(_id);
	}

	public synchronized void addNewUser(long _id) {
		news.add(_id);
	}

	public synchronized void addOldCharge(int rmb) {
		oldCharge += rmb;
	}

	public synchronized void addNewCharge(int rmb) {
		newCharge += rmb;
	}

	public synchronized int getDau() {
		return olds.size() + news.size();
	}

	public synchronized int getCharge() {
		return oldCharge + newCharge;
	}
}
