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
 * 服务器排名
 *
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

	// 昵称
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

	/**
	 * 为了在跳表中稳定排序，对6个指标都增加了一个写入时间，时间越早的写入者，相同的值排序更靠前
	 */
	private long[] index = new long[6];

	public long getOpIdx(int _idx) {
		return index[_idx];
	}
	public void setOpIdx(int _idx) {
		index[_idx] = System.currentTimeMillis();
	}

	/**
	 *
	 * 如果opIdx是默认为0的，则填充值进去
	 *
	 * @param _id
	 */
	public void updateOps(long _id) {
		if (index[0] != 0) {
			return;
		}

		for (int i = 0; i < index.length; ++i) {
			index[i] = _id;
		}
	}

	public OpenRankBean() { }

	public OpenRankBean(long uid) {
		this.id = uid;
	}
}
