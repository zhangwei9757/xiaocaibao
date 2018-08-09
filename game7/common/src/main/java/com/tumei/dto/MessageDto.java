package com.tumei.dto;

/**
 * Created by Administrator on 2017/3/6 0006.
 */
public class MessageDto {
	/**
	 * 发送者id
	 */
	public long id;
	/**
	 * 消息类型:
	 * 0 普通消息
	 * 1 全服消息
	 * 2 公会消息
	 */
	public int mode;
	/**
	 * 发送者服务器id
	 */
	public int zone;
	/**
	 * 头像
	 */
	public int icon;
	/**
	 * vip等级
	 */
	public int vip;
	/**
	 * 领主等级
	 *
	 */
	public int flag;
	/**
	 * 发送者昵称
	 */
	public String name;
	/**
	 * 消息内容
	 */
	public String msg;

	@Override
	public String toString() {
		return "MessageDto{" + "id=" + id + ", mode=" + mode + ", zone=" + zone + ", icon=" + icon + ", vip=" + vip + ", flag=" + flag + ", name='" + name + '\'' + ", msg='" + msg + '\'' + '}';
	}
}
