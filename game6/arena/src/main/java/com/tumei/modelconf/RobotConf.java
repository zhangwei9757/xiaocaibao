package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Robot")
public class RobotConf {
	@Id
	public String ObjectId;
	public int key;
	/**
	 * 排名最高值<= rank
	 */
	public int rank;
	/**
	 * 等级
	 */
	public int level;
	/**
	 * 突破
	 */
	public int grade;
	/**
	 * 所有英雄的品质在以下列表中随机
	 */
	public int[] quality;

}
