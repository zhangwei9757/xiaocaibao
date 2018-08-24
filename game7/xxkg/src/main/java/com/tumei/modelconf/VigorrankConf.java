package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/08/21
 *
 * <p>
 * 活力狂欢
 */
@Document(collection = "Vigorrank")
public class VigorrankConf {
    @Id
    public String id;
    public int key;
    // 排名奖励1
    public int[] reward1;
    public int limit;
    // 累计奖励2
    public int[] reward2;
    public int limit3;
    // 累计奖励3
    public int[] reward3;
}
