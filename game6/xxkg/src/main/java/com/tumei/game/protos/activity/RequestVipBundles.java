package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 *
 */
@Component
public class RequestVipBundles extends BaseProtocol {
    public int seq;

    class ReturnVipBundles extends BaseProtocol {
		public int seq;

		public List<Integer> bundles = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnVipBundles rl = new ReturnVipBundles();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());

		Arrays.stream(ab.getVipBundles()).forEach(i -> rl.bundles.add(i));

        user.send(rl);
    }
}
