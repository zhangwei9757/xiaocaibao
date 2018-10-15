package com.tumei.common.group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/5/10 0010.
 * <p>
 * 群组信息
 */
public class GroupSceneStruct {
	public int scene;
	public int[] progress = new int[4];
	public List<GroupSceneRoleStruct> roles = new ArrayList<>();
	public String result = "";
}
