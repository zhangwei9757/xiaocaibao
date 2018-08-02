package com.tumei.game.protos.mail;

import com.tumei.game.GameUser;
import com.tumei.websocket.WebSocketUser;
import com.tumei.model.MailsBean;
import com.tumei.model.beans.MailBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestMails extends BaseProtocol {
    public int seq;

    class ReturnMails extends BaseProtocol {
		public int seq;
		/**
		 * 消息通知
		 */
		public List<MailBean> infos;
		/**
		 * 奖励邮件
		 */
		public List<MailBean> awards;
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;

        ReturnMails rl = new ReturnMails();
		rl.seq = seq;

		MailsBean msb = user.getDao().findMails(user.getUid());
		rl.infos = msb.getInfos();
		rl.awards = msb.getAwards();

        user.send(rl);
    }
}
