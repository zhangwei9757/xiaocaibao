package com.tumei.modelconf;

import com.tumei.dto.arena.LadderSimpleDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Toprank")
public class TopRankConf {
	@Id
	public String id;

	public int key;
	
	public String name;

	public int num;
	public int limit;
	public int[] attadd;
	// 勋章奖励
	public int goods;
	// 衰弱
	public int[] time;
	// 可以创建几个大组
	public int slot;

	public int[] newslot;
}
