package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/4/11 0011.
 */
@Document(collection = "Role.Robs")
public class RobBean {
	@JsonIgnore
	@Id
	private String ObjectId;
	@Field("id")
	private Long id;
	/**
	 * 所有拥有的碎片
	 */
	private HashMap<Integer, Integer> frags = new HashMap<>();

	/**
	 * 保护结束时间
	 */
	private Date time = new Date();

	public RobBean() { }

	public RobBean(long _id) {
		id = _id;
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

	public HashMap<Integer, Integer> getFrags() {
		return frags;
	}

	public void setFrags(HashMap<Integer, Integer> frags) {
		this.frags = frags;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public synchronized void notProtect() {
		this.time = new Date(0);
	}

	public synchronized boolean isProtect() {
		return new Date().before(this.time);
	}

	public synchronized void protect(long secs) {
		Date now = new Date();
		if (this.time.before(now)) {
			this.time = new Date(now.getTime() + secs * 1000);
		} else {
			this.time = new Date(this.time.getTime() + secs * 1000);
		}
	}

	@Override
	public String toString() {
		return "RobBean{" + "ObjectId='" + ObjectId + '\'' + ", id=" + id + ", frags=" + frags + ", time=" + time + '}';
	}
}
