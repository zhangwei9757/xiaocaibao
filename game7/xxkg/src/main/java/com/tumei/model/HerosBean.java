package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.centermodel.ReceiptBean;
import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.dto.battle.HerosStruct;
import com.tumei.game.GameServer;
import com.tumei.game.protos.structs.SkinStruct;
import com.tumei.model.beans.ArtifactBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.model.beans.RelicBean;
import com.tumei.modelconf.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by leon on 2016/11/5.
 * <p>
 * 全体英雄结构
 */
@Document(collection = "Role.Heros")
public class HerosBean {
	@JsonIgnore
	@Id
	private String objectId;

	@Field("id")
	private long id; //

	// 战斗力
	private long power;

	// 记录的当前皮肤对应的领主模拟的英雄
	private int fakeHero;

	/**
	 * 战斗队伍
	 */
	private HeroBean[] heros = new HeroBean[6];

	/**
	 * 援军
	 */
	private HeroBean[] assists = new HeroBean[6];

	/**
	 * 使用的时装
	 */
	private int skin = 0;

	/**
	 * 当前拥有的时装
	 */
	private HashMap<Integer, SkinStruct> skins = new HashMap<>();

	/**
	 * 激活的时装套装与对应的等级
	 */
	@JsonIgnore
	private HashMap<Integer, Integer> skinsuit = new HashMap<>();

	/**
	 * 领主品质
	 */
	private int chain = 1; // 1-15
	/**
	 * 领主当前品质下的小进阶
	 */
	private int chainattr = 0; // 0-4 满5进1

	/**
	 * 战斗中需要提升的属性
	 * <p>
	 * 影响原因:
	 * 1. 战神像提升
	 * 2. 皮肤获得与进阶
	 */
	private HashMap<Integer, Integer> buffs = new HashMap<>();

	// 六个站位当前，阵形的等级，默认是0
	private int[] lineups = new int[6];

	/**
	 * 圣物列表
	 */
	private HashMap<Integer, RelicBean> relics = new HashMap<>();

	/**
	 * 激活的圣物
	 */
	private int relicid;

	/**
	 * 转换选择的英雄
	 */
	private List<Integer> choise = new ArrayList<>();

	public static HerosBean createNewHeros(long id) {
		HerosBean hb = new HerosBean();
		hb.setId(id);
		return hb;
	}

	/**
	 * 是否已经拥有该时装
	 *
	 * @param id
	 * @return
	 */
	public boolean hasSkin(int id) {
		return skins.containsKey(id);
	}

	public void addSkin(int id, String reason) {
		SkinStruct ss = new SkinStruct();
		ss.id = id;
		skins.put(id, ss);

		GameServer.getInstance().pushSta(id, "addskin|" + id + "|" + 1 + "|" + reason);

		// 增加皮肤改变套装属性加成
		checkSkinsuits();
	}

	public String getObjectId() {
							  return objectId;
											  }

	public void setObjectId(String objectId) {
										   this.objectId = objectId;
																	}

	public long getId() {
					  return id;
								}

	public void setId(long id) {
							 this.id = id;
										  }

	public HeroBean[] getHeros() {
							   return heros;
											}

	public void setHeros(HeroBean[] heros) {
										 this.heros = heros;
															}

	public HeroBean[] getAssists() {
								 return assists;
												}

	public void setAssists(HeroBean[] assists) {
											 this.assists = assists;
																	}

	public int getChain() {
						return chain;
									 }

	public void setChain(int chain) {
								  this.chain = chain;
													 }

	public int getChainattr() {
							return chainattr;
											 }

	public void setChainattr(int chainattr) {
										  this.chainattr = chainattr;
																	 }

	public HashMap<Integer, SkinStruct> getSkins() {
												 return skins;
															  }

	public void setSkins(HashMap<Integer, SkinStruct> skins) {
														   this.skins = skins;
																			  }

	public int getSkin() {
		return skin;
	}



	/**
	 * 修改皮肤，同时会影响真实记录的英雄
	 * @param skin
	 */
	public void setSkin(int skin) {
		this.skin = skin;

		if (skin != 0) {
			MaskConf mc = Readonly.getInstance().findMask(skin);
			if (mc != null) {
				fakeHero = mc.hero;
			}
		}
		else {
			fakeHero = 0;
		}
	}

