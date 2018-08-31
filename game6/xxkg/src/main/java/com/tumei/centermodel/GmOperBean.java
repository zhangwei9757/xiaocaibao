package com.tumei.centermodel;

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
				User user = (User)p;
				if (user != null) {
					this.gm = user.getUsername();
				} else {
					this.gm = p.toString();
				}
			}
		} catch (Exception e) {

		}

		this.data = data;
		this.mode = mode;
		this.time = new Date();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGm() {
		return gm;
	}

	public void setGm(String gm) {
		this.gm = gm;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}
}
