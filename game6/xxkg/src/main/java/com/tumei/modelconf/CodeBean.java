package com.tumei.modelconf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.utils.TimeUtil;
import com.tumei.model.beans.ChargeDayBean;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

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
	// 领取时间
	private Date time;
	// 奖品 偶数id 奇数count
	private int[] awards;
	/**
	 *
	 * 奖励模式, 同一个模式下玩家只能领取一次
	 *
	 */
	private String mode = "";
}
