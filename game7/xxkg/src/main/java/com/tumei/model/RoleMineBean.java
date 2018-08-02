package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.GameConfig;
import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameServer;
import com.tumei.game.protos.mine.structs.MineMerchantStruct;
import com.tumei.game.protos.mine.structs.MineMonsterStruct;
import com.tumei.game.protos.mine.structs.MineTreasureStruct;
import com.tumei.game.services.mine.MapData;
import com.tumei.model.beans.mine.MineAwardBean;
import com.tumei.modelconf.MineMonsterConf;
import com.tumei.modelconf.MineRefreshConf;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 * 玩家在矿区地图上的信息
 */
@Document(collection = "Role.MineMap")
public class RoleMineBean {
	@JsonIgnore
	@Id
	private String objectId;

	@JsonIgnore
	@Field("id")
	private Long id;

	private int day;

	// 周一刷新
	private int weekDay;

	/**
	 * 当前位置
	 */
	private int position = -1;

	/**
	 * 购买行动力次数
	 */
	private int buyEnergyCount = 0;

	/**
	 * 当前能量
	 */
	private int energy;

	/**
	 * 上次获得能量的时间, 十分钟，600秒一次
	 */
	private long energyTime;

	/**
	 * 一天内可以在矿区攻打矿脉的次数
	 */
	private int attackCount;

	/**
	 * 占领的矿脉
	 */
	private int occupy = -1;

	/**
	 * 宝箱 key为position, value是实际的物品
	 */
	private Map<Integer, MineTreasureStruct> treasure = new HashMap<>();

	/**
	 * 怪物
	 */
	private Map<Integer, MineMonsterStruct> monster = new HashMap<>();

	/**
	 * 商人
	 */
	private Map<Integer, MineMerchantStruct> merchant = new HashMap<>();

	/**
	 * 可见区域
	 */
	private HashSet<Integer> steps = new HashSet<>();

	/**
	 * 当前占领的矿
	 */
	private MineAwardBean mineAward = new MineAwardBean();

	/***
	 * 刷新地图
	 */
	public boolean flush() {
		flushEnergy(System.currentTimeMillis() / 1000);

		int today = TimeUtil.getToday();
		if (today != day) {
			day = today;
			attackCount = 0;
			buyEnergyCount = 0;
		}

		int wDay = TimeUtil.getWeekDay();
		if (wDay != weekDay) {
			weekDay = wDay;
			MapData md = GameServer.getInstance().getMineSystem().getMapData();
			treasure.clear();
			merchant.clear();
			monster.clear();

			// 重新刷新所有地图个人信息
			List<MineRefreshConf> mrcs = Readonly.getInstance().getMineRefreshConfs();
			for (MineRefreshConf mrc : mrcs) {
				// 根据类型，等级，个数，在地图上刷新出来
				switch (mrc.type) {
					case 1: // 矿产
						break;
					case 2: // 宝箱
					{
						List<Integer> positions = new ArrayList<>();
						md.treasure.stream().filter((ho) -> ho.level == mrc.level).mapToInt((ho) -> ho.pos).forEach(positions::add);
						// 随机这个列表
						Collections.shuffle(positions);
						// 取出前N个
						int max = Math.min(mrc.num, positions.size());
						for (int i = 0; i < max; ++i) {
							int pos = positions.get(i);
							MineTreasureStruct mts = new MineTreasureStruct(pos, mrc.level);
							treasure.put(pos, mts);
						}
					}
					break;
					case 3: // 怪物
					{
						List<Integer> positions = new ArrayList<>();
						md.monster.stream().filter((ho) -> ho.level == mrc.level).mapToInt((ho) -> ho.pos).forEach(positions::add);
						// 随机这个列表
						Collections.shuffle(positions);
						// 取出前N个
						int max = Math.min(mrc.num, positions.size());
						for (int i = 0; i < max; ++i) {
							int pos = positions.get(i);

							MineMonsterConf mmc = Readonly.getInstance().getMineMonsterConfs().get(mrc.level - 1);
							MineMonsterStruct mts = new MineMonsterStruct(pos, mrc.level, mmc.quality);
							monster.put(pos, mts);
						}
					}
					break;
					case 4: // 商人
					{
						List<Integer> positions = new ArrayList<>();
						md.npc.stream().filter((ho) -> ho.level == mrc.level).mapToInt((ho) -> ho.pos).forEach(positions::add);
						// 随机这个列表
						Collections.shuffle(positions);
						// 取出前N个
						int max = Math.min(mrc.num, positions.size());
						for (int i = 0; i < max; ++i) {
							int pos = positions.get(i);
							RoleBean rb = DaoGame.getInstance().findRole(this.id);
							MineMerchantStruct mts = new MineMerchantStruct(pos, rb.getLevel());
							merchant.put(pos, mts);
						}
					}
					break;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 检查刷新能量
	 *
	 * @param now
	 */
	public void flushEnergy(long now) {
		long diff = now - energyTime;
		int interval = GameConfig.getInstance().getMine_energy_recover();
		int count = (int) (diff / interval);
		if (count > 0) {
			energyTime = now - (diff % interval);
			int max = GameConfig.getInstance().getMine_energy_max();
			if (energy < max) {
				energy += GameConfig.getInstance().getMine_energy_recover_count() * count;
				if (energy > max) {
					energy = max;
				}
			}
		}
	}

	public RoleMineBean() {
	}

	/**
	 * 角色矿区表现创建的构造函数
	 *
	 * @param _id
	 */
	public RoleMineBean(long _id) {
		id = _id;
		// 首次创建，赋予最大值的能量
		energy = GameConfig.getInstance().getMine_energy_max();
		// 创建一个随机点

		reset();
	}

	/**
	 * 重置当前位置
	 *
	 * @return
	 */
	public int reset() {
		List<Integer> birth = GameServer.getInstance().getMineSystem().getMapData().birth;
		position = RandomUtil.getInList(birth);
		return position;
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

	public HashSet<Integer> getSteps() {
		return steps;
	}

	public void setSteps(HashSet<Integer> steps) {
		this.steps = steps;
	}

	public Map<Integer, MineMonsterStruct> getMonster() {
		return monster;
	}

	public void setMonster(Map<Integer, MineMonsterStruct> monster) {
		this.monster = monster;
	}

	public Map<Integer, MineMerchantStruct> getMerchant() {
		return merchant;
	}

	public void setMerchant(Map<Integer, MineMerchantStruct> merchant) {
		this.merchant = merchant;
	}

	public int getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public long getEnergyTime() {
		return energyTime;
	}

	public void setEnergyTime(long energyTime) {
		this.energyTime = energyTime;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Map<Integer, MineTreasureStruct> getTreasure() {
		return treasure;
	}

	public void setTreasure(Map<Integer, MineTreasureStruct> treasure) {
		this.treasure = treasure;
	}

	public int getAttackCount() {
		return attackCount;
	}

	public void setAttackCount(int attackCount) {
		this.attackCount = attackCount;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getBuyEnergyCount() {
		return buyEnergyCount;
	}

	public void setBuyEnergyCount(int buyEnergyCount) {
		this.buyEnergyCount = buyEnergyCount;
	}

	public synchronized int getOccupy() {
		return occupy;
	}

	public synchronized void setOccupy(int occupy) {
		this.occupy = occupy;
	}

	public MineAwardBean getMineAward() {
		return mineAward;
	}

	public void setMineAward(MineAwardBean mineAward) {
		this.mineAward = mineAward;
	}
}
