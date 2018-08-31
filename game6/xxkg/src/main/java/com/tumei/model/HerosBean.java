package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.fight.ArtifactStruct;
import com.tumei.common.fight.HeroStruct;
import com.tumei.common.fight.PowerStruct;
import com.tumei.game.protos.structs.SkinStruct;
import com.tumei.model.beans.ArtifactBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.ChainConf;
import com.tumei.modelconf.HeroConf;
import com.tumei.modelconf.MaskConf;
import com.tumei.modelconf.MasksuitConf;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Long id; //

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
	 * 当前拥有的时装
	 */
	private HashMap<Integer, SkinStruct> skins = new HashMap<>();

	/**
	 * 使用的时装
	 */
	private int skin = 0;

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
	 * 神器, Key:神器的id, val:对应神器的属性
	 */
	private HashMap<Integer, ArtifactBean> artifacts = new HashMap<>();

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

		DaoService.getInstance().pushSta(id, "addskin|" + id + "|" + 1 + "|" + reason);

		// 增加皮肤改变套装属性加成
		checkSkinsuits();
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

		PackBean pb = DaoService.getInstance().findPack(this.id);

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

	public HashMap<Integer, ArtifactBean> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(HashMap<Integer, ArtifactBean> artifacts) {
		this.artifacts = artifacts;
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

	/**
	 * 创建计算战斗力 需要的结构
	 *
	 * @return
	 */
	public PowerStruct createTeamStruct() {
		PowerStruct ts = new PowerStruct();
		ts.setUid(this.id);
		ts.setFasion(skin);
		fill(ts.getLineups(), ts.getBuffs(), ts.getHeros(), ts.getArts());
		return ts;
	}

	/***
	 * 战斗数据填充
	 *
	 * @param _lineups
	 * @param _buffs
	 * @param _heros
	 */
	public void fill(int[] _lineups, Map<Integer, Integer> _buffs, List<HeroStruct> _heros, List<ArtifactStruct> arts) {
		int sl = 0;
		SkinStruct ss = skins.getOrDefault(skin, null);
		if (ss != null) {
			sl = ss.level;
		}

		if (_buffs != null) {
			this.buffs.forEach((k, v) -> _buffs.merge(k, v, (a, b) -> a + b));
		}

		for (int i = 0; i < 6; ++i) {
			if (_lineups != null) {
				_lineups[i] = lineups[i];
			}

			if (heros[i] != null) {
				HeroStruct hs = heros[i].createHeroStruct(skin, sl);
				hs.assist = false;
				_heros.add(hs);
			}
		}

		for (int i = 0; i < 6; ++i) {
			if (assists[i] != null) {
				HeroStruct hs = assists[i].createHeroStruct();
				hs.assist = true;
				_heros.add(hs);
			}
		}

		if (arts != null) {
			artifacts.forEach((k, v) -> arts.add(v.createStruct()));
		}
	}

	/**
	 * 创建机器人需要的结构
	 * @param _heros
	 * @return
	 */
	public static PowerStruct createTeamStructForRobot(HeroBean[] _heros) {
		PowerStruct ts = new PowerStruct();
		ts.setFasion(0);

		for (HeroBean hb : _heros) {
			if (hb != null) {
				HeroStruct hs = hb.createHeroStruct();
				ts.getHeros().add(hs);
			}
		}
		return ts;
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
	 * 激活神器的部件
	 * @param artid
	 * @param comid
	 * @return
	 */
	public boolean equipArt(int artid, int comid) {
		ArtifactBean ab = artifacts.getOrDefault(artid, null);
		if (ab == null) {
			ab = new ArtifactBean(artid);
			artifacts.put(artid, ab);
		}

		return ab.equipCom(comid);
	}

	public boolean promoteArt(int artid) {
		ArtifactBean ab = artifacts.getOrDefault(artid, null);
		if (ab == null) {
			return false;
		}

		return ab.promote();
	}
}
