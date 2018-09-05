package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/09/04 0017.
 */
@Document(collection = "Recreturn")
public class RecreturnConf {
    @Id
    public String ObjectId;
    public int key;
    public int total1;
    public int total2;
    public float returngem;
}
