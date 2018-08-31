package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by leon on 2016/11/5.
 */
@Data
@Document(collection = "Role")
public class RoleBean {
	@JsonIgnore
	@Id
	private String objectId;

	@Field("id")
	private Long id;
	/**
	 * 昵称
	 */
	private String nickname;
	/**
	 * 领主品质，由英雄列表中领主英雄的等级决定
	 */
	private int grade = 2;
	/**
	 * 头像
	 */
	private int icon = 0;
	/**
	 * 领主等级
	 */
	private int level = 1;
	/**
	 * 领主经验
	 */
	private int exp;
	/**
	 * gm等级
	 */
	private int gmlevel;
	/**
	 * 新手步骤
	 */
	private int newbie;
	/**
	 * 总在线
	 */
	private int totaltime;
	/**
	 * 今日在线
	 */
	private int todaytime;
	/**
	 * 最近登录时间
	 */
	private Date logtime;
	/**
	 * 最近登出时间
	 */
	private Date logouttime;
	/**
	 * 0：不在线
	 * 1: 在线
	 */
	private int online;
	/**
	 * 上次登录天,判定是否跨天，跨天后logdays要增加
	 */
	private int logDay;
	/**
	 * 登录总天数
	 */
	private int logdays;
	/**
	 * vip等级
	 */
	private int vip;
	/**
	 * vip经验
	 */
	private int vipexp;
	/**
	 * 角色创建时间
	 */
	private Date createtime;
	/**
	 * 禁言结束时间
	 */
	private long saytime;

	/**
	 * 禁止登录结束时间
	 */
	private long playtime;
	/**
	 * 设备标识符(最近登录使用的)
	 */
	private String idfa;

	/**
	 * 性别
	 */
	private int sex;
}
