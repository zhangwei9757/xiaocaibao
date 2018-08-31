package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "COUNTERS")
public class IDBean {
    @Id
    public String ObjectId;
    public Long nextval;
    public String name;

    public IDBean() {}
}
