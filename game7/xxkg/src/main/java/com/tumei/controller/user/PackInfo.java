package com.tumei.controller.user;

import com.tumei.dto.battle.EquipStruct;
import com.tumei.dto.battle.HeroStruct;
import com.tumei.model.beans.EquipBean;
import com.tumei.model.beans.HeroBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Leon on 2017/8/29 0029.
 */
public class PackInfo {
	public List<HeroStruct> heros = new ArrayList<>();
	public List<EquipStruct> equips = new ArrayList<>();
	public HashMap<Integer, Integer> items = new HashMap<>();
}
