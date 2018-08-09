package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.FuwenraidConf;
import com.tumei.modelconf.VipConf;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 * 锤子敲宝箱
 */
@Document(collection = "Role.Rune")
public class RuneBean {
	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private Long id;

	/**
	 * 锤子个数，如果为0，需要先选择；
	 */
	private int hammer = -1;
	private int hammermode = 0;

	/**
	 * 当前层数
	 */
	private int level = 1;

	/**
	 * 各种奖励对应的个数，会减少, 每层刷新
	 * <p>
	 * 物品奖励；折扣奖励；怪物；钥匙
	 */
	private int[] numbers;
	/**
	 * 各种机率
	 */
	private int[] ratios;

	/**
	 * 今天的重置次数
	 */
	private int usedCount;

	/**
	 * 是否找到钥匙
	 */
	private boolean isOver;

	/**
	 * 最佳奖励, awardBean数量是负数表示已经被领取过了
	 */
	private List<AwardBean> best = new ArrayList<>();

	/**
	 * 今天重置单层的次数
	 */
	private int revert;

	/**
	 * 上次刷新日期
	 */
	private int flushDay;

	/**
	 * 9个位置的东西
	 */
	private int[] objects = new int[9];

	/**
	 * 阵营
	 */
	private int group;

	/**
	 * 上次的阵营
	 */
	private int lastGroup;

	private int score;

	/**
	 * 折扣商店, key是价格,value是物品
	 */
	private List<StoreStruct> stores = new ArrayList<>();

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getHammermode() {
		return hammermode;
	}

	public void setHammermode(int hammermode) {
		this.hammermode = hammermode;
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

	public int getHammer() {
		return hammer;
	}

	public void setHammer(int hammer) {
		this.hammer = hammer;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int[] getNumbers() {
		return numbers;
	}

	public void setNumbers(int[] numbers) {
		this.numbers = numbers;
	}

	public int[] getRatios() {
		return ratios;
	}

	public void setRatios(int[] ratios) {
		this.ratios = ratios;
	}

	public int getUsedCount() {
		return usedCount;
	}

	public void setUsedCount(int usedCount) {
		this.usedCount = usedCount;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getLastGroup() {
		return lastGroup;
	}

	public void setLastGroup(int lastGroup) {
		this.lastGroup = lastGroup;
	}

	public boolean isOver() {
		return isOver;
	}

	public void setOver(boolean over) {
		isOver = over;
	}

	public List<AwardBean> getBest() {
		return best;
	}

	public void setBest(List<AwardBean> best) {
		this.best = best;
	}

	public int getRevert() {
		return revert;
	}

	public void setRevert(int revert) {
		this.revert = revert;
	}

	public int getFlushDay() {
		return flushDay;
	}

	public void setFlushDay(int flushDay) {
		this.flushDay = flushDay;
	}

	public int[] getObjects() {
		return objects;
	}

	public void setObjects(int[] objects) {
		this.objects = objects;
	}

	public List<StoreStruct> getStores() {
		return stores;
	}

	public void setStores(List<StoreStruct> stores) {
		this.stores = stores;
	}

	/**
	 * 每日凌晨刷新
	 */
	public void flush(int vip) {
		int today = TimeUtil.getToday();
		if (today != flushDay) {
			flushDay = today;
			usedCount = 0;

//			VipConf vc = Readonly.getInstance().findVip(vip);
//			revert = vc.fuwenag;

			// 周一切换阵营
			if (LocalDate.now().getDayOfWeek().getValue() == 1 || group == 0) {
				lastGroup = group;
				group = (RandomUtil.getRandom() % 4) + 1;
			}
		}
	}

	/**
	 * 前进到指定的层
	 *
	 * @param force 强制进入当前层，层不会增加，不强制进入，则根据isOver是否找到钥匙，来确定是否进入下一层，层要增加
	 * @return
	 */
	public boolean step(boolean force) {
		if (!isOver && !force) {
			return false;
		}

		if (!force) {
			++level;
		}

		isOver = false;

		// 根据当前层，获取numbers;
		FuwenraidConf fc = Readonly.getInstance().findFuwen(level);
		if (fc != null) {
			// 如果关卡还有就重置，没有就不理会
			for (int i = 0; i < 9; ++i) {
				objects[i] = 0;
			}

			numbers = Arrays.copyOf(fc.spread, 4);
			ratios = Arrays.copyOf(fc.rewardrate, 5);
			int total = 0;
			for (int i = 0; i < 5; ++i) {
				total += ratios[i];
				ratios[i] = total;
			}

			if (hammer != -1) {
				hammer = hammermode * 5;
			}

			// 制造最佳奖励
			best.clear();
			int c = fc.bestdetail.length / 2;
			for (int i = 0; i < 2; ++i) {
				int idx = RandomUtil.getRandom() % c;
				best.add(new AwardBean(fc.bestdetail[idx * 2], fc.bestdetail[idx * 2 + 1], 0));
			}
		}

		return true;
	}

	/**
	 * 充值
	 *
	 * @param flag true: 大重置
	 *             false: 小重置
	 */
	public boolean reset(int vip, boolean flag) {
		if (flag) {
			VipConf vc = Readonly.getInstance().findVip(vip);
			if (usedCount < vc.fuwenre) {
				++usedCount;
				hammer = -1;
				hammermode = 0;
				level = 1;
				revert = vc.fuwenag;
				stores.clear();
			}
			else {
				return false;
			}
		}
		else {
			if (revert > 0) {
				--revert;
				hammer = hammermode * 5;
			}
			else {
				return false;
			}
		}

		step(true);

		return true;
	}

	/**
	 * 开挖
	 *
	 * @param index [0, 9]
	 * @return <-1: 其他错误
	 * -1: 没有足够的锤子
	 * 0: 打不开
	 * >0: 各种表示
	 */
	public int dig(int index) {
		if (hammer > 0) {
			--hammer;

			int r = RandomUtil.getRandom() % 100;
			int i;
			// 四种物品的出现概率
			for (i = 0; i < 5; ++i) {
				if (r <= ratios[i]) {
					break;
				}
			}

			if (i > 0 && i < 5) {
				int j = i - 1;
				numbers[j] = numbers[j] - 1;
				if (numbers[j] <= 0) {
					// 此时，需要将ratios中i部分的概率转移到i=0上
					int diff = ratios[i] - ratios[i - 1];
					for (int ii = 0; ii < i; ++ii) {
						ratios[ii] = ratios[ii] + diff;
					}
				}

				FuwenraidConf fc = Readonly.getInstance().findFuwen(level);
				switch (i) {
					case 1: // 物品奖励
					{
						int rr = RandomUtil.getBetween(1, 100);
						if (rr <= fc.best) {
							// 中了最佳，查看是否还有最佳剩余
							int k = 5;
							for (AwardBean ab : best) {
								if (ab.count > 0) {
									ab.count = -ab.count;
									objects[index] = k;
									return k; // 5是第一个最佳，6是第二个最佳
								}
								++k;
							}
							// 到了这里表示最近已经没有了，直接返回物品 4。
						}
					}
					break;
					case 2: // 打折奖励
					{

					}
					break;
					case 3: // 怪物
						break;
					case 4: // 钥匙
						isOver = true;
						break;
				}

				objects[index] = i;
				return i;
			}
			objects[index] = 9;
			return 9;
		}
		else {
			return -1;
		}
	}

	public RuneBean() {
	}

	public RuneBean(long _id) {
		id = _id;
		hammer = -1;
		step(true);
	}
}
