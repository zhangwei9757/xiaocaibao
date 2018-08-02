package com.tumei.game.services.mine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/7/25 0025.
 */
public class Hex {
	public int q;
	public int r;
	public int s;

	public static List<Hex> directions;

	static {
		directions = new ArrayList<Hex>();
		directions.add(new Hex(1, 0));
		directions.add(new Hex(1, -1));
		directions.add(new Hex(0, -1));
		directions.add(new Hex(-1, 0));
		directions.add(new Hex(-1, 1));
		directions.add(new Hex(0, 1));
	}

	public Hex(int q, int r)
	{
		this.q = q;
		this.r = r;
		this.s = -(q + r);
	}

	public int ID() {
		return r * 1000 + q;
	}

	public Hex add(Hex other) {
		return new Hex(q + other.q, r + other.r);
	}

	public Hex substract(Hex other) {
		return new Hex(q - other.q, r - other.r);
	}

	/**
	 * 返回指定的方向上邻居，方向0为右边，顺时针旋转
	 * @param direction
	 * @return
	 */
	public Hex neighbour(int direction) {
		return add(Hex.directions.get(direction));
	}

	public int length() {
		return (int)((Math.abs(q) + Math.abs(r) + Math.abs(s)) / 2);
	}

	public int distance(Hex other) {
		return substract(other).length();
	}
}
