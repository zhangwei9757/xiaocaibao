package com.tumei.modelconf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
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
@Data
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

	private int level;

	private int vip;
}
