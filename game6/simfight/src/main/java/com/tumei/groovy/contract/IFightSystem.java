package com.tumei.groovy.contract;

import com.tumei.common.fight.*;
import java.util.List;

/**
 * Created by Leon on 2017/9/1 0001.
 */
public interface IFightSystem {

	long calc_power(HerosStruct herosBean);

	FightResult doSceneBattle(HerosStruct herosBean, List<DirectHeroStruct> enemy, int condition, boolean isBoss, int relic, int star, int legend, int level);

	FightResult doBattle(HerosStruct herosBean, HerosStruct other);

	FightResult doBattle(HerosStruct herosBean, HerosStruct other, int weak);
}
