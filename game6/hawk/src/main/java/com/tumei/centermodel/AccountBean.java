package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "Account")
public class AccountBean {
    @Id
    private String ObjectId;
    @Field("id")
    private Long id;
    private String account;
    private String passwd;
    private String role;
    private String digest;
    private Date createtime;
    private int status;
    private Date forbidtime;
    private String source;
    private String idfa;
    private String ip;
    private String os;

    public AccountBean() {}

    public AccountBean(Long id, String account, String passwd, Date createtime, String source, String idfa) {
        this.id = id;
        this.account = account;
        this.passwd = passwd;
        this.createtime = createtime;
        this.source = source;
        this.idfa = idfa;
    }

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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    @Override
    public String toString() {
        return "AccountBean{" + "ObjectId='" + ObjectId + '\'' + ", id=" + id + ", account='" + account + '\'' + ", passwd='" + passwd + '\'' + ", role='" + role + '\'' + ", digest='" + digest + '\'' + ", createtime=" + createtime + ", status=" + status + ", forbidtime=" + forbidtime + ", source='" + source + '\'' + ", idfa='" + idfa + '\'' + '}';
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getForbidtime() {
        return forbidtime;
    }

    public void setForbidtime(Date forbidtime) {
        this.forbidtime = forbidtime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIdfa() {
        return idfa;
    }

    public void setIdfa(String idfa) {
        this.idfa = idfa;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }
}
