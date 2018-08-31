package com.tumei.game.protos.activity;

import com.tumei.GameConfig;
import com.tumei.common.LocalService;
import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.SingleStruct;
import com.tumei.model.ActivityBean;
import com.tumei.model.ChargeBean;
import com.tumei.model.beans.ChargeDayBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.SinglerechargeConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by leon on 2016/12/31.
 * 请求单冲信息
 *
 */
@Component
public class RequestSingleInfo extends BaseProtocol {
    public int seq;

    class ReturnSingleInfo extends BaseProtocol {
		public int seq;
		// 本周期 配置表起始点[0,...
		public int begin;
		// 本周起 配置表结束点[0,...
		public int end;
		public int current;
		// 本次周期时间
		public int day;
		public List<SingleStruct> status = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnSingleInfo rl = new ReturnSingleInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushSingle();

		rl.status = ab.getSingleChargeAwards();

		LocalService r = LocalService.getInstance();
		rl.begin = r.getSingleBeginIdx();
		rl.end = r.getSingleEndIdx();
		rl.day = GameConfig.getInstance().getSinglePeriod();
		rl.current = r.getSingleCur() % rl.day;

        user.send(rl);
    }
}
