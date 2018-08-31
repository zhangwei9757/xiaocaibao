package com.tumei.game.protos.mine;

import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
@Component
public class RequestMineAction extends BaseProtocol {
	public int seq;
	public int dir;
	/**
	 * 1: 开宝箱
	 * 2: 打怪物
	 * 3: 商人
	 * 4: 矿
	 */
	public int mode;

	/**
	 * 当操作矿的时候，需要告诉看到的矿的等级
	 */
	public int level;

	public static class Return extends BaseProtocol {
		public int seq;
		public String result = "";

		/**
		 * 战报
		 */
		public String data = "";

		/**
		 * win = 1 表示本次挑战胜利
		 */
		public int win;

		public List<AwardBean> awards = new ArrayList<>();
	}
}
