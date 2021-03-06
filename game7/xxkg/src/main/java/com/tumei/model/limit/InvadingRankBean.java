package com.tumei.model.limit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by zw on 2018/09/28
 * <p>
 * 限时活动
 */
@Data
@Document(collection = "Role.InvadingRank")
public class InvadingRankBean {
    @JsonIgnore
    @Id
    private String objectId;

    @JsonIgnore
    @Field("id")
    @Indexed(unique = true, name = "i_id")
    private Long id;

    private String name = "";

    private long ts;

    /**
     * 当前活动的总次数, 多种活动均可以使用这个字段进行记录
     */
    private long count;

    public InvadingRankBean() {
    }

    public InvadingRankBean(long _id) {
        this.id = _id;
    }
}
