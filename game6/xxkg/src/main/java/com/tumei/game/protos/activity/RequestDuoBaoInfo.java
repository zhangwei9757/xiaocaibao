package com.tumei.game.protos.activity;

import com.tumei.GameConfig;
import com.tumei.common.LocalService;
import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.tumei.common.utils.Defs.夺宝积分;
import static com.tumei.common.utils.Defs.夺宝魂币;

/**
 * Created by leon on 2016/12/31.
 * 请求 征战竞技
 */
@Component
public class RequestDuoBaoInfo extends BaseProtocol {
	public int seq;

	class ReturnDuoBaoInfo extends BaseProtocol {
		public int seq;

		// [1,4]当前等级
		public int level;

		// 等分在每个循环会被清空
		public int soul;
		public int score;
		// 0表示没有保护，其他时间是保护的截至时间
		public long time;
		public int couple;

		// 单冲剩余的次数
		public List<Integer> singlecount = new ArrayList<>();
		// 单冲在本周期内每个档位的次数,比如六元充值了5次，则返回次数5，客户端根据singlecount领取的次数自行判断
		public List<Integer> singlestatus = new ArrayList<>();

		public int spend = 0;
		// 本周期内，花费每个档位的领取次数
		public List<Integer> spends = new ArrayList<>();

		// 本周期内商店的每个档位购买的次数
		public List<Integer> stores = new ArrayList<>();

		// 本周期内每个积分位置的领取次数
		public List<Integer> scores = new ArrayList<>();

		// 夺宝间隔
		public int day;

		// 今天第几天[0,xxx], 之前是14
		public int dayth;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;
		ReturnDuoBaoInfo rl = new ReturnDuoBaoInfo();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		int day = ab.flushDb();

		rl.day = GameConfig.getInstance().getDbPeriod();
		rl.dayth = day;

		if (day > 2) {
			user.send(rl);
			return;
		}

		rl.time = ab.flushDbCouple();
		rl.couple = ab.getCoupleType();
		rl.level = ab.getDbLevel();

		// 单冲
		{
			rl.singlecount.addAll(ab.getDbsingles());
			rl.singlestatus.addAll(ab.getDbcharges());
		}

		// 消费
		{
			rl.spend = ab.getDbSpend();
			rl.spends.addAll(ab.getDbspends());
		}

		// 商店
		{
			rl.stores.addAll(ab.getDbstores());
		}

		// 积分
		{
			rl.scores.addAll(ab.getDbscores());
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		rl.soul = pb.getItemCount(夺宝魂币);
		rl.score = pb.getItemCount(夺宝积分);
		GameServer.getInstance().info("玩家(" + user.getUid() + ") 神秘雕像:" + rl.soul + " 积分:" + rl.score);


		user.send(rl);
	}
}
