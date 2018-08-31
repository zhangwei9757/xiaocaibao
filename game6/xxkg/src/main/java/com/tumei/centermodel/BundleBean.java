package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by leon on 2016/11/5.
 *
 * 所有允许注册和登录的包名
 *
 */
@Document(collection = "Bundles")
public class BundleBean {
    @Id
    public String id;

    public String bundle;

    public BundleBean() {}
}
