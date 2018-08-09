package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2018/7/27
 */
@Document(collection = "Rdshop")
public class RdshopConf {
    @Id
    public String objectId;

    public int key;

    public int type;

    public int limit;

    public int level;

    public int [] rewards;

    public int cost;
}
