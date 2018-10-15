package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2018/7/24
 */
@Document(collection = "Dailysupply")
public class DailysupplyConf {
    @Id
    public String ObjectId;

    public int key;
    public int [] rewards;
}
