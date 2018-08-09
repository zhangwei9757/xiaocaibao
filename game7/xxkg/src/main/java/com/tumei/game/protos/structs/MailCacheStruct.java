package com.tumei.game.protos.structs;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 批量发送邮件的时候，用于暂存邮件信息
 *
 */
public class MailCacheStruct {

	public long id;
	public String title = "";
	public String content = "";
	public String awards = "";

	public MailCacheStruct(long _id, String _title, String _content)
	{
		id = _id;
		title = _title;
		content = _content;
	}

	public void addAward(int[] _awards)
	{
		for (int i = 0; i < _awards.length; ++i) {
//			if (i == _awards.length - 1) {
//				awards += _awards[i];
//			} else {
				awards += _awards[i] + ",";
//			}
		}
	}
}
