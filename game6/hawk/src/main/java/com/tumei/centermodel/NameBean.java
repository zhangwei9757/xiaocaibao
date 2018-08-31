package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "Names")
public class NameBean {

	@Id
    public String ObjectId;

    @Field("id")
    public Long id;
    public String name;

    public NameBean() {}

    @Override
    public String toString() {
        return "NameBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
