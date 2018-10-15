package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zw on 2018/08/21
 *
 * <p>
 * 英雄狂欢
 */
@Document(collection = "Summonrank")
public class SummonrankConf {
    @Id
    public String id;
    public int key;
    // 排名奖励1
    public int[] reward1;
    public int limit;
    // 排名奖励2
    public int[] reward2;
    public int limit3;
    // 排名奖励3
    public int[] reward3;

}
