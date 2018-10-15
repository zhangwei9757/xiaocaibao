package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 签到
 */
@Document(collection = "Sign")
public class SignConf {
	@Id
	public String ObjectId;

	public int key;
	public int[] reward1;
	public int[] reward2;
	public int twice;
	public int type;
	public int cost;
	public int[] reward3;
}
