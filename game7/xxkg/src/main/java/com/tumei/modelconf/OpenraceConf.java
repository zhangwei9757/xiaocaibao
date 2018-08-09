package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Openrace")
public class OpenraceConf {
	@Id
	public String objectId;

	public int key;
	public String powerrank;
	public String raidrank;
	public String levrank;
	public String firrank;
	public String recrank;
	public String sperank;
}

