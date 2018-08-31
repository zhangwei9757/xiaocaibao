package com.tumei.game.protos.activity;

import com.tumei.game.services.FriendService;
import com.tumei.game.GameUser;
import com.tumei.model.FriendsBean;
import com.tumei.model.beans.FriendBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by leon on 2016/12/31.
 * 请求好友信息
 *
 */
@Component
public class RequestFriendsRecommand extends BaseProtocol {
    public int seq;

    class ReturnFriendsRecommand extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<FriendBean> recommands = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnFriendsRecommand rl = new ReturnFriendsRecommand();
		rl.seq = seq;

		if (!user.judegeProtocolInterval(this, 3)) {
			rl.result = "点击过于频繁.";
			user.send(rl);
			return;
		}


		FriendsBean fsb = user.getDao().findFriends(user.getUid());

		rl.recommands = FriendService.getInstance().randomFriends(10, user.getUid());
		for (FriendBean fb : rl.recommands) {
			fb.mode = fsb.isFriendStatus(fb.id);
		}

        user.send(rl);
    }
}
