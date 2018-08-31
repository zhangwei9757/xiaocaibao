package com.tumei.centermodel;

import com.tumei.centermodel.beans.ServerBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2017/3/3.
 *
 * 公告与服务器列表
 *
 */
@Document(collection = "Servers")
public class ServersBean {
    @Id
    private String ObjectId;

    /**
     * 公告
     */
    private String descrition;

    /**
     * 服务器列表
     */
    private List<ServerBean> servers = new ArrayList<>();

    public String getObjectId() {
        return ObjectId;
    }

    public void setObjectId(String objectId) {
        ObjectId = objectId;
    }

    public String getDescrition() {
        return descrition;
    }

    public void setDescrition(String descrition) {
        this.descrition = descrition;
    }

    public List<ServerBean> getServers() {
        return servers;
    }

    public void setServers(List<ServerBean> servers) {
        this.servers = servers;
    }
}
