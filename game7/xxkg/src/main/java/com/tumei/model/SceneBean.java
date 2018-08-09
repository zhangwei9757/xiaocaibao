package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.DaoGame;
import com.tumei.game.GameUser;
import com.tumei.game.protos.scene.RequestSceneFast;
import com.tumei.game.protos.structs.OfflineAwardStruct;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.festival.FestivalBean;
import com.tumei.modelconf.RaidConf;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.modelconf.VipConf;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

import static com.tumei.common.utils.Defs.经验;
import static com.tumei.common.utils.Defs.金币;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "Role.Scene")
public class SceneBean {
	@JsonIgnore
	@Id
	private String objectId;

	@JsonIgnore
	@Field("id")
	private Long id;
	/**
	 * 当前挂机的场景,不能前移
	 */
	private int scene = 0;
	/**
	 * 能量槽, 上限为120，不会改变，不能超出
	 */
	private int energy;

	/**
	 * 60秒累计
	 */
	private int cumerate;
	/**
	 * 已经经历了多少个60秒小事件,
	 * 每满十一个跳为0
	 */
	private int cumcount;
	/**
	 * 每十一个事件生成一个大事件，发送一次大奖励,
	 * 这里可以记录玩家总共经历了多少个事件
	 */
	private int largecount;

	/**
	 * 上次收割的时间
	 */
	private Date lastHarvest;

	/**
	 * 上次增长energy的时间
	 */
	private long lastEnergy;

	/**
	 * 今日内挂机加速次数
	 */
	private int speedCount;

	/**
	 * 上次刷新的日期 20160203
	 */
	private int lastFlushDay;

	public long getLastEnergy() {
		return lastEnergy;
	}

