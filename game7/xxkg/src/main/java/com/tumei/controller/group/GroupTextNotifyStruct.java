package com.tumei.controller.group;

import java.util.ArrayList;
import java.util.List;

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
}
