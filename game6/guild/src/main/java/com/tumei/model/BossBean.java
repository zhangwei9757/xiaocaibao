package com.tumei.model;

import com.tumei.common.fight.DirectHeroStruct;
import com.tumei.modelconf.BossConf;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by Leon on 2017/11/6 0006.
 */
@Document(collection = "BossInfo")
public class BossBean {
	private String id;

	/**
	 * 日期标识每天的boss状态
	 */
	@Indexed(unique = true)
	private int day;

	/**
	 * 当前怪物的等级
	 */
	private int level = 1;

	/**
	 * 六个boss怪物对应的血量
	 */
	private DirectHeroStruct[] peers = new DirectHeroStruct[6];

	// 多少秒将之杀死
	private int killTime;

	private int sendAwards;

	private String killer;

	public int getSendAwards() {
		return sendAwards;
	}

	public void setSendAwards(int sendAwards) {
		this.sendAwards = sendAwards;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public DirectHeroStruct[] getPeers() {
		return peers;
	}

	public void setPeers(DirectHeroStruct[] peers) {
		this.peers = peers;
	}

	/**
	 * boss是否死亡
	 * @return
	 */
	public boolean isDead() {
		for (int i = 0; i < 6; ++i) {
			if (peers[i] != null && peers[i].life > 0) {
				return false;
			}
		}
		return true;
	}

	public long life() {
		long rtn = 0;
		for (int i = 0; i < 6; ++i) {
			if (peers[i] != null) {
				rtn += peers[i].life;
			}
		}
		return rtn;
	}

	/**
	 * 创建对战角色
	 */
	public void createPeers(BossConf rc) {
		int i = 0;
		for (int h : rc.guard) {
			if (h != 0) {
				DirectHeroStruct shs = new DirectHeroStruct();
				shs.hero = h;
				shs.life = rc.hp;
				shs.attack = rc.attack;
				shs.def = rc.defence1;
				shs.mdef = rc.defence2;
				shs.critical = rc.crit;
				shs.aim = rc.hit;
				shs.antiCrit = rc.critoff;
				shs.dodge = rc.dog;
				shs.enHarm = rc.increase;
				shs.overHarm = -rc.reduce;
				peers[i] = shs;
			}
			else {
				peers[i] = null;
			}
			++i;
		}
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getKillTime() {
		return killTime;
	}

	public void setKillTime(int killTime) {
		this.killTime = killTime;
	}

	public String getKiller() {
		return killer;
	}

	public void setKiller(String killer) {
		this.killer = killer;
	}
}

