package com.tumei.modelconf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 优惠码, 领取记录
 *
 */
@Data
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
}
