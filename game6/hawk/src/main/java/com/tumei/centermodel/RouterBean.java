package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "Routers")
public class RouterBean {
    @Id
    public String ObjectId;
    @Field("id")
    public Integer id;
    public String name;
    public String versions;

    public RouterBean() {}

    @Override
    public String toString() {
        return "RouterBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
