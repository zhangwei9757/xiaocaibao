package com.tumei.dto.arena;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/11/20 0020.
 */
public class LadderInfoDto {
	// 排名[0,...]
	public int rank;
	// 所属的分区，-1表示没有分区，需要重新选择
	public int slot;
	// 最大的slot个数，玩家即可知道选择哪个
	public int maxSlot;
	// 当前所有的荣誉
	public int honor;
	// 下次获取荣誉的时间，1970秒
	public long next;

	// 具体当前分组的情况
	public int group;
	// 分组内的序号
	public int gindex;

	public List<LadderSimpleDto> roles = new ArrayList<>();
}