	public void setLastEnergy(long lastEnergy) {
		this.lastEnergy = lastEnergy;
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

	public int getScene() {
		return scene;
	}

	public void setScene(int scene) {
		this.scene = scene;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public int getCumerate() {
		return cumerate;
	}

	public void setCumerate(int cumerate) {
		this.cumerate = cumerate;
	}

	public int getCumcount() {
		return cumcount;
	}

	public void setCumcount(int cumcount) {
		this.cumcount = cumcount;
	}

	public int getLargecount() {
		return largecount;
	}

	public void setLargecount(int largecount) {
		this.largecount = largecount;
	}

	public Date getLastHarvest() {
		return lastHarvest;
	}

	public void setLastHarvest(Date lastHarvest) {
		this.lastHarvest = lastHarvest;
	}

	public int getSpeedCount() {
		return speedCount;
	}

	public void setSpeedCount(int speedCount) {
		this.speedCount = speedCount;
	}

	public int addSpeedCount(int s) {
		this.speedCount += s;
		return this.speedCount;
	}

	public int getLastFlushDay() {
		return lastFlushDay;
	}

	public void setLastFlushDay(int lastFlushDay) {
		this.lastFlushDay = lastFlushDay;
	}

	/**
	 * 首次创建；
	 *
	 * @param _id 玩家id
	 * @return
	 */
	public static SceneBean createNewScene(Long _id) {
		SceneBean sb = new SceneBean();
		sb.id = _id;
		sb.energy = 120;
		sb.scene = 1;
		sb.lastHarvest = new Date();

		return sb;
	}

	/**
	 * 更新 能量 活力
	 * @param _extra
	 */
	public int updateEnergy(int _extra) {
		long now = System.currentTimeMillis();
		long diff = now - lastEnergy;
		while (diff >= 60000) {
			lastEnergy += 60000;
			energy += 1;
			diff -= 60000;
		}

		energy += _extra;

		if (energy > 120) {
			energy = 120;
		}
		return energy;
	}

	public void flush() {
		updateEnergy(0);

		int today = TimeUtil.getToday();
		if (today > lastFlushDay) {
			lastFlushDay = today;
			speedCount = 0;
		}
	}

	/**
	 * 进行一次收割
	 */
	public void harvest(GameUser user) {
		Date now = new Date();
		int diff = (int)((now.getTime() - lastHarvest.getTime()) / 1000);

		lastHarvest = now;
		cumerate += diff;

		RaidConf rb = Readonly.getInstance().findRaid(scene);
		if (rb != null) {
			while (cumerate >= rb.cd) {
				boolean trigger = false;
				cumerate -= rb.cd;
				long gold = (long)(rb.gold / 3600f * rb.cd);
				long exp = (long)(rb.exp / 3600f * rb.cd);

				// 满11个事件
				if (++cumcount >= 11) {
					int idx = RandomUtil.getRandom() % rb.drop.length;
					int treasure = rb.drop[idx];

					trigger = true;
					// 触发大事件，战胜后，发送大奖励
					user.triggerSceneBigEvent(rb, treasure, gold, exp);
					++largecount;
					cumcount -= 11;
				}

				// 如果大事件也触发了，则不要在制造小事件战斗过程了
				if (!trigger) {
					user.triggerSceneEvent(rb, (int)(rb.gold / 3600f * rb.cd), (long)(rb.exp / 3600f * rb.cd));
				}
			}
		}
	}

	/**
	 * 离线奖励，或者快速时间奖励
	 *
	 * @param user
	 * @param bean
	 * @param extra 大于0是购买的战斗时间
	 */
	public void harvest(GameUser user, OfflineAwardStruct bean, long extra) {
		Date now = new Date();
		int diff = (int) (now.getTime() - lastHarvest.getTime() + extra) / 1000;

		bean.period = diff;

		// 根据vip获取可离线挂机的最大收益时间
		VipConf vc = Readonly.getInstance().findVip(user.getVip());
		if (diff > vc.time) {
			diff = vc.time;
		}

		lastHarvest = now;
		cumerate += diff;
		bean.scene = scene;

		RaidConf rb = Readonly.getInstance().findRaid(scene);
		if (rb != null) {
			while (cumerate >= rb.cd) {
				// 触发小事件
				bean.gold += (long) (rb.gold / 3600f * rb.cd);
				bean.exp += (int) (rb.exp / 3600f * rb.cd);
				++bean.events;

				cumerate -= rb.cd;

				// 满11个事件
				if (++cumcount >= 11) {
					// 触发大事件，发送大奖励
					int idx = RandomUtil.getRandom() % rb.drop.length;
					int treasure = rb.drop[idx];

					// 随机宝箱获得物品
					bean.awards.addAll(user.addItem(treasure, 1, true, "离线挂机"));
					++bean.boss;
					++largecount;
					cumcount -= 11;
				}

				// 节日活动期间可以有新的东西出现
				FestivalBean fb = DaoGame.getInstance().findFestival(this.id);
				if (fb.getMode() > 0 && fb.getFlag() == 0) {
					int[] b1 = fb.getB1();
					int rr = RandomUtil.getBetween(1, 100);
					if (b1.length > 0 && rr <= b1[0]) {
						int c = RandomUtil.getBetween(b1[2], b1[3]);
						bean.awards.addAll(user.addItem(b1[1], c, false, "挂机节日"));
					}
				}
			}

			if (bean.events > 0) {
				user.addItem(金币, bean.gold, false, "离线挂机");
				user.addLordExp(bean.exp);
			}
		}
	}


	/**
	 * 快速战斗n秒
	 *
	 * @param user
	 * @param diff 快速战斗时间
	 *
	 */
	public void harvest(GameUser user, int diff, RequestSceneFast.ReturnSceneFast sf) {
		int gold = 0;
		int exp = 0;
		int cc = cumcount;

		FestivalBean fb = DaoGame.getInstance().findFestival(this.id);

		RaidConf rb = Readonly.getInstance().findRaid(scene);
		if (rb != null) {
			while (diff >= rb.cd) {
				// 触发小事件
				gold += (int) (rb.gold / 3600f * rb.cd);
				exp += (int) (rb.exp / 3600f * rb.cd);
				++sf.event;

				diff -= rb.cd;
				if (fb.getMode() > 0 && fb.getFlag() == 0) {
					int[] b1 = fb.getB1();
					int rr = RandomUtil.getBetween(1, 100);
					if (b1.length > 0 && rr <= b1[0]) {
						int c = RandomUtil.getBetween(b1[2], b1[3]);
						sf.awards.addAll(user.addItem(b1[1], c, false, "快速节日"));
					}
				}

				// 满11个事件
				if (++cc >= 11) {
					// 触发大事件，发送大奖励
					int idx = RandomUtil.getRandom() % rb.drop.length;
					int treasure = rb.drop[idx];

					// 随机宝箱获得物品
					sf.awards.addAll(user.addItem(treasure, 1, true, "快速战斗事件"));
					++sf.special;
					cc -= 11;
				}
			}

			if (gold > 0) {
				sf.awards.addAll(user.addItem(金币, gold, false, "快速战斗"));
			}
			if (exp > 0) {
				user.addLordExp(exp);
				sf.awards.add(new AwardBean(经验, exp, 0));
			}
		}
	}

}
