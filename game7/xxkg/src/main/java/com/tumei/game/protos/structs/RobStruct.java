package com.tumei.game.protos.structs;

import com.tumei.dto.battle.HeroStruct;
import com.tumei.dto.battle.HerosStruct;
import com.tumei.model.RoleBean;
import com.tumei.common.Readonly;
import com.tumei.model.beans.HeroBean;
import com.tumei.model.beans.RelicBean;
import com.tumei.modelconf.TresackerConf;
import com.tumei.common.utils.RandomUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/11 0011.
 *
 * 碎片可抢劫的玩家信息
 *
 */
public class RobStruct {
	private String name = "";
	private int icon;
	private int level;
	private int grade = 1;
	// 是否npc， 0表示是npc，其他表示是玩家的真实id
	private long uid;
	// 抢夺概率
	private int ratio;

	private List<FightRoleStruct> heros = new ArrayList<>();

	public RobStruct() {}

	/**
	 * 创建角色
	 * @param _lord  抢劫者
	 * @param rb 构造的角色基础
	 */
	public RobStruct(long _lord, int quality, RoleBean rb) {
		uid = _lord;

		if (_lord == 0) { // 抢劫机器人
			name = Readonly.getInstance().randomName();

			// _level 可能小于15级，那么不可能选择佐佑15级的其他贝抢劫者，所以这里根据传入等级进行佐佑选择
			int mod = rb.getLevel();
			if (mod > 15) {
				mod = 15;
			}
			level = rb.getLevel() + (RandomUtil.getRandom() % (2*mod)) - mod;

			TresackerConf tc = Readonly.getInstance().findTresacker();

			ratio = tc.rate[5 - quality][1];
			heros.add(new FightRoleStruct(90010 + (RandomUtil.getRandom() % 4) * 10, 0));
			List<Integer> hs = Readonly.getInstance().randHeros(5);
			for (int h : hs) {
				heros.add(new FightRoleStruct(h, 0));
			}
			grade = 1;
		} else {
			name = rb.getNickname();
			level = rb.getLevel();
			grade = rb.getGrade();
		}
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	public List<FightRoleStruct> getHeros() {
		return heros;
	}

	public void setHeros(List<FightRoleStruct> heros) {
		this.heros = heros;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}
}
