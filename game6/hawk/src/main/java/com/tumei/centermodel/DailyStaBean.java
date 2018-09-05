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

    // 新增充值
    private int ncharge;

    // 所有登录的玩家
    private HashSet<Long> users = new HashSet<>();

    // 所有新增的玩家
    private HashSet<Long> nusers = new HashSet<>();

    // 所有充值的玩家
    private HashSet<Long> cusers = new HashSet<>();

    // 2日留存
    private float[] rs = new float[7];

    // false则在查询的时候会进行重建 charge r2 r7
    private boolean checked;

    public synchronized void addCharge(long uid, int rmb) {
        charge += rmb;
        cusers.add(uid);
        if (nusers.contains(uid)) {
            ncharge += rmb;
        }
    }

    public synchronized void addUser(long uid, boolean isNew) {
        users.add(uid);
        if (isNew) {
            nusers.add(uid);
        }
    }
}
