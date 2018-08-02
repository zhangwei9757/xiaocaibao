package com.tumei.game.protos.misc;

import com.tumei.game.GameUser;
import com.tumei.websocket.WebSocketUser;
import com.tumei.model.SummonBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级英雄
 */
@Component
public class RequestSummonInfo extends BaseProtocol {
	public int seq;

	class ReturnSummonInfo extends BaseProtocol {
		public int seq;
		/**
		 * 上次使用免费召唤的时间
		 */
		public long lastSmallFree;
		/**
		 * 剩余普通召唤的免费次数
		 */
		public int smallFreeCount;
		/**
		 * 上次使用免费中等召唤的时间
		 */
		public long lastMiddleFree;
		/**
		 * 已经进行的中等召唤次数(没有计算使用传奇卡), 每日首次使用钻石打折
		 */
		public int middleCount;
		/**
		 * 中级 总抽奖
		 */
		public int middleTotal;
		/**
		 * 高级召唤剩余免费次数
		 */
		public int advanceFreeCount;
		/**
		 * 高级召唤的碎片池
		 */
		public int advanceIndex;
		/**
		 * 当前的幸运值
		 */
		public int lucky;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnSummonInfo rsi = new ReturnSummonInfo();
		rsi.seq = seq;

		SummonBean sb = user.getDao().findSummon(user.getUid());
		sb.flushSummon(user);

		rsi.smallFreeCount = sb.getSmallFreeCount();
		rsi.lastSmallFree = sb.getLastSmallFree();
		/**
		 * 今日已经使用的次数(不包含使用传奇卡次数)
		 */
		rsi.middleCount = sb.getTodayCount();
		rsi.middleTotal = sb.getMiddleCount();
		rsi.lastMiddleFree = sb.getLastMiddleFree();

		rsi.advanceFreeCount = sb.getAdvanceFreeCount();
		rsi.advanceIndex = sb.getAdvanceIndex();

		rsi.lucky = sb.getLucky();

		user.send(rsi);
	}

}
