package com.tumei.centermodel;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by leon on 2016/11/5.
 */
@Data
@Document(collection = "Account")
public class AccountBean {
    @Id
    private String objectId;
    @Field("id")
    private Long id;
    @Indexed(unique = true)
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
    private String channel;
    private int charge; // 总充值
    private int chargecount; // 充值次数
    private int openrmb; //开服测试积累的人民币数量
}
