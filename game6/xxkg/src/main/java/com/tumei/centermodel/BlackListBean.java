package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "BlackList")
public class BlackListBean {
	@Id
	public String ObjectId;
    @Field("id")
    public Long id;
    public String account;
    public Date forbidtime; // 指定封禁的时间
	public String gamemaster; // 管理员名字
    public String reason;
    public Date endtime; // 封闭结束时间

    public BlackListBean() {}

    @Override
    public String toString() {
        return "BlackListBean{" + "id=" + id + ", account='" + account + '\'' + ", forbidtime=" + forbidtime + ", gamemaster='" + gamemaster + '\'' + ", reason='" + reason + '\'' + ", endtime=" + endtime + '}';
    }
}
