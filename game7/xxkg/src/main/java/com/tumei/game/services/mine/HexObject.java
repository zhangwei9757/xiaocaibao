package com.tumei.game.services.mine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/7/25 0025.
 */
public class HexObject {
	public int id;
	public int pos;
	public int level;
	public List<Integer> relative = new ArrayList<>();
}
