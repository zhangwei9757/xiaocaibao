package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.model.structs.GroupPreRole;
import com.tumei.model.structs.GroupRole;
import com.tumei.model.structs.GroupScene;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "Groups")
public class GroupBean {
	@Id
	public String objectId;
	@Field("id")
	public Long id;

	public String name = "";

	public int icon;

	public boolean dirty;

	/**
	 * 创建时间
	 */
	public Date create;

	/**
	 * 公会所在服务器
	 */
	public int zone;

	/**
	 * 今日贡献总数值
	 */
	public int progress;

	/**
	 * 加入公会的方案
	 * 0: 自动加入
	 * 1: 需要审批
	 * 2: 拒绝加入
	 */
	public int approval;

	/**
	 * 军团贡献
	 */
	public int contrib;
	/**
	 * 军团等级
	 */
	public int level = 1;
	/**
	 * 军团经验
	 */
	public int exp;

	/**
	 * 总军团经验
	 */
	public int allExp;

	/**
	 * 军团描述
	 */
	public String desc = "";

	/**
	 * 内部公告
	 */
	public String notify = "";

	public HashMap<Long, GroupRole> roles = new HashMap<>();

	/**
	 * 待审批成员
	 */
	public List<GroupPreRole> preRoles = new ArrayList<>();

	/**
	 * 按照服务器分块的成员列表, 不会保存到数据库
	 */
	public HashMap<Integer, List<GroupRole>> zoneRoles = new HashMap<>();

	/**
	 * 需要广播全体的消息
	 */
	public List<String> messages = new ArrayList<>();

	public int flushDay;

	public GroupScene scene = new GroupScene();

	/**
	 * 消息记录,登录公会的时候返回以前的100条老消息
	 */
	public List<String> notifys = new ArrayList<>();
}
