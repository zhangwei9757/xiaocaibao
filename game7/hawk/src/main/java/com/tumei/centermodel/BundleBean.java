package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by leon on 2016/11/5.
 *
 * 所有允许注册和登录的包名
 *
 */
@Document(collection = "Bundles")
public class BundleBean {
    @Id
    public String objectId;

    public String bundle;

    // 云顶或者其他渠道需要根据包名登陆时发送不同的id参数
    public String appid = "";

    // 支付验证签名
    public String pay = "";

    public BundleBean() {}
}
