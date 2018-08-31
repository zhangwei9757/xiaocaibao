package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Equrecost")
public class EquiprefcostConf {
	@Id
	public String ObjectId;

	public int key;
	public int gr;
	public int bl;
	public int pu;
	public int or;
	public int re;

}
