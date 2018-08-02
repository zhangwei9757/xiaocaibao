package com.tumei.modelconf.festival;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Festival")
public class FestivalConf {
	@Id
	public String id;

	public int key;
	public int mode;
	public int flag; // 0:节日活动, 1:神器活动
	public String name;
	public String des;
	// 节日货币
	public int[] fesdrop;
	public int start;
	public int last;
	// 副本小事件额外概率
	public int[] bonus1;
	public int[] bonus2;
	public int num;
	// 消费最大条数
	public int costnum;
}
