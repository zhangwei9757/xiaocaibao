package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.GameConfig;
import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.TimeUtil;
import com.tumei.controller.user.HeroInfo;
import com.tumei.game.GameServer;
import com.tumei.game.services.OpenRankService;
import com.tumei.game.services.RobService;
import com.tumei.model.beans.ArtifactComBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.EquipConf;
import com.tumei.modelconf.HeroConf;
import com.tumei.modelconf.ItemConf;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 * 背包信息:
 * 1. 英雄背包
 * 2. 装备、宝物背包
 * 3. 道具背包
 */
@Document(collection = "Role.Package")
public class PackBean {
	@JsonIgnore
	@Id
	private String objectId;

	@Field("id")
	private Long id;
	/**
	 * 英雄分配id最大值
	 * <p>
	 * 初始为1，因为默认玩家会有一个英雄
	 */
	private int maxhid = 100;
	/**
	 * 装备分配id最大值
	 */
	private int maxeid = 100;
	/**
	 * 英雄上限
	 */
	private int herolimit = 100;
	/**
	 * 装备上限
	 */
	private int equiplimit = 100;

	/**
	 * 活力
	 */
	private long lastspirit = 0;

	/**
	 * 金币
	 */
	private long coin = 0;

	/**
	 * 钻石
	 */
	private int gem = 0;

	/**
	 * 拥有的所有英雄
	 */
	private HashMap<Integer, HeroBean> heros = new HashMap<>();
	/**
	 * 拥有的所有装备
	 */
	private HashMap<Integer, EquipBean> equips = new HashMap<>();
	/**
	 * 道具物品，无上限，可叠放
	 * <p>
	 * 1. 道具
	 * 2. 符文
	 * 3. 装备碎片
	 * 4. 英雄碎片
	 * 5. 不可显示的所有币与宝石
	 */
	private HashMap<Integer, Integer> items = new HashMap<>();

	/**
	 * 神器部件,key是对应的部件id, val是对应的个数
	 */
	private HashMap<Integer, Integer> artcoms = new HashMap<>();

	/**
	 * 初始创建背包
	 *
	 * @param id
	 * @return
	 */
	public static PackBean createNewPack(long id) {
		PackBean pb = new PackBean();
		pb.setId(id);
		pb.lastspirit = System.currentTimeMillis() / 1000;

		pb.addItem(金币, Defs.初始化金币, "初始");
		pb.addItem(钻石, Defs.初始化钻石, "初始发放");

		pb.addItem(活力, 30, "初始");
		pb.addItem(突破石, 100, "初始");
		pb.addItem(初级精炼石, 10, "初始");

		pb.addItem(20012, 1, "初始");
		pb.addItem(20013, 1, "初始");

		pb.addEquip(10010, "初始");
		pb.addEquip(10020, "初始");

		return pb;
	}

	/**
	 * 刷新活力, 增加活力之前也需要刷新
	 *
	 * @param _extra 更新后是否增加
	 * @return
	 */
	public int flushSpirit(int _extra) {
		GameConfig gc = GameConfig.getInstance();

		int spirit = items.getOrDefault(活力, 0);
		long now = System.currentTimeMillis() / 1000;
		long diff = now - lastspirit;
		int count = (int) (diff / gc.getSp_recover_interval());
		if (count > 0 || _extra != 0) { // 30分钟一次加点
			int max = gc.getUser_spirit_max();

			if (spirit < max) {
				spirit = spirit + count;
				if (spirit > max) {
					spirit = max;
				}
			}

			spirit += _extra;

			items.put(活力, spirit);
			if (count > 0) {
				lastspirit = now - (diff % gc.getSp_recover_interval());
			}
		}
		return spirit;
	}

	public long getLastspirit() {
		return lastspirit;
	}

