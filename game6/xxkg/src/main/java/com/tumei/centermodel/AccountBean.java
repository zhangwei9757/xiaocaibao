package com.tumei.centermodel;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "Account")
public class AccountBean {
	@Id
	public String ObjectId;

	@Field("id")
	private Long id;
	private String account;
	private String passwd;
	private String role;
	private DateTime createtime;

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

	public String getRole() {
		return role;
	}

	public void setRole(String _role) {
		if (this.role != null && !this.role.isEmpty()) {
			String[] roles = this.role.split(",");
			for (String r : roles) {
				if (r.equalsIgnoreCase(_role)) {
					return;
				}
			}

			this.role += "," + _role;
		} else {
			this.role = _role;
		}
	}

	public DateTime getCreatetime() {
		return createtime;
	}

	public void setCreatetime(DateTime createtime) {
		this.createtime = createtime;
	}

	public AccountBean() {}
}
