package com.tumei.game.protos.mail;

import com.tumei.game.GameUser;
import com.tumei.model.MailsBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.MailBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestReadMail extends BaseProtocol {
    public int seq;

	/**
	 * 序号:  1开始，如果是0表示一次性读取或者删除
	 */
	public int index;

	/**
	 * 哪种邮件: 0表示 信息邮件  1是奖励邮件
	 */
	public int mode;

    class ReturnReadMail extends BaseProtocol {
		public int seq;
		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnReadMail rl = new ReturnReadMail();

		MailsBean msb = user.getDao().findMails(user.getUid());

		if (index == 0) {
			if (mode == 0) {
				msb.getInfos().clear();
			} else {
				List<MailBean> mails = msb.getAwards();
				mails.forEach((MailBean mb) -> {
					String[] fields = mb.awards.split(",");
					for (int i=0; (i+1) < fields.length; i += 2) {
						int aid = Integer.parseInt(fields[i]);
						long count = Long.parseLong(fields[i+1]);
						rl.awards.addAll(user.addItem(aid, count, false, "邮件"));
					}
				});
				mails.clear();
			}
		} else {
			if (mode == 0) {
				msb.getInfos().remove(index - 1);
			} else {
				MailBean mb = msb.getAwards().get(index - 1);
				String[] fields = mb.awards.split(",");
//				if ((fields.length % 2) == 0) {
					for (int i=0; (i+1) < fields.length; i += 2) {
						int aid = Integer.parseInt(fields[i]);
						long count = Long.parseLong(fields[i+1]);
						rl.awards.addAll(user.addItem(aid, count, false, "邮件"));
					}
//				}

				msb.getAwards().remove(index - 1);
			}
		}

		rl.seq = seq;
        user.send(rl);
    }
}
