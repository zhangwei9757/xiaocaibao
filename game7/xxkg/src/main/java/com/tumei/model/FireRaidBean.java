package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.protos.structs.ChoiceStruct;
import com.tumei.modelconf.FireraidConf;
import com.tumei.common.Readonly;
import com.tumei.common.utils.TimeUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 召唤英雄相关的记录
 *
 */
@Document(collection = "Role.FireRaid")
public class FireRaidBean {
	public FireRaidBean() {}

	public FireRaidBean(long _id) {
		id = _id;
	}

	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private Long id;

	/**
	 * 是否失败
	 */
	private boolean failed;

	/**
	 * 重置日期，重置的时候只重置次数，状态不变
	 */
	private int lastFlushDay;

	/**
	 * 今日已经重置的次数
	 */
	private int resetCount;
	/**
	 * 当前挑战的关卡，从1开始，默认没有挑战任何一关
	 */
	private int scene = 1;
	/**
	 * 最高挑战的总星数
	 */
	private int peekStars;
	/**
	 * 当前挑战的总星数
	 */
	private int totalStars;
	/**
	 * 剩余未使用的星数
	 */
	private int leftStars;
	/**
	 * 最高的三星关卡，则可以一键挑战到这个关卡所在的章节
	 */
	private int peek3Stars;
	/**
	 * 当前的总加成
	 */
	private HashMap<Integer, Integer> buffs = new HashMap<>();

	/**
	 * 如果不为空，表示玩家还没有做出选择，需要返回页面让其选择
	 * 如果当前有没有选择的buff,则展示出来，里面一共有三组数据
	 * key表示属性，value是值，三对数据分为3星，6星，9星
	 */
	public List<ChoiceStruct> choise = new ArrayList<>();

	/**
	 * 每关对应的星数
	 */
	private List<Integer> stars = new ArrayList<>();

	/**
	 * 更新对应关卡的星数, 大于以前的星数才能更新
	 * @param scene [1,...]
	 * @param star
	 */
	public void upateStart(int scene, int star) {
		if (scene > stars.size()) {
			stars.add(star);
		}

		int old = stars.get(scene - 1);
		if (star > old) {
			stars.set(scene - 1, star);
		}
	}

	public List<Integer> getStars() {
		return stars;
	}

	public void setStars(List<Integer> stars) {
		this.stars = stars;
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

	public int getLastFlushDay() {
		return lastFlushDay;
	}

	public void setLastFlushDay(int lastFlushDay) {
		this.lastFlushDay = lastFlushDay;
	}

	public int getResetCount() {
		return resetCount;
	}

	public void setResetCount(int resetCount) {
		this.resetCount = resetCount;
	}

	public int getScene() {
		return scene;
	}

	public void setScene(int scene) {
		this.scene = scene;
	}

	public int getPeekStars() {
		return peekStars;
	}

	public void setPeekStars(int peekStars) {
		this.peekStars = peekStars;
	}

	public int getTotalStars() {
		return totalStars;
	}

	public void setTotalStars(int totalStars) {
		this.totalStars = totalStars;
	}

	public int getPeek3Stars() {
		return peek3Stars;
	}

	public void setPeek3Stars(int peek3Stars) {
		this.peek3Stars = peek3Stars;
	}

	public HashMap<Integer, Integer> getBuffs() {
		return buffs;
	}

	public void setBuffs(HashMap<Integer, Integer> buffs) {
		this.buffs = buffs;
	}

	public int getLeftStars() {
		return leftStars;
	}

	public void setLeftStars(int leftStars) {
		this.leftStars = leftStars;
	}

	/**
	 * 检查是否需要刷新重置次数
	 */
	public void flush() {
		int today = TimeUtil.getToday();
		if (today != lastFlushDay) {
			lastFlushDay = today;
			resetCount = 0;
		}
	}

	/**
	 * 重置
	 *
	 * @return
	 */
	public boolean reset() {
		++resetCount;
		totalStars = 0;
		scene = 1;
		leftStars = 0;
		buffs.clear();
		stars.clear();
		choise.clear();
		failed = false;
		return true;
	}

	public List<ChoiceStruct> getChoise() {
		return choise;
	}

	public void setChoise(List<ChoiceStruct> choise) {
		this.choise = choise;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	/**
	 * 制作随机选择
	 * @return
	 */
	public List<ChoiceStruct> makeChoice() {
		choise.clear();

		FireraidConf frc = Readonly.getInstance().findFireraid(1);
		for (int i = 0; i < 3; ++i) {
			int index = RandomUtil.getRandom() % (frc.buff[i].length / 2);
			int key = frc.buff[i][index * 2];
			int val = frc.buff[i][index * 2 + 1];
			ChoiceStruct cs = new ChoiceStruct(key, val);
			choise.add(cs);
		}

		return choise;
	}
}


