package com.tumei.game.protos.activity;

import com.tumei.GameConfig;
import com.tumei.game.services.LocalService;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 * 请求 半价限购
 *
 */
@Component
public class RequestSaleInfo extends BaseProtocol {
    public int seq;
	/**
	 * 0:半价限购
	 * 1:折扣兑换
	 */
	public int mode;

    class ReturnSaleInfo extends BaseProtocol {
		public int seq;
		// 本周期 配置表起始点[0,...
		public int begin;
		// 本周起 配置表结束点[0,...
		public int end;
		public int current;
		public int day;
		// 对应的购买次数
		public List<Integer> status = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnSaleInfo rl = new ReturnSaleInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushDc();

		LocalService r = LocalService.getInstance();
		rl.current = r.getDcCur() % GameConfig.getInstance().getSalePeriod();
		if (mode == 0) {
			rl.begin = r.getDcBeginIdx();
			rl.end = r.getDcEndIdx();
			rl.status = ab.getDcStatus();
		} else {
			rl.begin = r.getEcBeginIdx();
			rl.end = r.getEcEndIdx();
			rl.status = ab.getEcStatus();
		}

		rl.day = GameConfig.getInstance().getSalePeriod();

        user.send(rl);
    }
}
