package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by leon on 2016/11/5.
 *
 * 服务器相关参数
 *
 */
@Document(collection = "Params")
public class ParamBean {
    @Id
    public String id;

    // 配置的关键字
    public String key;

    // 配置的内容
    public String value;

    public ParamBean() {}
}
