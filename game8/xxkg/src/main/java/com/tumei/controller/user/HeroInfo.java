package com.tumei.controller.user;

import com.tumei.model.beans.HeroBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Leon on 2017/8/29 0029.
 */
public class HeroInfo {

	// 皮肤
	public int skin;

	// 拥有的皮肤以及等级
	public HashMap<Integer, Integer> skins = new HashMap<>();

	public List<HeroBean> heros = new ArrayList<>();

	public List<HeroBean> assits = new ArrayList<>();

	public int chain;

	public int chainattr;

}
