package com.tumei.model;

import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameUser;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.war.WarTask;
import com.tumei.modelconf.MissionConf;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 *     战争学院
 *
 *     常驻任务：
 *     	1. 日常任务，每日刷出有次数限制
 *     	2. 普通任务
 *
 *     限时任务:
 *     	1. 晚上十点到第二天3点刷出的任务
 *     	2. 加速副本随机出现的任务
 *
 *
 *
 *
 *
 * <p>
 */
@Data
@Document(collection = "Role.War")
public class WarBean {
	@Id
	private String objectId;

	@Field("id")
	@Indexed(unique = true, name = "i_id")
	private Long id;

	/**
	 * 上次刷新的日期,用于判断当前是否要刷新日常次数
	 */
	private int day;

	/**
	 * 日常任务今日已经刷出来的个数，上限为16条
	 */
	private int dailyCount;

	/**
	 * 给任务进行递增编号, 否则难以区分任务
	 */
	private int maxid;

	/**
	 * 当前获得的所有任务
	 */
	private HashMap<Integer, WarTask> tasks = new HashMap<>();


	public WarBean() {}

	public WarBean(long _id) {
		id = _id;
	}

	/**
	 * 刷新的时候必须知道当前领主等级
	 *
	 * @param level
	 */
	public void flush(int level) {
		int today = TimeUtil.getToday();
		if (today != day) {
			day = today;

			dailyCount = 16;
			flushConstTasks(level, true);
		}

		int yejian = 0; // 夜间任务个数

		long now = System.currentTimeMillis() / 1000;
		// 刷新限时任务
		List<Integer> deletes = new ArrayList<>();
		for (int tid : tasks.keySet()) {
			WarTask wt = tasks.getOrDefault(tid, null);
			if (wt != null) {
				if (wt.mode > 2) {
					// 没有开始，但是超期的限时任务直接删除
				    if (wt.complete <= 0 && now >= wt.expire) {
				        deletes.add(tid);
					} else if (wt.mode == 3){
				        ++yejian; // 统计夜间任务个数
					}
				}
			}
		}
		for (int tid : deletes) {
			tasks.remove(tid);
		}

		if (yejian <= 0) {
			LocalDateTime ldt = LocalDateTime.now();
			long expire = 0;
			if (ldt.getHour() >= 22) {
                expire = LocalDateTime.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth(), 3, 0).plusDays(1).toEpochSecond(ZoneOffset.ofHours(8));
			} else if (ldt.getHour() < 3) {
				expire = LocalDateTime.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth(), 3, 0).toEpochSecond(ZoneOffset.ofHours(8));
			}

			// 增加四个夜间任务，开始时间都是3点结束
			if (expire > 0) {
				List<MissionConf> mcs = Readonly.getInstance().getMissions().stream().filter(mc -> (mc.mode == 3 && mc.level <= level)).collect(Collectors.toList());

				for (int i = 0; i < 4; ++i) {
					int idx = RandomUtil.getRandom() % mcs.size();
					MissionConf mc = mcs.get(idx);
					WarTask task = new WarTask();
					task.tid = ++maxid;
					task.task = mc.key;
					task.mode = mc.mode;
					task.expire = expire;
					tasks.put(task.tid, task);
				}
			}
		}
	}

	/**
	 *
	 * 生成紧急任务
	 */
	public void generateEmergy(int level) {
		List<MissionConf> mcs = Readonly.getInstance().getMissions().stream().filter(mc -> (mc.mode == 4 && mc.level <= level)).collect(Collectors.toList());

		if (mcs.size() > 0) {
			int idx = RandomUtil.getRandom() % mcs.size();
			MissionConf mc = mcs.get(idx);
			WarTask task = new WarTask();
			task.tid = ++maxid;
			task.task = mc.key;
			task.mode = mc.mode;
			task.expire = System.currentTimeMillis()/1000 + mc.last;
			tasks.put(task.tid, task);
		}
	}

	public int runningTask() {
		return (int)tasks.values().stream().filter(t -> t.complete > 0).count();
	}

	/**
	 * 刷新常驻任务
	 */
	public void flushConstTasks(int level, boolean nextDay) {
	    if (nextDay) { // 跨天, 所有常驻任务中没有开始的，都要重新刷新为新的日常任务
	        List<Integer> deletes = new ArrayList<>();
            for (int tid : tasks.keySet()) {
            	WarTask wt = tasks.getOrDefault(tid, null);
				if (wt != null) {
					if (wt.mode <= 2) {
						deletes.add(tid);
					}
				}
			}

			for (int tid : deletes) {
				tasks.remove(tid);
			}

		}

		generateConstTasks(level);
	}

	/**
	 * 生成一定数量的日常任务, 补齐4个
	 * 超过4个常驻任务数量就不会生成
	 *
	 */
	private void generateConstTasks(int level) {
	    // 如果常驻任务个数小于4个，才会继续生成
		long n = 4 - tasks.values().stream().filter(t -> t.mode <= 2).count();
		if (n > 0) {

			// 检查需要生成多少个日常，多少个普通
			long a = n;
			long b = 0;
            if (n > dailyCount) {
                a = dailyCount;
                b = n - dailyCount;
			}

			// 日常
			if (a > 0) {
				List<MissionConf> mcs = Readonly.getInstance().getMissions().stream().filter(mc -> (mc.mode == 1 && mc.level <= level)).collect(Collectors.toList());
				for (int i = 0; i < a; ++i) {
					int idx = RandomUtil.getRandom() % mcs.size();
					MissionConf mc = mcs.get(idx);
					WarTask task = new WarTask();
					task.tid = ++maxid;
					task.task = mc.key;
					task.mode = mc.mode;
					tasks.put(task.tid, task);
				}
				dailyCount -= a;
			}
			// 普通
			if (b > 0) {
				List<MissionConf> mcs = Readonly.getInstance().getMissions().stream().filter(mc -> (mc.mode == 2 && mc.level <= level)).collect(Collectors.toList());
				for (int i = 0; i < b; ++i) {
					int idx = RandomUtil.getRandom() % mcs.size();
					MissionConf mc = mcs.get(idx);
					WarTask task = new WarTask();
					task.tid = ++maxid;
					task.task = mc.key;
					task.mode = mc.mode;
					tasks.put(task.tid, task);
				}
			}
		}
	}
}
