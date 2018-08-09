package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.utils.TimeUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Role.Group")
public class GroupBean {
	@JsonIgnore
	@Id
	private String ObjectId;
	@Field("id")
	private Long id;
	// 所在公会id
	private long gid;

	private String name = "";

	// 刷新日期
	private int flushDay;

	// 下次可以加入，创建的公会时间
	private Date leaveDay;

	// 公会捐献次数
	private int donate;

	// 公会等级，缓存的，根据天来变化 不是实际公会等级
	private int level;

	// 公会进度
	private int progress;

	// 4个捐献进度是否领取
	private int[] marks = new int[4];

	// 当前副本攻击的章节
	private int scene;

	// 副本奖励领取的关卡
	private Set<Integer> sceneAward = new HashSet<>();

	// 当前副本章节中的4个阵营的奖励是否领取
	private int[] fetch = new int[4];

	// 公会副本可攻击次数
	private int sceneCount;

	private int buyCount;

	// 下次恢复的时间
	private Date sceneTime = new Date();

	public GroupBean() {
	}

	public GroupBean(long _id) {
		id = _id;
	}

	public int getScene() {
		return scene;
	}

	public void setScene(int scene) {
		this.scene = scene;
	}

	public int[] getFetch() {
		return fetch;
	}

	public void setFetch(int[] fetch) {
		this.fetch = fetch;
	}

	public int getBuyCount() {
		return buyCount;
	}

	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
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

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		if (this.level == 0) {
			this.level = level;
		}
	}

	public Set<Integer> getSceneAward() {
		return sceneAward;
	}

	public void setSceneAward(Set<Integer> sceneAward) {
		this.sceneAward = sceneAward;
	}

	public synchronized long getGid() {
		return gid;
	}

	public synchronized void setGid(long gid) {
		this.gid = gid;
	}

	public int getFlushDay() {
		return flushDay;
	}

	public void setFlushDay(int flushDay) {
		this.flushDay = flushDay;
	}

	public Date getLeaveDay() {
		return leaveDay;
	}

	public void setLeaveDay(Date leaveDay) {
		this.leaveDay = leaveDay;
	}

	public int getDonate() {
		return donate;
	}

	public void setDonate(int donate) {
		this.donate = donate;
	}

	public int[] getMarks() {
		return marks;
	}

	public void setMarks(int[] marks) {
		this.marks = marks;
	}

	public int getSceneCount() {
		return sceneCount;
	}

	public void setSceneCount(int sceneCount) {
		this.sceneCount = sceneCount;
	}

	public Date getSceneTime() {
		return sceneTime;
	}

	public void setSceneTime(Date sceneTime) {
		this.sceneTime = sceneTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 刷新
	 * 1. 捐献次数
	 * 2. 暂存的公会等级与进度，主要是公会等级在一天之内，只记录当前第一次看到的
	 * 3. 公会副本次数，凌晨刷新到3次，从上午12点开始才能增长，实际是10点开始增长，12点第一次涨1点，可以累计。
	 * 4. 公会副本下次恢复1点的时间，每次调用刷新都会根据间隔的时间进行计算，恢复不止1点，无上限，直到第二天清空。
	 *
	 * @return
	 */
	public boolean flush(int vip) {
		int today = TimeUtil.getToday();
		if (today != flushDay) {
			flushDay = today;
			donate = 0; // 重置公会捐献次数
			for (int i= 0; i < 4; ++i) {
				marks[i] = 0;
				fetch[i] = 0;
			}

			level = 0;
			progress = 0;
			//VipConf vc = Readonly.getInstance().findVip(vip);
			sceneCount = 3;
			buyCount = 0;
			LocalDateTime now = LocalDateTime.now();
			sceneTime = new GregorianCalendar(now.getYear(), now.getMonth().getValue() - 1,
				now.getDayOfMonth(), 12, 0, 0).getTime();
			return true;
		} else {
			long now = System.currentTimeMillis();
			long lt = sceneTime.getTime();
			while (lt <= now) {
				lt += 7200000;
				sceneTime = new Date(lt);
				++sceneCount;
			}
		}
		return false;
	}
}
