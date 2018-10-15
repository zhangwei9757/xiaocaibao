package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Refresh")
public class MineRefreshConf {
	@Id
	public String objectId;

	public int key;
	public int type;
	public int level;
	public int num;


}

