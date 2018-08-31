package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 社交关系信息:
 *
 *  Rules:
 *  1. 每个玩家设备有一个邀请码，可以接受最多50个其他独立设备的加入邀请
 *  2. 每个玩家设备只能加入一个其他玩家设备的邀请中
 *
 */
@Document(collection = "Society")
public class SocietyBean {
    @Id
    private String ObjectId;
    @Field(value = "id")
    private Long id; // 帐号
    private String code; // 玩家设备的邀请码
    private Date time; // 时间
    private List<String> idfa = new ArrayList<>();

    public String getObjectId() {
        return ObjectId;
    }

    public void setObjectId(String objectId) {
        ObjectId = objectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public List<String> getIdfa() {
        return idfa;
    }

    public void setIdfa(List<String> idfa) {
        this.idfa = idfa;
    }

    @Override
    public String toString() {
        return "SocietyBean{" + "id=" + id + ", code='" + code + '\'' + ", time=" + time + ", idfa=" + idfa + '}';
    }
}