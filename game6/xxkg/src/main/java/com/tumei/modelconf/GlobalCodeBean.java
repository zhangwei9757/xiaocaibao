package com.tumei.modelconf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.HashSet;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 优惠码
 *
 */
@Document(collection = "GlobalCode")
public class GlobalCodeBean {
	@JsonIgnore
	@Id
	private String objectId;

	@Field("id")
	@Indexed(unique = true)
	private String id;

	// 奖品 偶数id 奇数count
	private int[] awards;

	private int flag;

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

	public int[] getAwards() {
		return awards;
	}

	public void setAwards(int[] awards) {
		this.awards = awards;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}
}