	/**
	 * 增加新帐号的初始英雄, 该英雄一定存在
	 *
	 * @param hero
	 */
	public HeroBean addFirstHero(int hero) {
		HeroBean hb = new HeroBean();
		EquipBean[] ebs = hb.getEquips();

		PackBean pb = DaoGame.getInstance().findPack(this.id);

		ebs[0] = pb.buildEquip(10010);
		ebs[1] = pb.buildEquip(10020);
		ebs[2] = pb.buildEquip(10030);
		ebs[3] = pb.buildEquip(10040);

		hb.setId(hero);
		hb.setHid(1);
		heros[0] = hb;
		return hb;
	}

	public List<Integer> getChoise() {
								   return choise;
												 }

	public void setChoise(List<Integer> choise) {
											  this.choise = choise;
																   }

	public int[] getLineups() {
		return lineups;
	}

	public void setLineups(int[] lineups) {
										this.lineups = lineups;
															   }

	public HashMap<Integer, Integer> getSkinsuit() {
												 return skinsuit;
																 }

	public void setSkinsuit(HashMap<Integer, Integer> skinsuit) {
															  this.skinsuit = skinsuit;
																					   }

	/**
	 * 检查时装套装激活状态
	 */
	public void checkSkinsuits() {
		List<MasksuitConf> suits = Readonly.getInstance().findMasksuits();
		for (MasksuitConf suit : suits) {
			int min = 999;
			for (int s : suit.cost) {
				SkinStruct ss = skins.get(s);
				if (ss == null) {
					min = -1;
					break;
				}
				// 找到套装中时装强化等级最小的，套装加成按照最小值来计算
				if (ss.level < min) {
					min = ss.level;
				}
			}

			if (min > 0) { // 激活
				skinsuit.put(suit.key, min);
			}
		}

		updateBuffs();
	}

	public int getFakeHero() {
		return fakeHero;
	}

	public void setFakeHero(int fakeHero) {
		this.fakeHero = fakeHero;
	}

	public HashMap<Integer, Integer> getBuffs() {
		return buffs;
	}

	public void setBuffs(HashMap<Integer, Integer> buffs) {
		this.buffs = buffs;
	}

	public long getPower() {
		return power;
	}

	public void setPower(long power) {
		this.power = power;
	}

	public HashMap<Integer, RelicBean> getRelics() {
		return relics;
	}

	public void setRelics(HashMap<Integer, RelicBean> relics) {
		this.relics = relics;
	}

	/**
	 * 查找指定的圣物id
	 * @param _relic 圣物id
	 * @return
	 */
	public RelicBean findRelic(int _relic) {
		return relics.getOrDefault(_relic, null);
	}

	public int getRelicid() {
		return relicid;
	}

	public void setRelicid(int relicid) {
		this.relicid = relicid;
	}

	/**
	 * 更新所有buff
	 * <p>
	 * 1. 战神像
	 * 2. 皮肤
	 */
	public void updateBuffs() {
		buffs.clear();

		// 1. 战神像更新buff
		for (int i = 1; i <= chain; ++i) {
			ChainConf cc = Readonly.getInstance().findChain(i);
			int end = 5;
			if (i == chain) {
				end = chainattr;
			}
			for (int j = 0; j < end; ++j) {
				updateBuff(cc.reward[j], 1);
			}
		}

		// 3. 皮肤套装更新buff
		skinsuit.forEach((k, v) -> {
			MasksuitConf msc = Readonly.getInstance().findMasksuits().get(k - 1);
			if (msc != null) {
				updateBuff(msc.basic, 1);
				updateBuff(msc.stratt, v - 1);
			}
		});
	}

	private void updateBuff(int[] buf, int r) {
		if (buf.length > 1) {
			for (int i = 0; i < buf.length; i += 2) {
				int eff = buf[i];
				int val = buf[i + 1] * r;
				buffs.merge(eff, val, (a, b) -> a + b);
			}
		}
	}

