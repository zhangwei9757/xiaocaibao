package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Texp")
public class TeamExpConf {
	@Id
	public String ObjectId;

	public int key;

	public long cost;
}
