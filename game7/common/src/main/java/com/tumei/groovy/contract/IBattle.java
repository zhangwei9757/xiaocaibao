package com.tumei.groovy.contract;

import com.tumei.dto.battle.DirectHeroStruct;
import com.tumei.dto.battle.FightResult;
import com.tumei.dto.battle.HerosStruct;

import java.util.List;

/**
 * Created by Leon on 2018/3/8.
 */
public interface IBattle {

	long calc_power(HerosStruct herosBean);

	FightResult doSceneBattle(HerosStruct herosBean, List<DirectHeroStruct> enemy, int condition, boolean isBoss, int relic, int star, int legend, int level);

	FightResult doBattle(HerosStruct herosBean, HerosStruct other);

	FightResult doBattle(HerosStruct herosBean, HerosStruct other, int weak);
}
