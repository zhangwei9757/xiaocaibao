package com.tumei.modelconf.limit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/11
 */
@Document(collection = "Invtotal")
public class InvtotalConf {
    @Id
    public String objectId;

    public int key;

    public int cost;

    public int[] reward;
}
