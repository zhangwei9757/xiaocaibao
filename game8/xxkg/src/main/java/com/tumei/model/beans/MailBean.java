package com.tumei.model.beans;

import java.util.Date;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
public class MailBean {
	/**
	 * 邮件日期
	 */
	public Date ts = new Date();
	/**
	 * 标题
	 */
	public String title = "";
	/**
	 * 邮件主体内容
	 */
	public String content = "";
	/**
	 * 邮件奖励
	 */
//	public List<AwardBean> rewards = new ArrayList<>();
	public String awards = "";

	public MailBean() {}

	public MailBean(String _title, String _data) {
		title = _title;
		content = _data;
	}
}
