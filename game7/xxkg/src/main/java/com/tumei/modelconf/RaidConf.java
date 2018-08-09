package com.tumei.modelconf;

import com.tumei.common.Readonly;
import com.tumei.dto.battle.DirectHeroStruct;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Raid")
public class RaidConf {
	@Id
	public String objectId;

	public int key;
	/**
	 * 推荐战斗力
	 */
	public long power;
	/**
	 * 挑战奖励1 - 5
	 */
	public int[] reward1;
	public int[] reward2;
	public int[] reward3;
	public int[] reward4;
	public int[] reward5;

	/**
	 * 挂机金币
	 */
	public int gold;
	/**
	 * 挂机经验
	 */
	public int exp;
	/**
	 * 挑战消耗: 挑战一次消耗的能量
	 */
	public int cost;
	/**
	 * 奖励间隔
	 */
	public int cd;
	/**
	 * 关卡宝箱
	 */
	public int[] chest;
	/**
	 * 事件掉落
	 */
	public int[] drop;
	/**
	 * 扫荡出货机率
	 */
	public int addrate;
	/**
	 * 扫荡额外掉落
	 */
	public int[] sweep;
	/**
	 * 关卡代表
	 */
	public int boss;

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

	public int condition;
	public int[] glory;

	/**
	 * 副本数据填充
	 */
	public List<DirectHeroStruct> makeSceneBattle(int ratio) {
		List<DirectHeroStruct> rtn = new ArrayList<>();
		for (int h : guard) {
			if (h != 0) {
//				ratio = details[i];

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
		}
		return rtn;
	}

	/**
	 * 矿区内单人战斗，副本数据填充, 但是英雄选择随机，仅固定传入一个英雄
	 */
	public List<DirectHeroStruct> makeMineBattle(int mainHero) {
		List<DirectHeroStruct> rtn = new ArrayList<>();
		List<Integer> heros = Readonly.getInstance().randHeros(guard.length);
		heros.set(0, mainHero);

		int i = 0;
		for (int h : heros) {
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
