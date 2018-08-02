package com.tumei.centermodel;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Date;

/**
 * Created by leon on 2016/11/5.
 */
@Data
@Document(collection = "GmOpers")
public class GmOperBean {
	@Id
	private String id;

	// 管理员
	private String gm;
	private Date time;
	private String data;

	/**
	 * 1 充值
	 */
	private int mode;

	public GmOperBean() {
	}

	public GmOperBean(int mode, String data) {

		try {
			Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (p != null) {
				this.gm = p.toString();
			} else {
				this.gm = "未知管理者";
			}
		} catch (Exception e) {

		}

		this.data = data;
		this.mode = mode;
		this.time = new Date();
	}
}
