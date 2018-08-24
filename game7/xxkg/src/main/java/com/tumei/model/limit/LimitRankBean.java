package com.tumei.model.limit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 * 注灵狂欢和限时抽奖 活动
 */
@Data
@Document(collection = "Role.Happy")
public class LimitRankBean {
    @JsonIgnore
    @Id
    private String objectId;

    @JsonIgnore
    @Field("id")
    @Indexed(unique = true, name = "i_id")
    private Long id;

    private String name = "";

    // 当前活动的总次数, 两种活动均可以使用这个字段进行记录
    private long count;

    private int rank;

    /**
     * 已经领取的个人奖励次数标记
     */
    private int[] awd = new int[5];

    public void updaAwd(int index, int update) {
        this.awd[index] = update;
    }

    public LimitRankBean() {
    }

    public LimitRankBean(long _id) {
        this.id = _id;
    }
}
