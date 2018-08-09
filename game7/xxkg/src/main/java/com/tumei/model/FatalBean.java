package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 * 服务器日常运行中可能的一些异常信息，需要维护人员甄别处理
 */
@Document(collection = "Fatals")
public class FatalBean {
	@JsonIgnore
	@Id
	private String id;

	private Date time;

	/**
	 * 玩家id
	 */
	private Long uid;

	/**
	 * 服务器类型
	 * <p>
	 * game
	 * cetner
	 * fighter
	 * chat
	 * or else
	 */
	private int serverType;

	/**
	 * 信息内容
	 */
	private String data;

	public FatalBean() {}

	public FatalBean(long _uid, String _info) {
		this.uid = _uid;
		this.data = _info;
		this.time = new Date();
	}

	public FatalBean(String _info) {
		this.data = _info;
		this.time = new Date();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
