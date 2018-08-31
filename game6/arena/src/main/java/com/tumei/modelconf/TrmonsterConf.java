package com.tumei.modelconf;

import com.tumei.common.fight.DirectHeroStruct;
import com.tumei.common.fight.SceneFightStruct;
import com.tumei.dto.arena.LadderSimpleDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Trmonsters")
public class TrmonsterConf {
	@Id
	public String id;

	public int key;
	
	public String name;
	
	public long power;

	public int[] guard;

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

	public LadderSimpleDto createSimpleDto() {
		LadderSimpleDto dto = new LadderSimpleDto();
		dto.name = name;
		dto.icon = guard[0];
		dto.power = power;
		dto.uid = key;
		return dto;
	}

	public void fill(SceneFightStruct sfs) {
		List<DirectHeroStruct> dhss = sfs.getRight();
		for (int h : guard) {
			DirectHeroStruct shs = new DirectHeroStruct();
			shs.hero = h;
			shs.life = hp;
			shs.attack = attack;
			shs.def = defence1;
			shs.mdef = defence2;
			shs.critical = crit;
			shs.aim = hit;
			shs.antiCrit = critoff;
			shs.dodge = dog;
			shs.enHarm = increase;
			shs.overHarm = reduce;
			dhss.add(shs);
		}
	}
}
