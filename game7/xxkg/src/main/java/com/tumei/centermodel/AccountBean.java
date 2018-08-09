package com.tumei.centermodel;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
	private String account;
	private String passwd;
	private int openrmb; // 开服积累的人民币
}
