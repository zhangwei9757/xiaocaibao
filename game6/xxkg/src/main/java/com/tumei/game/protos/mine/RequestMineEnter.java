package com.tumei.game.protos.mine;

import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
@Component
public class RequestMineEnter extends BaseProtocol {
	public int seq;

	public static class Return extends BaseProtocol {
		public int seq;

		public String result = "";

		/**
		 * 进入矿区所在的位置, 占领的矿，直接发送到矿区坐标，
		 * 否则服务器随机找个出生点
		 */
		public int pos;

		public int occupy;

		/**
		 * 行动能量
		 */
		public int energy;

		public int count;

		public int attack;


		public int key;

		public MineRobInfo info;

		/**
		 * 待收割的物品
		 */
		public List<Integer> awards = new ArrayList<>();

		/**
		 * 矿区重置个人地图信息:
		 *
		 * 这个是一个日期数字，客户端需要在进入矿区的时候记录这个数字，并比较上次的数字，发现不同了进行刷新
		 *
		 */
		public int refresh;
	}
}
