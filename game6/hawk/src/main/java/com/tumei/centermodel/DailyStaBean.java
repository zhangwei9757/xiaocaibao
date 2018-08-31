package com.tumei.centermodel;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.HashSet;

/**
 * Created by leon on 2016/11/5.
 */
@Data
@Document(collection = "DailyStats")
public class DailyStaBean {
    @Id
    private String id;

    // 日期
    private int day;

    // 总充值
    private int charge;

    // 所有登录的玩家
    private HashSet<Long> users = new HashSet<>();

    public void addCharge(int rmb) {
    	charge += rmb;
    }

    public void addUser(long uid) {
        users.add(uid);
    }
}
