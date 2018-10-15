package com.tumei.common.group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/5/10 0010.
 * <p>
 * 群组信息
 */
public class GroupMessage {
	public long gid;
	public String name;
	public int icon;
	public int approval;
	public int zone;
	public long create;
	public int contrib;
	public String desc;
	public String notify;
	public int level;
	public int exp;

	// 玩家列表
	public List<GroupRoleMessage> roles = new ArrayList<>();
	public List<GroupRoleMessage> pres = new ArrayList<>();

}
