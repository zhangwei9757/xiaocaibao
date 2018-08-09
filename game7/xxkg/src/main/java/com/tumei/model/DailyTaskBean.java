package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.model.beans.TaskItemBean;
import com.tumei.modelconf.DailyConf;
import com.tumei.common.Readonly;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Role.Tasks")
public class DailyTaskBean {
	@JsonIgnore
	@Id
	private String ObjectId;

	@JsonIgnore
	@Field("id")
	private Long id;

	/**
	 * 当前积分
	 */
	private int score;

	/**
	 * 0表示未领取，[1-4]表示当前领取进度
	 */
	private int scoreAwardProgress = 0;

	@JsonIgnore
	private int lastFlushDay;

	/**
	 * 日常任务
	 */
	private List<TaskItemBean> tasks = new ArrayList<>();

	public DailyTaskBean() {}

	public DailyTaskBean(long id) {
		this.id = id;
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

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getScoreAwardProgress() {
		return scoreAwardProgress;
	}

	public void setScoreAwardProgress(int scoreAwardProgress) {
		this.scoreAwardProgress = scoreAwardProgress;
	}

	public List<TaskItemBean> getTasks() {
		return tasks;
	}

	public void setTasks(List<TaskItemBean> tasks) {
		this.tasks = tasks;
	}

	public int getLastFlushDay() {
		return lastFlushDay;
	}

	public void setLastFlushDay(int lastFlushDay) {
		this.lastFlushDay = lastFlushDay;
	}

	/**
	 * 根据vip等级重新更新当前的任务状态
	 * @param vip
	 */
	public void flush(int vip) {
		tasks.clear();

		List<DailyConf> dcs = Readonly.getInstance().getDailytasks();
		for (DailyConf dc : dcs) {
			TaskItemBean tib = new TaskItemBean();
			tib.tid = dc.key;
			tib.limit = dc.time;
			tasks.add(tib);
		}

		setScore(0);
		setScoreAwardProgress(0);
	}

	/**
	 * 完成任务属性一点
	 *
	 * @param tid [1,..]
	 * @return 是否任务完全完成
	 *
	 */
	public boolean step(int tid, int val) {
		TaskItemBean tib = tasks.get(tid-1);
		if (tib != null && tib.status == 0) {
			tib.progress += val;
			if (tib.progress >= tib.limit) {
				tib.status = 1;
				return true;
			}
		}
		return false;
	}
}
