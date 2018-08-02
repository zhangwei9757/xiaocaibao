package com.tumei.groovy.contract;

import com.tumei.common.webio.BattleResultStruct;
import com.tumei.dto.battle.HerosStruct;
import com.tumei.dto.boss.BossDto;
import com.tumei.dto.boss.BossGuildDto;
import com.tumei.dto.boss.BossRoleDto;

import java.util.List;

/**
 * Created by Leon on 2018/2/5.
 */
public interface IBossSystem {
	void schedule();

	void update();

	void conclusion();

	long bossLife();

	BattleResultStruct callFight(HerosStruct bs);

	List<BossRoleDto> getRanks();

	List<BossGuildDto> getGuildRanks();

	BossDto getInfo(long uid, String name);

	long getLife();

	void enable(boolean flag);
}
