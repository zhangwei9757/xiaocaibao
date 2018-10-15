package com.tumei.model.beans;

import com.tumei.modelconf.StarConf;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;

/**
 * Created by Administrator on 2017/3/29 0029.
 * <p>
 * 一条占星台挂机信息
 */
public class StarHeroFragsBean {
	/**
	 * [1,13]
	 */
	public int index;
	/**
	 * 3个英雄的碎片
	 */
	public HeroFragBean[] heros = new HeroFragBean[3];
	/**
	 * 挂机结束时间
	 */
	public long elapse;
	/**
	 * 0: 6个小时
	 * 1: 8
	 * 2: 10
	 */
	public int mode;
	/**
	 * 0: 未选择
	 * 1: 已选择
	 * 2: 待领取
	 * <p>
	 * 领取之后又进入状态0
	 */
	public int status;

	public StarHeroFragsBean() {
	}

	public StarHeroFragsBean(int _idx) {
		index = _idx;
		update(false);
	}

	/**
	 * 选择
	 *
	 * @return
	 */
	public boolean select(int _mode) {
		if (status != 0) {
			return false;
		}

		status = 1;
		mode = _mode;
		update(true);
		return true;
	}

	public void update(boolean needCount) {
		StarConf sc = Readonly.getInstance().findStars().get(index - 1);
		int[][] hours;
		long secs = 36000;
		switch (mode) {
			case 0:
				hours = sc.hour6;
				secs = sc.cost[0][0];
				break;
			case 1:
				hours = sc.hour8;
				secs = sc.cost[1][0];
				break;
			case 2:
				hours = sc.hour10;
				secs = sc.cost[2][0];
				break;
			default:
				hours = sc.hour10;
				break;
		}

		for (int i = 0; i < 3; ++i) {
			HeroFragBean hfb = new HeroFragBean();
			hfb.hero = hours[i][0];

			if (needCount) {
				hfb.count = (RandomUtil.getRandom() % (hours[i][2] - hours[i][1])) + hours[i][1];
			}
			heros[i] = hfb;
		}

		if (needCount) {
			elapse = System.currentTimeMillis() / 1000 + secs;
		}
	}

	/**
	 * 检查当前状态
	 * @param now
	 */
	public void check(long now) {
		if (status == 1) {
			if (now >= elapse) {
				status = 2;
			}
		}
	}
}
