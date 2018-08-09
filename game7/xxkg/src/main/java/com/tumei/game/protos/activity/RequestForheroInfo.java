package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.ChargeBean;
import com.tumei.model.RoleBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by leon on 2016/12/31.
 * 请求
 *
 */
@Component
public class RequestForheroInfo extends BaseProtocol {
    public int seq;

    class ReturnForheroInfo extends BaseProtocol {
		public int seq;
		// 0未购买 1已购买
		public int used;
		// 角色登录的天数
		public int day;
		public HashMap<Integer, Integer> levels = new HashMap<>();
		public HashMap<Integer, Integer> logons = new HashMap<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnForheroInfo rl = new ReturnForheroInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flush();

		RoleBean rb = user.getDao().findRole(user.getUid());
		rl.day = rb.getLogdays();
		rl.logons = ab.getLogoFuli();
		rl.levels = ab.getLevelFuli();
		rl.used = ab.getHeroFuli();

        user.send(rl);
    }
}
