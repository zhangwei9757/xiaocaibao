package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 社交关系信息:
 *
 *  Rules:
 *  1. 每个玩家设备有一个邀请码，可以接受最多50个其他独立设备的加入邀请
 *  2. 每个玩家设备只能加入一个其他玩家设备的邀请中
 *
 */
@Document(collection = "Configs")
public class ConfigBean {
    @Id
    public String ObjectId;
    @Field(value = "id")
    private Long id; // 帐号

    private String name;

    private String val;

    private int mode;

    private String desc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "ConfigBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", val='" + val + '\'' +
                ", mode=" + mode +
                ", desc='" + desc + '\'' +
                '}';
    }
}