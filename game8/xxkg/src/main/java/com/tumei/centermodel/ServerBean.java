package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by leon on 2017/3/3.
 *
 * 公告与服务器列表
 *
 */
@Document(collection = "Servers")
public class ServerBean {
    @Id
    public String objectId;
    /**
     * 服务器id
     */
    @Field("id")
    public int id;
    /**
     * 服务器名
     */
    public String name;
    /**
     * 服务器状态
     */
    public String status;
    /**
     * 服务器连接地址
     */
    public String host;
    /**
     * 生效时间
     */
    public Date start;

    @Override
    public String toString() {
        return "ServerStruct{" + "objectId='" + objectId + '\'' + ", id=" + id + ", name='" + name + '\'' + ", status='" + status + '\'' + ", host='" + host + '\'' + ", start=" + start + '}';
    }
}
