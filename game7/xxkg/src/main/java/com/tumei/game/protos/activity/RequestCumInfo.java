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
 * 请求累计充值信息
 *
 */
@Component
public class RequestCumInfo extends BaseProtocol {
    public int seq;

    class ReturnCumInfo extends BaseProtocol {
		public int seq;
		// 本周期 配置表起始点[0,...
		public int begin;
		// 本周起 配置表结束点[0,...
		public int end;
		public int current;
		public int day;
		// 对应的档位的充值状态, -1:满足， -2:已经领取, 其他值表示充值金额
		public List<Integer> status = new ArrayList<>();

		// 各个档位的奖励内容，客户端不要再查config表了。
		public List<int[]> list = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnCumInfo rl = new ReturnCumInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushCum();

		LocalService r = LocalService.getInstance();
		rl.status = ab.getCumChargeAwards();
		rl.begin = r.getCumBeginIdx();
		rl.end = r.getCumEndIdx();
		rl.day = GameConfig.getInstance().getCumPeriod();
		rl.current = r.getCumCur() % rl.day;
		rl.list = ab.getCumContents();

        user.send(rl);
    }
}
