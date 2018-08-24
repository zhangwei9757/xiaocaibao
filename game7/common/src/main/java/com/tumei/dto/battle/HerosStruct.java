package com.tumei.dto.battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Leon on 2018/3/16.
 */
public class HerosStruct {
	public long uid;

	public HeroStruct[] heros = new HeroStruct[6];

	public int[] assists = new int[6];

	// 全部buff
	public HashMap<Integer, Integer> buffs = new HashMap<>();

	public int fake = 0;

	public int skin = 0;

	/**
	 * 当前拥有的时装与对应的等级
	 */
	public HashMap<Integer, Integer> skins = new HashMap<>();

	public List<RelicStruct> relics = new ArrayList<>();

	public int[] lineups = new int[6];

	public HerosStruct clone() {
		HerosStruct hss = new HerosStruct();

		hss.uid = this.uid;
		hss.fake = this.fake;
		hss.skin = this.skin;
		hss.skins.putAll(this.skins);
		hss.buffs.putAll(this.buffs);

		hss.relics.addAll(relics);

		for (int i = 0; i < 6; ++i) {
			HeroStruct hb = heros[i];
			if (hb != null) {
				hss.heros[i] = new HeroStruct(hb);
			} else {
				hss.heros[i] = null;
			}
			hss.assists[i] = this.assists[i];
		}

		return hss;
	}
}
