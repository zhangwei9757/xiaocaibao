package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.modelconf.*;
import com.tumei.common.utils.TimeUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/1/17 0017.
 * 竞技场商店单独处理
 * <p>
 * 这里有 道具商店
 * 远征商店
 * 等。。。
 */
@Document(collection = "Role.Store")
public class StoreBean {
	@JsonIgnore
	@Id
	private String ObjectId;
	@Field("id")
	private Long id;

	private int flushItemStoreDay;
	private Map<Integer, Integer> itemStoreMap = new HashMap<>();

	private int flushRaidStoreDay;
	private Map<Integer, Integer> raidStoreMap = new HashMap<>();

	// 公会商店刷新时间
	private int flushGroupStoreDay;
	private Map<Integer, Integer> groupStoreMap = new HashMap<>();
	private Map<Integer, Integer> groupStore2Map = new HashMap<>();

	// 神器商店
	private int flushArtStoreDay;
	// 刷新次数
	private int artCount;
	private List<StoreStruct> artStore = new ArrayList<>();


	/**
	 * 第二个商店上次刷新的时间
	 */
	private long lastGroupStore2Flush;

	/**
	 * 上次刷新英雄商店的时间，7200秒刷新一次
	 */
	private long lastFlushHeroStore;
	/**
	 * 英雄商店今日刷新次数
	 */
	private int flushHeroCount;

	private int flushHeroCountDay;

	/**
	 * 英雄商店暂存表
	 */
	private List<StoreStruct> heroStore = new ArrayList<>();

	public StoreBean() {
	}

	public StoreBean(long _id) {
		id = _id;
	}


	public Map<Integer, Integer> flushItemStore() {
		int today = TimeUtil.getToday();

		if (today != flushItemStoreDay) {
			List<ItemstoreConf> ics = Readonly.getInstance().findItemStores();

			for (ItemstoreConf ic : ics) {
				if (ic.clear == 1) {
					itemStoreMap.remove(ic.key);
				}
			}
			flushItemStoreDay = today;
		}

		return itemStoreMap;
	}

	/**
	 * 远征商店购买次数记录的刷新
	 * @return
	 */
	public Map<Integer, Integer> flushRaidStore() {
		int today = TimeUtil.getToday();

		if (today != flushRaidStoreDay) {
			List<RaidstoreConf> ics = Readonly.getInstance().findRaidStores();

			for (RaidstoreConf ic : ics) {
				if (ic.clear == 1) {
					raidStoreMap.remove(ic.key);
				}
			}
			flushRaidStoreDay = today;
		}

		return raidStoreMap;
	}

	public void flushGroupStore() {
		int today = TimeUtil.getToday();

		if (today != flushGroupStoreDay) {
			List<GroupstoreConf> ics = Readonly.getInstance().getGroupstoreConfs();

			for (GroupstoreConf ic : ics) {
				if (ic.clear == 1) {
					groupStoreMap.remove(ic.key);
				}
			}
			flushGroupStoreDay = today;
		}
	}

	public Map<Integer, Integer> flushGroupStore2(int level) {
		long now = System.currentTimeMillis() / 1000;
		long diff = now - this.lastGroupStore2Flush;

		if (diff >= 21600) { // 刷新
			this.lastGroupStore2Flush = now - (diff % 21600);
			this.groupStore2Map.clear();
			List<Groupstore2Conf> ics = Readonly.getInstance().getGroupstore2Confs();
			List<Groupstore2Conf> tmp = ics.stream().filter(g2c ->
				g2c.guildlevel <= level
			).collect(Collectors.toList());

			int count  = tmp.size();
			for (int i = 0; i < 4; ++i) {
				int idx = RandomUtil.getRandom() % count;
				Groupstore2Conf gc = tmp.get(idx);
				this.groupStore2Map.put(gc.key, 0);
			}
		}
		return this.groupStore2Map;
	}



	/**
	 * 刷新英雄商店
	 *
	 * @param lordLevel 领主的等级
	 * @return
	 */
	public List<StoreStruct> flushHeroStore(int lordLevel, boolean force) {
		int today = TimeUtil.getToday();
		if (today != flushHeroCountDay) {
			flushHeroCountDay = today;
			flushHeroCount = 0;
		}

		long now = System.currentTimeMillis() / 1000;

		if (now >= (lastFlushHeroStore + 7200)) {
			lastFlushHeroStore = now;
			force = true;
		}

		if (force) {
			List<HerostoreConf> hcs = Readonly.getInstance().findHeroStores();
			hcs = hcs.stream().filter(hc -> hc.level <= lordLevel).collect(Collectors.toList());
			//随机选择六个出来
			Collections.shuffle(hcs);

			int max = (hcs.size() < 6) ? hcs.size() : 6;

			heroStore.clear();
			for (int i = 0; i < max; ++i) {
				HerostoreConf hc = hcs.get(i);
				StoreStruct ss = new StoreStruct(hc.key);
				ss.id = hc.item;
				ss.count = hc.num;
				ss.price = hc.price;
				ss.limit = 1;
				heroStore.add(ss);
			}
		}

		return heroStore;
	}

