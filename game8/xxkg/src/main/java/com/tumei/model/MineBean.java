package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.modelconf.MineStoneConf;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 * 在地图上的矿脉信息
 */
@Document(collection = "All.mines")
public class MineBean {
	@JsonIgnore
	@Id
	public String id;

	// 矿区坐标位置
	public int pos;

	/**
	 * 矿产配置id
	 */
	public int key;
	/**
	 * 随机出来的矿产 三种产出对应的序号，从0开始，比如配置中good1随机出的第一个 就是0 第二个就是1
	 */
	public int[] goods = new int[3];
	/**
	 * 占领时间 秒
	 */
	public long occupyTime;
	/**
	 * 占领者
	 */
	public long uid;
	/**
	 * 占领者姓名
	 */
	public String name;
	/**
	 * 占领者的头像
	 */
	public int skin;
	/**
	 * 占领者 等级
	 */
	public int level;

	/**
	 * 占领者 等级
	 */
	public long power;

	/**
	 * 延长时间
	 */
	public int enhance;

	/**
	 * 标识本矿已经被采集的时间长度
	 */
	public int used;

	public MineBean() {
	}

	public MineBean(int _pos, int _level) {
		this.pos = _pos;
		this.level = _level;

		MineStoneConf mcc = Readonly.getInstance().getMineStoneConfs().get(_level - 1);
		this.key = mcc.key;

		// 对应三个产出，不同的奖励
		goods[0] = RandomUtil.getRandom() % (mcc.good1.length / 2);
		goods[1] = RandomUtil.getRandom() % (mcc.good2.length / 2);
		goods[2] = RandomUtil.getRandom() % (mcc.good3.length / 2);
	}

	public MineBean(MineBean other) {
		this.pos = other.pos;
		this.key = other.key;
		this.goods = other.goods;
		this.occupyTime = other.occupyTime;
		this.uid = other.uid;
		this.skin = other.skin;
		this.level = other.level;
		this.name = other.name;
		this.power = other.power;
		this.enhance = other.enhance;
		this.used = other.used;
	}

	public int needToDefende(MineStoneConf msc) {
		return msc.time + enhance - used;
	}

	public int accelerate() {
		long now = System.currentTimeMillis() / 1000;

		// 1. 查看当前是否可以收获
		MineStoneConf msc = Readonly.getInstance().getMineStoneConfs().get(key - 1);
		if (msc != null) {
			// 计算矿需要占领的时间 = 矿的原本时间 + 延长时间 - 已经使用的时间
			int need = needToDefende(msc);
			int duration = (int) (now - occupyTime);

			int left = (need - duration);
			if (left <= 0) {
				return 0;
			}
			else {
				return (left / 3600) + 1;
			}
		}
		return 0;
	}


	@Override
	public String toString() {
		return "MineBean{" + "id='" + id + '\'' + ", pos=" + pos + ", key=" + key + ", goods=" + Arrays.toString(goods) + ", occupyTime=" + occupyTime + ", uid=" + uid + ", name='" + name + '\'' + ", skin=" + skin + ", level=" + level + ", power=" + power + ", enhance=" + enhance + ", used=" + used + '}';
	}
}
