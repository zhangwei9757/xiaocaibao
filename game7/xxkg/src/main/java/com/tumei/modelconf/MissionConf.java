package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Missions")
public class MissionConf {
	@Id
	public String objectId;

	public int key;

	public int mode;
	public int cost;
	public int level;
	// 持续时间
	public int last;
	// 所需时间
	public int timeneed;
	public int[] reward1;
	public int[] reward2;
	public int[] reward3;
	public int[] reward4;

}
