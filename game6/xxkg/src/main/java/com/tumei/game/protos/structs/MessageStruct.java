package com.tumei.game.protos.structs;

/**
 * Created by Administrator on 2017/3/6 0006.
 */
public class MessageStruct {
	/**
	 * 发送者id
	 */
	public long id;
	/**
	 * 消息类型:
	 * 0 普通消息
	 * 1 公会聊天消息
	 * 2 公会通知消息
	 * 3 单服聊天
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
	 * 发送者公会
	 */
	public String union;
	/**
	 * 消息内容
	 */
	public String msg;


	@Override
	public String toString() {
		return "MessageStruct{" + "id=" + id + ", rmb=" + mode + ", zone=" + zone + ", icon=" + icon + ", vip=" + vip + ", flag=" + flag + ", name='" + name + '\'' + ", union='" + union + '\'' + ", msg='" + msg + '\'' + '}';
	}
}
