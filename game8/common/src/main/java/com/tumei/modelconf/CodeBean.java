package com.tumei.modelconf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
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
@Data
@Document(collection = "Role.Code")
public class CodeBean {
	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private String id;

	// 领取截止时间, 超过之后表示已经不能满足了
	private Date time;

	// 领取要求的等级
	private int level;

	// 领取要求的vip
	private int vip;

	// 奖品 偶数id 奇数count
	private int[] awards;

	/**
	 * 奖励的模式:
	 *
	 * 用来确定玩家在该模式下是否还能领取
	 *
	 */
	private String mode = "";
}