	public String logInfos() {
		String info = "玩家英雄(" + this.id + ") ";
		for (HeroBean hb : heros) {
			if (hb != null) {
				HeroConf hc = Readonly.getInstance().findHero(hb.getId());
				info += hc.name + ":" + hb.toString() + "|";
			}
		}

		for (HeroBean hb : assists) {
			if (hb != null) {
				HeroConf hc = Readonly.getInstance().findHero(hb.getId());
				info += "助战," + hc.name + ":" + hb.toString() + "|";
			}
		}

		for (SkinStruct ss :skins.values()) {
			if (ss != null) {
				info += "皮肤," + ss.id + "," + ss.level + "|";
			}
		}

		return info;
	}

	/**
	 * 复制自己
	 * @return
	 */
	public HerosStruct createHerosStruct() {
		HerosStruct hss = new HerosStruct();
		hss.uid = this.id;
		hss.fake = this.fakeHero;
		hss.skin = this.skin;
		this.skins.forEach((k, v) -> hss.skins.put(k, v.level));
		hss.buffs.putAll(this.buffs);

		for (int i = 0; i < 6; ++i) {
			HeroBean hb = heros[i];
			if (hb != null) {
				hss.heros[i] = hb.createHeroStruct();
			} else {
				hss.heros[i] = null;
			}

			HeroBean ab = assists[i];
			if (ab != null) {
				hss.assists[i] = ab.getId();
			}
		}

		if (relicid > 0) {
			RelicBean rb = relics.getOrDefault(relicid, null);
			if (rb != null) {
				hss.relics.add(rb.createRelicStruct());
			}
		}

		relics.forEach((k, v) -> {
			if (k != relicid && v != null) {
				hss.relics.add(v.createRelicStruct());
			}
		});

		return hss;
	}

	/**
	 *
	 * 激活圣物，如果没有就增加一个
	 *
	 * @param rid
	 * @return
	 */
	public RelicBean addRelic(int rid) {
		RelicBean rb = relics.getOrDefault(rid, null);
		if (rb == null) {
			rb = new RelicBean(rid);
			relics.put(rid, rb);
		} else {
			return null;
		}

		if (relicid == 0) {
			relicid = rid;
		}

		return rb;
	}

	public RelicBean changeRelic(int rid) {
		RelicBean rb = relics.getOrDefault(rid, null);
		if (rb == null) {
			return null;
		}

		relicid = rid;
		return rb;
	}

	/***
	 *
	 * 注灵增加圣物的经验, 每次调用不会升2级，所以循环判断不用担心
	 *
	 * @param rb
	 * @param total
	 * @return
	 */
	public boolean addRelicExp(RelicBean rb, int total) {
		int level_limit = rb.star * 20;
		if (level_limit <= 0) {
			return false;
		}

		int cost = Readonly.getInstance().findHolyexp(rb.level);
		if (cost < 0) {
			return false;
		}

		while (total > 0) {
			int need = cost - rb.exp;
			if (total >= need) {
				// 本次注灵导致升级了，判断一下最高能到达的等级是否满足条件
                if (rb.level >= level_limit) {
                	return false;
				}

				total -= need;
				++rb.level;
				cost = Readonly.getInstance().findHolyexp(rb.level);
				if (cost < 0) { //证明之后已经无法升级
					break;
				}
			} else {
				rb.exp += total;
				break;
			}
		}
		return true;
	}

	public RelicBean strongRelic(int rid, int star, int level) {
		RelicBean rb = relics.getOrDefault(rid, null);
		if (rb == null) {
			return null;
		}

		rb.star = star;
		rb.level = level;

		return rb;
	}

	public RelicBean strongRelicHero(int rid, int level) {
		RelicBean rb = relics.getOrDefault(rid, null);
		if (rb == null) {
			return null;
		}

		rb.hlvl = level;

		return rb;
	}

	public RelicBean wakeRelicHero(int rid, int level) {
		RelicBean rb = relics.getOrDefault(rid, null);
		if (rb == null) {
			return null;
		}

		rb.hwlvl = level;

		return rb;
	}

	/**
	 * 圣物的12个属性,index:[0,11]分别增加哪一个
	 *
	 *
	 * @param rid
	 * @param index
	 * @return
	 */
	public RelicBean strongRelicAttrs(int rid, int index) {
		RelicBean rb = relics.getOrDefault(rid, null);
		if (rb == null) {
			return null;
		}

		if (rb.hero == 0) {
			HolyConf hc = Readonly.getInstance().findHoly(rid);
			rb.hero = hc.hero;
		}

		++rb.attrs[index];
		return rb;
	}

}
