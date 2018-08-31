package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.DaoService;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.game.services.TreasureRankService;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.DailytreasureConf;
import com.tumei.common.Readonly;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 锤子敲宝箱
 *
 */
@Document(collection = "Role.Treasure")
public class TreasureBean {
	@JsonIgnore
	@Id
	private String objectId;

	@Field("id")
	private Long id;


	/**
	 * 每天刷新
	 */
	private int flushDay;

	/**
	 * 周刷新日期
	 */
	private int flushWeekDay;

	/**
	 * 今日挖掘的次数, 免费3次
	 */
	private int digCount;

	/**
	 * 今日使用刷新的次数 免费10次
	 */
	private int flushCount;

	/**
	 * 当前宝箱的类型
	 * 1. 铜
	 * 2. 银
	 * 3. 金
	 */
	private int current;

	/**
	 * 当前的积分总数
	 */
	private int score;

	/**
	 * 上次挖掘而没有领取的奖励
	 */
	private List<AwardBean> awards;

	/**
	 * 未领取的箱子价格
	 */
	private int doubleBox;

	public TreasureBean() {}

	public TreasureBean(long id) {
		this.id = id;
	}

	public void flush() {
		// 周一 刷新积分
		LocalDate ld = LocalDate.now();
		if (ld.getDayOfWeek().getValue() == 1) {
			int week = TimeUtil.getWeekDay();
			if (week != flushWeekDay) {
				flushWeekDay = week;
				score = 0;
			}
		}

		int today = TimeUtil.getToday();
		if (today != flushDay) {
			flushDay = today;

			digCount = 0;
			flushCount = 0;
		}

		if (current == 0) {
			changeTreasure(0);
		}
	}

	public void changeTreasure(int mode) {
		List<DailytreasureConf> dcs = Readonly.getInstance().getDailytreasureConf();

		if (mode == 0) { // 挖掘刷新宝箱的机率
			int r = RandomUtil.getRandom() % 100;
			int total = 0;
			for (DailytreasureConf dc : dcs) {
				total += dc.rate1;
				if (r <= total) {
					current = dc.key;
					break;
				}
			}
		} else { // 开箱子出宝箱的机率
			int r = RandomUtil.getRandom() % 100;
			int total = 0;
			for (DailytreasureConf dc : dcs) {
				total += dc.rate2;
				if (r <= total) {
					current = dc.key;
					break;
				}
			}
		}
	}

	/**
	 * 挖掘宝箱
	 * @return
	 */
	public List<Integer> dig() {
		List<Integer> awards = new ArrayList<>();

		DailytreasureConf dc = Readonly.getInstance().findDailytreasureConf(current);
		int count = RandomUtil.randomWeightIndex(dc.num) + 1;

		for (int i = 0; i < count; ++i) {
			awards.add(RandomUtil.randomByWeight(dc.reward));
			awards.add(1);
		}

		++digCount;
		score += dc.score;

		RoleBean rb = DaoService.getInstance().findRole(this.id);
		TreasureRankService.getInstance().fixRank(this.id, rb.getNickname(), score);

		// 开箱后出箱
		changeTreasure(1);
		return awards;
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

	public int getFlushDay() {
		return flushDay;
	}

	public void setFlushDay(int flushDay) {
		this.flushDay = flushDay;
	}

	public int getFlushWeekDay() {
		return flushWeekDay;
	}

	public void setFlushWeekDay(int flushWeekDay) {
		this.flushWeekDay = flushWeekDay;
	}

	public int getDigCount() {
		return digCount;
	}

	public void setDigCount(int digCount) {
		this.digCount = digCount;
	}

	public int getFlushCount() {
		return flushCount;
	}

	public void setFlushCount(int flushCount) {
		this.flushCount = flushCount;
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public List<AwardBean> getAwards() {
		return awards;
	}

	public void setAwards(List<AwardBean> awards) {
		this.awards = awards;
	}

	public int getDoubleBox() {
		return doubleBox;
	}

	public void setDoubleBox(int doubleBox) {
		this.doubleBox = doubleBox;
	}
}
