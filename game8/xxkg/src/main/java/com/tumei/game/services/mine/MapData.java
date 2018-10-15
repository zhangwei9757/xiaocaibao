package com.tumei.game.services.mine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/7/25 0025.
 *
 * 客户端编辑器输出的配置结构
 *
 */
public class MapData {
	/**
	 * 行
	 */
	public int rows = 95;

	/**
	 * 列
	 */
	public int columns = 121;

	/**
	 * 可能的出生点
	 */
	public List<Integer> birth = new ArrayList<>();

	/**
	 * 阻挡的路径
	 */
	public List<Integer> path = new ArrayList<>();

	/**
	 * 可能的宝箱
	 */
	public List<HexObject> treasure = new ArrayList<>();

	/**
	 * 可能的怪物
	 */
	public List<HexObject> monster = new ArrayList<>();

	/**
	 * 可能的Npc
	 */
	public List<HexObject> npc = new ArrayList<>();

	/**
	 * 可能的矿区
	 */
	public List<HexObject> building = new ArrayList<>();

	/**
	 * 传送点
	 */
	public List<HexObject> transporter = new ArrayList<>();
}
