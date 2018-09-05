package com.tumei.centermodel;

import com.tumei.centermodel.beans.UserRoleBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by leon on 2016/11/5.
 *
 * 玩家与服务器之间的映射关系
 *
 */
@Document(collection = "Users")
public class UserBean {
	@Id
	private String objectId;

	@Field("id")
	private Long id;

    /**
     * 上次登录的服务器
     */
	private int last;

	private int charge;

	private List<UserRoleBean> relates = new ArrayList<>();

    public UserBean() {}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getLast() {
		return last;
	}

	public void setLast(int last) {
		this.last = last;
	}

	public List<UserRoleBean> getRelates() {
		return relates;
	}

	public void setRelates(List<UserRoleBean> relates) {
		this.relates = relates;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	/**
	 * 去重增加玩家所在的服务器
	 * @param server
	 */
	public void update(long role, int server, int level, int vip, int icon, String name) {
		Optional<UserRoleBean> opt = relates.stream().filter(r -> r.server == server).findFirst();
		UserRoleBean urb;
		if (opt.isPresent()) {
			urb = opt.get();
		} else {
			urb = new UserRoleBean();
			relates.add(urb);
		}

		urb.role = role;
		urb.server = server;
		urb.level = level;
		urb.vip = vip;
		urb.icon = icon;
		urb.name = name;
	}
}
