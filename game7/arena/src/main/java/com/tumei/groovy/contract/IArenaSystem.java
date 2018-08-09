package com.tumei.groovy.contract;

import com.tumei.dto.arena.*;

import java.util.List;

/**
 * Created by Leon on 2017/11/6 0006.
 */
public interface IArenaSystem {
	/**
	 * 将自身信息提交到竞技场
	 */
	void submitInfo(ArenaRoleDto ard);

	ArenaInfo getInfo(long uid);

	ArenaFightResult fight(long uid, int peerRank);

	LadderFightResult fightLadder(long uid, long pid, int group, int index);

	LadderInfoDto enterLadder(long uid);

	int chooseSlot(long uid, int slot);

	LadderHonorDto getHonor(long uid);

	List<LadderVideoDto> getVideos(long uid);

	void update();
	void schedule();
	void arenaSchedule();
}
