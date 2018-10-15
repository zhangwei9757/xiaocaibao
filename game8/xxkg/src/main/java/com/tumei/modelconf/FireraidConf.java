package com.tumei.modelconf;

import com.tumei.dto.battle.DirectHeroStruct;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Fireraid")
public class FireraidConf {
	@Id
	public String objectId;

	public int key;
	public int chapter;
	public int stage;
	public int condition;
	public int[][] buff;
	public int[][] krewards;
	public int[][] crewards;
	public int chief;
	/**
	 * 以下为六个站位的守卫:
	 *
	 * details:生命 攻击 物防 法防 暴击 命中 抗暴 闪避 的比例
	 *
	 */
	public int[] guard;
	public int[] details;

	public long hp;
	public int attack;
	public int defence1;
	public int defence2;
	public int crit;
	public int hit;
	public int critoff;
	public int dog;
	public int increase;
	public int reduce;

	public int[] glory;

	/**
	 * 远征填充
	 */
	public List<DirectHeroStruct> makeFireRaid() {
		List<DirectHeroStruct> rtn = new ArrayList<>();
		int i = 0;
		for (int h : guard) {
			if (h != 0) {
				int ratio = details[i];
				DirectHeroStruct shs = new DirectHeroStruct();
				shs.hero = h;
				shs.life = (long)(hp * (ratio / 100.0));
				shs.attack = (int)(attack * (ratio / 100.0));
				shs.def = (int)(defence1 * (ratio / 100.0));
				shs.mdef = (int)(defence2 * (ratio / 100.0));
				shs.critical = (int)(crit * (ratio / 100.0));
				shs.aim = (int)(hit * (ratio / 100.0));
				shs.antiCrit = (int)(critoff * (ratio / 100.0));
				shs.dodge = (int)(dog * (ratio / 100.0));
				shs.enHarm = (int)(increase * (ratio / 100.0));
				shs.overHarm = (int)(reduce * (ratio / 100.0));
				rtn.add(shs);
			} else {
				rtn.add(null);
			}
			++i;
		}
		return rtn;
	}

}
