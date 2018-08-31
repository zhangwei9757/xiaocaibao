package com.tumei.modelconf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 优惠码
 *
 */
@Document(collection = "GlobalCodeRecords")
public class GlobalCRBean {
	@JsonIgnore
	@Id
	private String objectId;

	@Field("id")
	@Indexed()
	private String id;

	// 领取的玩家
	@Indexed()
	private long uid;

	// 领取的时间
	private Date time;

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
}
