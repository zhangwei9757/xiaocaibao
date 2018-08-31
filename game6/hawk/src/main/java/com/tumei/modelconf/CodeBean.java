package com.tumei.modelconf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 优惠码
 *
 */
@Document(collection = "Role.Code")
public class CodeBean {
	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private String id;
	// 领取时间
	private Date time;
	// 奖品 偶数id 奇数count
	private int[] awards;

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

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int[] getAwards() {
		return awards;
	}

	public void setAwards(int[] awards) {
		this.awards = awards;
	}
}
