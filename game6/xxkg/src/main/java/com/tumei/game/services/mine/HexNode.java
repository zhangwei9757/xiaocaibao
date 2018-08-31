package com.tumei.game.services.mine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Leon on 2017/7/25 0025.
 *
 * 六边形网格计算器
 *
 */
public class HexNode {
	public int id;

	public Hex hex;

	public int row;

	public int col;

	/**
	 * 格子中的玩家
	 * key 是玩家id
	 * value 是玩家当前的状态
	 * 1  表示只接收
	 * 2: 表示只发送
	 * 3: 表示接收且发送
	 */
	private HashMap<Long, Integer> roles = new HashMap<>();

	public HexNode() {}
	public HexNode(int _q, int _r) {
		hex = new Hex(_q, _r);
		id = hex.ID();
		row = _r;
		col = _q + _r / 2;
	}

	public String dumpPlayers() {
		List<String> msgs = new ArrayList<String>();
		roles.forEach((k, v) -> {
			msgs.add("[" + k + "," + v + "]");
		});
		return String.join(" ", msgs);
	}

	/**
	 *
	 * @param _id
	 * @param _status
	 */
	public void addRole(long _id, int _status, boolean _override) {
		if (!_override) {
			int old = roles.getOrDefault(_id, 0);
			roles.put(_id, _status | old);
		} else {
			roles.put(_id, _status);
		}
	}

	public void removeRole(long _id) {
		roles.remove(_id);
	}

	public HashMap<Long, Integer> getRoles() {
		return roles;
	}

	@Override
	public String toString() {
		return "坐标(" + row + "," + col + ")";
	}
}
