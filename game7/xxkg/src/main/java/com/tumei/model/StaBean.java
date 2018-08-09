package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.GameConfig;
import com.tumei.common.Readonly;
import com.tumei.modelconf.HeroConf;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by leon on 2016/11/5.
 * <p>
 * 角色统计，详细记录角色的个人成就与信息
 */
@Document(collection = "Role.Sta")
public class StaBean {
	@JsonIgnore
	@Id
	private String ObjectId;

	@JsonIgnore
	@Field("id")
	private Long id;

	// 总共获取的金币个数
	private long gold;
	// 总共获取的钻石个数
	private int gem;

	// 总共消费的金币
	private long goldFee;
	// 总共消费的钻石
	private int gemFee;

	// 所有英雄拥有最高个数
	private int heroCount;
	// 紫色英雄拥有最高个数
	private int hcPurple;
	// 橙色英雄拥有最高个数
	private int hcGold;
	// 红色英雄拥有最高个数
	private int hcRed;

	// 合成宝物次数
	private int composeTreasure;
	private int ctPurple;
	private int ctGold;

	private int heroStoreFlush;
	private int heroStoreBuy;

	public void fixGold(long _gold) {
		if (_gold > 0) {
			this.gold += _gold;
		}
		else if (_gold < 0) {
			this.goldFee -= _gold;
		}
	}

	public void fixGem(long _gem) {
		if (_gem > 0) {
			this.gem += _gem;
		}
		else if (_gem < 0) {
			this.gemFee -= _gem;
		}
	}

	public void addHero(int id) {
		HeroConf hc = Readonly.getInstance().findHero(id);
		if (hc != null) {
			++this.heroCount;
			switch (hc.quality) {
				case 3:
					++this.hcPurple;
					break;
				case 4:
					++this.hcGold;
					break;
				case 5:
					++this.hcRed;
					break;
			}
		}
	}

	public String getObjectId() {
		return ObjectId;
	}

	public void setObjectId(String objectId) {
		ObjectId = objectId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getGold() {
		return gold;
	}

	public void setGold(long gold) {
		this.gold = gold;
	}

	public int getGem() {
		return gem;
	}

	public void setGem(int gem) {
		this.gem = gem;
	}

	public int getHeroCount() {
		return heroCount;
	}

	public void setHeroCount(int heroCount) {
		this.heroCount = heroCount;
	}

	public int getHcPurple() {
		return hcPurple;
	}

	public void setHcPurple(int hcPurple) {
		this.hcPurple = hcPurple;
	}

	public int getHcGold() {
		return hcGold;
	}

	public void setHcGold(int hcGold) {
		this.hcGold = hcGold;
	}

	public int getHcRed() {
		return hcRed;
	}

	public void setHcRed(int hcRed) {
		this.hcRed = hcRed;
	}

	public long getGoldFee() {
		return goldFee;
	}

	public void setGoldFee(int goldFee) {
		this.goldFee = goldFee;
	}

	public int getGemFee() {
		return gemFee;
	}

	public void setGemFee(int gemFee) {
		this.gemFee = gemFee;
	}

	public StaBean() {
	}

	public StaBean(long _id) {
		id = _id;
	}

	public void incComposeTreasure(int quality) {
		++composeTreasure;
		if (quality >= 3) {
			++ctPurple;
		}
		if (quality >= 4) {
			++ctGold;
		}
	}

	public void incHeroStoreFlush() {
		++heroStoreFlush;
	}

	public void incHeroStoreBuy(int count) {
		heroStoreBuy += count;
	}

	public int getComposeTreasure() {
		return composeTreasure;
	}

	public void setComposeTreasure(int composeTreasure) {
		this.composeTreasure = composeTreasure;
	}

	public int getCtPurple() {
		return ctPurple;
	}

	public void setCtPurple(int ctPurple) {
		this.ctPurple = ctPurple;
	}

	public int getCtGold() {
		return ctGold;
	}

	public void setCtGold(int ctGold) {
		this.ctGold = ctGold;
	}

	public int getHeroStoreFlush() {
		return heroStoreFlush;
	}

	public void setHeroStoreFlush(int heroStoreFlush) {
		this.heroStoreFlush = heroStoreFlush;
	}

	public int getHeroStoreBuy() {
		return heroStoreBuy;
	}

	public void setHeroStoreBuy(int heroStoreBuy) {
		this.heroStoreBuy = heroStoreBuy;
	}

	public void setGoldFee(long goldFee) {
		this.goldFee = goldFee;
	}
}
