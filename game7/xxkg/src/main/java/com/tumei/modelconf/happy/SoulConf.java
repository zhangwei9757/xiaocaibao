package com.tumei.modelconf.happy;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Soullist")
public class SoulConf {
	@Id
	public String id;

	public int key;
	public int mode;
	public String name;
	public int flag; // 1:注灵狂欢活动, 2:抽奖活动
    public int start;
    public int last;
    public int time;
    // 达标次数，可领取的奖励
    public int[] reward;
}
