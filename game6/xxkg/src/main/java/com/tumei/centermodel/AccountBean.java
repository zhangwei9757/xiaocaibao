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
	private String channel;
	private int charge;
	private int chargecount;
	private int openrmb;
}