	public void setLastspirit(long lastspirit) {
		this.lastspirit = lastspirit;
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

	public int getMaxhid() {
		return maxhid;
	}

	public void setMaxhid(int maxhid) {
		this.maxhid = maxhid;
	}

	public int getMaxeid() {
		return maxeid;
	}

	public void setMaxeid(int maxeid) {
		this.maxeid = maxeid;
	}

	public int getHerolimit() {
		return herolimit;
	}

	public void setHerolimit(int herolimit) {
		this.herolimit = herolimit;
	}

	public int getEquiplimit() {
		return equiplimit;
	}

	public void setEquiplimit(int equiplimit) {
		this.equiplimit = equiplimit;
	}

	public HashMap<Integer, HeroBean> getHeros() {
		return heros;
	}

	public void setHeros(HashMap<Integer, HeroBean> heros) {
		this.heros = heros;
	}

	public HashMap<Integer, EquipBean> getEquips() {
		return equips;
	}

	public void setEquips(HashMap<Integer, EquipBean> equips) {
		this.equips = equips;
	}

	public HashMap<Integer, Integer> getArtcoms() {
		return artcoms;
	}

	public void setArtcoms(HashMap<Integer, Integer> artcoms) {
		this.artcoms = artcoms;
	}

	/**
	 * 增加英雄
	 *
	 * @param hero
	 * @return 返回null, 表示英雄上限满
	 */
	public HeroBean addHero(int hero, String reason) {
//		if (herolimit <= heros.size()) {
//			return null;
//		}
		HeroBean hb = new HeroBean();
		hb.setId(hero);
		++maxhid;
		hb.setHid(maxhid);
		heros.put(maxhid, hb);

		DaoService.getInstance().findSta(this.id).addHero(hero);

		DaoService.getInstance().pushSta(this.id, "addhero|" + hero + "|" + 1 + "|" + reason);

		return hb;
	}

	public HeroBean findHero(int hid) {
		return heros.getOrDefault(hid, null);
	}

	/**
	 * 删除英雄, 分解成魂石
	 *
	 * @param hid
	 */
	public void removeHero(int hid) {
		heros.remove(hid);
	}

	/**
	 * 突破的时候支付初级英雄
	 *
	 * @param hero
	 * @param count
	 */
	public List<Integer> payHero(int hero, int count, String reason) {
		List<Integer> rtn = new ArrayList<>();
		if (count <= 0) {
			return null;
		}

		Object[] sets = heros.keySet().toArray();
		for (Object o : sets) {
			int key = (Integer) o;
			HeroBean hb = heros.get(key);
			if (hb != null && hb.getId() == hero && hb.isInitStatus()) {
				heros.remove(key);
				rtn.add(key);
				if (--count == 0) {
					break;
				}
			}
		}
		DaoService.getInstance().pushSta(this.id, "payhero|" + hero + "|" + count + "|" + reason);
		return rtn;
	}

	/**
	 * 挑战英雄的时候，会卸载到背包中
	 *
	 * @param hb
	 */
	public void returnHero(HeroBean hb) {
		heros.put(hb.getHid(), hb);
	}

	/**
	 * 增加装备
	 *
	 * @param equip
	 * @return 返回null, 表示装备上限满
	 */
	public EquipBean addEquip(int equip, String reason) {
//		if (equiplimit <= equips.size()) {
//			return null;
//		}

		EquipBean hb = new EquipBean();
		hb.setId(equip);
		hb.setEid(++maxeid);
		equips.put(maxeid, hb);

		DaoService.getInstance().pushSta(this.id, "addequip|" + equip + "|" + 1 + "|" + reason);

		return hb;
	}

	/**
	 * 消耗装备, 出售装备的时候会使用
	 *
	 * @param eid
	 */
	public void payEquip(int eid, String reason) {
		DaoService.getInstance().pushSta(this.id, "payeq|" + eid + "|" + 1 + "|" + reason);
		equips.remove(eid);
	}

	/**
	 * 增加神器组件个数
	 * @param id
	 * @param val
	 * @param reason
	 */
	public void addArtcom(int id, int val, String reason) {
		DaoService.getInstance().pushSta(this.id, "addartcom|" + id + "|" + val + "|" + reason);
		artcoms.merge(id, val, (a, b) -> a + b);
	}

	/**
	 * 消耗神器组件
	 * @param id
	 * @param val
	 * @param reason
	 */
	public void payArtcom(int id, int val, String reason) {
		artcoms.merge(id, val, (a, b) -> a - b);
	}

	/**
	 * 检测神器组件的个数是否满足
	 * @param id
	 * @param val
	 * @return
	 */
	public boolean hasArtcom(int id, int val) {
		int count = artcoms.getOrDefault(id, 0);
		if (count < val) {
			return false;
		}
		return true;
	}

	/**
	 * 宝物分解，或者升级宝物的时候会消耗宝物
	 *
	 * @param key
	 * @return
	 */
	public EquipBean payTreasure(int key, String reason) {
		DaoService.getInstance().pushSta(this.id, "paytr|" + key + "|" + 1 + "|" + reason);
		return equips.remove(key);
	}

	/**
	 * 初次上阵位置上的英雄如果没有装备，使用该函数构造
	 *
	 * @param equip
	 * @return
	 */
	public EquipBean buildEquip(int equip) {
		EquipBean hb = new EquipBean();
		hb.setId(equip);
		hb.setEid(++maxeid);
		return hb;
	}

	/**
	 * 在背包中增加物品道具，非英雄，非装备
	 *
	 * @param key
	 * @param count
	 */
	public synchronized void addItem(int key, long count, String reason) {
		if (count == 0) {
			return;
		}
		else if (count < 0) {
			throw new RuntimeException("PackBean::addItem 不能小于0");
		}
		else { // 增加
			{
				ItemConf ic = Readonly.getInstance().findItem(key);
				if (ic != null) {
					DaoService.getInstance().pushSta(this.id, "additem|" + ic.good + "|" + count + "|" + reason);
				}
				else {
					DaoService.getInstance().pushSta(this.id, "additem|" + key + "|" + count + "|" + reason);
				}
			}


			switch (key) {
				case 金币: {
					StaBean sb = DaoService.getInstance().findSta(this.id);
					sb.fixGold(count);
					coin += count;
				}
				return;
				case 钻石: {
					StaBean sb = DaoService.getInstance().findSta(this.id);
					sb.fixGem(count);
					gem += count;
				}
				return;
			}
			// 增加碎片，就修改反向索引
			if (Defs.isTreasureFragID(key)) {
				RobService.getInstance().commitFrags(this.id, key, (int) count);
			}
			items.merge(key, (int) count, (a, b) -> (a + b));

		}
	}

	/**
	 * @param key
	 * @param val
	 * @param reason
	 * @return 返回消耗的个数
	 */
	public synchronized long payItem(int key, long val, String reason) {
		if (val < 0) {
			throw new RuntimeException("PackBean::payItem 不能小于0");
		}

		{
			ItemConf ic = Readonly.getInstance().findItem(key);
			if (ic != null) {
				DaoService.getInstance().pushSta(this.id, "payitem|" + ic.good + "|" + val + "|" + reason);
			}
			else {
				DaoService.getInstance().pushSta(this.id, "payitem|" + key + "|" + val + "|" + reason);
			}
		}

		switch (key) {
			case 金币: {
				coin = coin - val;
				if (coin < 0) {
					DaoService.getInstance().fatal(this.id, String.format("消耗物品:%d 数量(%d), 当前错误值(%d) val(%d), 行为原因:(%s)", key, val, coin, val, reason));
					val += coin;
				}

				if (val > 0) {
					StaBean sb = DaoService.getInstance().findSta(this.id);
					sb.fixGold(-val);
				}
			}
			return val;
			case 钻石: {
				gem = gem - (int) val;
				if (gem < 0) {
					DaoService.getInstance().fatal(this.id, String.format("消耗物品:%d 数量(%d), 当前错误值(%d), 行为原因:(%s)", key, val, gem, reason));
					val += gem;
				}

				if (val > 0) {
					StaBean sb = DaoService.getInstance().findSta(this.id);
					sb.fixGem(-val);
					DaoService.getInstance().findActivity(this.id).putDbspends((int) val);
					DaoService.getInstance().findFestival(this.id).onSpend((int)val);
					OpenRankService.getInstance().putSpend(this.id, (int)val);
				}
			}
			return val;
		}

		// 减少碎片，就修改反向索引
		if (Defs.isTreasureFragID(key)) {
			RobService.getInstance().commitFrags(this.id, key, (int) -val);
		}

		int has = items.getOrDefault(key, 0);
		if (val == Integer.MAX_VALUE) { // 表示有多少用多少
			val = has;
		}

		if (has == val) {
			items.remove(key);
			return val;
		}
		else if (has < val) {
			DaoService.getInstance().fatal(this.id, String.format("消耗物品:%d 数量(%d), 但是实际只有(%d), 行为原因:(%s)", key, val, has, reason));
			items.remove(key);
			return -1;
		}

		has -= val;
		items.put(key, has);

		return val;
	}

	/**
	 * 获取物品的数量
	 *
	 * @param key
	 * @return
	 */
	public synchronized int getItemCount(int key) {
		return items.getOrDefault(key, 0);
	}

	public synchronized void delItem(int key) {
		items.remove(key);
	}

	// 用于正反序列化
	public HashMap<Integer, Integer> getItems() {
		return items;
	}

	// 用于正反序列化
	public void setItems(HashMap<Integer, Integer> items) {
		this.items = items;
	}

	/**
	 * 更新抢劫宝物的时候，需要得到宝物信息
	 *
	 * @return
	 */
	public synchronized HashMap<Integer, Integer> copyRobItems() {
		HashMap<Integer, Integer> tmp = new HashMap<>();
		items.forEach((k, v) -> {
			if (Defs.isTreasureFragID(k)) {
				tmp.put(k, v);
			}
		});
		return tmp;
	}

	/**
	 * 检测对应的 资源是否足够
	 *
	 * @param key 资源id
	 * @param val 资源数量
	 * @return
	 */
	public synchronized boolean contains(int key, long val) {
		switch (key) {
			case 金币:
				return (coin >= val);
			case 钻石:
				return (gem >= val);
		}

		if (items.getOrDefault(key, 0) < val) {
			return false;
		}
		return true;
	}

	public synchronized boolean contains(int[] prices, int count) {
		for (int i = 0; i < prices.length; i += 2) {
			int key = prices[i];
			long val = prices[i + 1] * count;
			if (!contains(key, val)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否有指定的宝物
	 *
	 * @param key
	 * @param originid
	 * @return
	 */
	public boolean containsTreasure(int key, int originid) {
		EquipBean eb = equips.getOrDefault(key, null);
		if (eb == null) {
			return false;
		}
		if (originid != 0 && originid != eb.getId()) {
			return false;
		}

		return true;
	}

	/**
	 * 在突破的时候需要初级英雄, n个
	 *
	 * @param key 英雄id
	 * @param val 英雄个数
	 * @return
	 */
	public boolean hasHeros(int key, int val) {
		if (heros.values().stream().filter((HeroBean hb) -> {
			if (hb.getId() == key) {
				if (hb.isInitStatus()) {
					return true;
				}
			}
			return false;
		}).count() >= val) {
			return true;
		}
		return false;
	}

	public long getCoin() {
		return coin;
	}

	public void setCoin(long coin) {
		this.coin = coin;
	}

	public int getGem() {
		return gem;
	}

	public void setGem(int gem) {
		this.gem = gem;
	}

	// 检测是否有装备的eid相同，如果相同，则随机选择一个新建编号
	public void check() {
		HashSet<Integer> eids = new HashSet<>();

		HerosBean hsb = DaoService.getInstance().findHeros(this.id);
		for (HeroBean hb : hsb.getHeros()) {
			if (hb != null) {
				EquipBean[] ebs = hb.getEquips();
				for (EquipBean eb : ebs) {
					if (eb == null)
						continue;
					int eid = eb.getEid();
					if (eids.contains(eid)) {
						eb.setEid(++maxeid);
					}
					else {
						eids.add(eid);
					}
				}
			}
		}

		// key 也需要改变
//		equips.forEach((k, v) -> {
//			if (v != null) {
//				int eid = v.getEid();
//				if (eids.contains(eid)) {
//					v.setEid(++maxeid);
//				} else {
//					eids.add(eid);
//				}
//			}
//		});
	}

	public String logInfos() {
		String info = "玩家装备(" + this.id + ") 金币(" + this.coin + ") 钻石(" + this.gem + ") 英雄:|";
		for (HeroBean hb : heros.values()) {
			if (hb != null) {
				HeroConf hc = Readonly.getInstance().findHero(hb.getId());
				if (hc.quality >= 4) {
					info += hc.name + ":" + hb.toString() + "|";
				}
			}
		}

		info += "装备:|";
		for (EquipBean eb : equips.values()) {
			if (eb != null) {
				ItemConf ic = Readonly.getInstance().findItem(eb.getId());
				if (ic.quality >= 4) {
					info += ic.good + ":" + eb.toString() + "|";
				}
			}
		}

		info += "物品:" + items.toString();
		return info;
	}

}
