package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.utils.TimeUtil;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by Administrator on 2018/7/24
 *
 * 每日限时领取
 *
 */
@Data
@Document(collection = "Role.LimitReceive")
public class LimitReceiveBean {
	public LimitReceiveBean() {}

	public LimitReceiveBean(long _id) {
		id = _id;
	}

	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private Long id;

	private int day;

	private int [] counts ={0,0,0};

	public void flush(){
		int today = TimeUtil.getToday();
		if (day != today) {
			day = today;
			for (int i = 0; i < counts.length; i++) {
				counts [i] = 0;
			}
		}
	}
}