	public List<StoreStruct> flushArtStore(boolean force) {
		int today = TimeUtil.getToday();
		if (force || today != flushArtStoreDay) {
			flushArtStoreDay = today;
			if (force) {
				++artCount;
			} else {
				artCount = 0;
			}
			artStore.clear();
			List<ArtstoreConf> artstoreConfs = Readonly.getInstance().getArtstoreConfs();
			for (int i = 1; i <= 8; ++i) {
				int a = i;
				List<ArtstoreConf> tmp = artstoreConfs.stream().filter(ac -> ac.part == a).collect(Collectors.toList());
				// 根据每一个的权重计算是否显示
				float total = 0;
				for (ArtstoreConf ac : tmp) {
					total += ac.hev;
				}
				float r = RandomUtil.getFloat();
				float t = 0;
				for (ArtstoreConf ac : tmp) {
					t += ac.hev;
					if (r <= t / total) {
						StoreStruct ss = new StoreStruct();
						ss.key = ac.key;
						ss.id = ac.good[0];
						ss.count = ac.good[1];
						ss.price = ac.price;
						if (ac.limit.length != 0) {
							ss.limit = ac.limit[0] * 10000 + ac.limit[1];
						}

						artStore.add(ss);
						break; // 命中
					}
				}
			}
		}
		return artStore;
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

	public int getFlushItemStoreDay() {
		return flushItemStoreDay;
	}

	public void setFlushItemStoreDay(int flushItemStoreDay) {
		this.flushItemStoreDay = flushItemStoreDay;
	}

	public Map<Integer, Integer> getItemStoreMap() {
		return itemStoreMap;
	}

	public void setItemStoreMap(Map<Integer, Integer> itemStoreMap) {
		this.itemStoreMap = itemStoreMap;
	}

	public long getLastFlushHeroStore() {
		return lastFlushHeroStore;
	}

	public void setLastFlushHeroStore(long lastFlushHeroStore) {
		this.lastFlushHeroStore = lastFlushHeroStore;
	}

	public List<StoreStruct> getHeroStore() {
		return heroStore;
	}

	public void setHeroStore(List<StoreStruct> heroStore) {
		this.heroStore = heroStore;
	}

	public int getFlushHeroCount() {
		return flushHeroCount;
	}

	public void setFlushHeroCount(int flushHeroCount) {
		this.flushHeroCount = flushHeroCount;
	}

	public int getFlushHeroCountDay() {
		return flushHeroCountDay;
	}

	public void setFlushHeroCountDay(int flushHeroCountDay) {
		this.flushHeroCountDay = flushHeroCountDay;
	}

	public int getFlushRaidStoreDay() {
		return flushRaidStoreDay;
	}

	public void setFlushRaidStoreDay(int flushRaidStoreDay) {
		this.flushRaidStoreDay = flushRaidStoreDay;
	}

	public Map<Integer, Integer> getRaidStoreMap() {
		return raidStoreMap;
	}

	public void setRaidStoreMap(Map<Integer, Integer> raidStoreMap) {
		this.raidStoreMap = raidStoreMap;
	}

	public int getFlushGroupStoreDay() {
		return flushGroupStoreDay;
	}

	public void setFlushGroupStoreDay(int flushGroupStoreDay) {
		this.flushGroupStoreDay = flushGroupStoreDay;
	}

	public Map<Integer, Integer> getGroupStoreMap() {
		return groupStoreMap;
	}

	public void setGroupStoreMap(Map<Integer, Integer> groupStoreMap) {
		this.groupStoreMap = groupStoreMap;
	}

	public Map<Integer, Integer> getGroupStore2Map() {
		return groupStore2Map;
	}

	public void setGroupStore2Map(Map<Integer, Integer> groupStore2Map) {
		this.groupStore2Map = groupStore2Map;
	}

	public long getLastGroupStore2Flush() {
		return lastGroupStore2Flush;
	}

	public void setLastGroupStore2Flush(long lastGroupStore2Flush) {
		this.lastGroupStore2Flush = lastGroupStore2Flush;
	}

	public int getFlushArtStoreDay() {
		return flushArtStoreDay;
	}

	public void setFlushArtStoreDay(int flushArtStoreDay) {
		this.flushArtStoreDay = flushArtStoreDay;
	}

	public int getArtCount() {
		return artCount;
	}

	public void setArtCount(int artCount) {
		this.artCount = artCount;
	}

	public List<StoreStruct> getArtStore() {
		return artStore;
	}

	public void setArtStore(List<StoreStruct> artStore) {
		this.artStore = artStore;
	}
}
