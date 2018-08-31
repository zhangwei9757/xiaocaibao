package com.tumei.centermodel.beans;

import java.util.Date;

/**
 * Created by leon on 2017/3/3.
 */
public class ServerBean {
    /**
     * 服务器id
     */
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
        return "ServerBean{" + "id=" + id + ", name='" + name + '\'' + ", status='" + status + '\'' + ", host='" + host + '\'' + ", start=" + start + '}';
    }
}
