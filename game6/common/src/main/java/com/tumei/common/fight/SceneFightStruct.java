package com.tumei.common.fight;

import java.util.List;

/**
 * Created by Administrator on 2017/2/20 0020.
 * <p>
 * 战斗信息
 */
public class SceneFightStruct {
    public HerosStruct hss;

    public List<DirectHeroStruct> right;

    // 胜利条件
    public int condition = 0;
    public boolean isBoss;
    public int relic;
    public int star;
    public int legend;
    public int level;
}
