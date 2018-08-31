package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 * 服务器日常运行中可能的一些异常信息，需要维护人员甄别处理
 */
@Data
@Document(collection = "Role.OpenRanks")
public class OpenRankBean {
	@JsonIgnore
	@Id
	private String objectId;

	/**
	 * 玩家id
	 */
	@Field("id")
	private Long id;

	private String name;

	// 战斗力
	private long power;

	// 副本
	private int scene;

	// 领主等级
	private int level;

	// 远征星级
	private int star;

	// 充值
	private int charge;

	// 消费
	private int spend;

	public OpenRankBean() {}
	public OpenRankBean(long uid) { this.id = uid; }
}
