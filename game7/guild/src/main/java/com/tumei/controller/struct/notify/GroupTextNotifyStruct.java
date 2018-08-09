package com.tumei.controller.struct.notify;

import com.tumei.controller.struct.GroupRole;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Leon on 2017/5/12 0012.
 *
 * 文本通知
 *
 */
public class GroupTextNotifyStruct {
	/**
	 * 通知对象
	 */
	public List<Long> users = new ArrayList<>();

	/**
	 * 通知内容
	 */
	public String text = "";

	public GroupTextNotifyStruct() {}

	public GroupTextNotifyStruct(String text) {
		this.text = text;
	}

	public void addUser(long id) {
		users.add(id);
	}

	public void addUserIds(Collection<Long> _ids) {
		users.addAll(_ids);
	}

	public void addUsers(Collection<GroupRole> _users) {
		_users.stream().forEach((r) -> {
			users.add(r.id);
		});
	}
}
