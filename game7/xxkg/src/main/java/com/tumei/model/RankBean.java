package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.dto.battle.HeroStruct;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.ArenastoreConf;
import com.tumei.common.Readonly;
import com.tumei.common.utils.TimeUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/11 0011.
 */
@Document(collection = "Role.Ranks")
public class RankBean {
	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private Long id;
	/**
	 * 排名
	 */
	private int rank;
	/**
	 * 名字
	 */
	private String name = "";
	/**
	 * 头像
	 */
	private int icon;
	@JsonIgnore
	private int peek;

	private int level;

	private long power;

	private int fashion;

	/**
	 * 阵形
	 */
	private HeroBean[] formation = new HeroBean[6];

	/**
	 * 商店购买次数刷新日期
	 */
	@JsonIgnore
	private int flushDay;
	/**
	 * 对应的物品已经购买次数
	 */
	@JsonIgnore
	private Map<Integer, Integer> storeLimits = new HashMap<>();

	public RankBean() { }
	public RankBean(long _id, int _rank) {
		id = _id;
		rank = _rank;
		peek = _rank;
	}

	public RankBean(RankBean other) {
		id = other.id;
		rank = other.rank;
		name = other.name;
		icon = other.icon;
		formation = other.formation;
		peek = other.peek;
		level = other.level;
		power = other.power;
	}

	/**
	 * 刷新 排行玩家的购买次数限制
	 */
	public boolean flush() {
		int today = TimeUtil.getToday();
		if (flushDay != today) {
			List<ArenastoreConf> store = Readonly.getInstance().findArenaStores();
			for (int key : storeLimits.keySet()) {
				ArenastoreConf asc = store.get(key - 1);
				if (asc.clear == 1) {
					storeLimits.put(key, 0);
				}
			}

			flushDay = today;
			return true;
		}
		return false;
	}

	/**
	 * 战斗的时候填充这个FightStruct
	 * @param _fs
	 */
	public void fillHeros(HeroStruct[] _fs) {
		for (int i = 0; i < 6; ++i) {
			HeroBean hb = formation[i];
			if (hb != null) {
				HeroStruct hs = hb.createHeroStruct();
				_fs[i] = hs;
			} else {
				_fs[i] = null;
			}
		}
	}

	/**
	 * 增加商店购买次数
	 * @param key
	 * @param val
	 */
	public void addStoreLimit(int key, int val) {
		storeLimits.put(key, storeLimits.getOrDefault(key, 0) + val);
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public HeroBean[] getFormation() {
		return formation;
	}

	public void setFormation(HeroBean[] formation) {
		this.formation = formation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPeek() {
		return peek;
	}

	public void setPeek(int peek) {
		this.peek = peek;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getPower() {
		return power;
	}

	public void setPower(long power) {
		this.power = power;
	}

	public int getFlushDay() {
		return flushDay;
	}

	public void setFlushDay(int flushDay) {
		this.flushDay = flushDay;
	}

	public Map<Integer, Integer> getStoreLimits() {
		return storeLimits;
	}

	public void setStoreLimits(Map<Integer, Integer> storeLimits) {
		this.storeLimits = storeLimits;
	}

	public int getFashion() {
		return fashion;
	}

	public void setFashion(int fashion) {
		this.fashion = fashion;
	}
}
