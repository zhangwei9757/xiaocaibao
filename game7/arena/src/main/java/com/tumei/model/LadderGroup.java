package com.tumei.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/11/17 0017.
 */
@Data
public class LadderGroup {
	// 分组 [王者，钻石，铂金，黄金，白银，青铜] 分别为0,1,2,3,4,5,6
	private int group;

	// 本组内的人员情况  如果roles小于10000 表示是机器人，直接到机器人表里查询即可
	private List<Long> roles = new ArrayList<>();
}
