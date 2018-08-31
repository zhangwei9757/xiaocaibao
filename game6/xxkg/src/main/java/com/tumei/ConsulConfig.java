package com.tumei;

import com.tumei.common.service.RouterService;
import com.tumei.common.utils.JsonUtil;

import java.util.Arrays;

/**
 * Created by Leon on 2017/9/5 0005.
 */
public class ConsulConfig {
	/**
	 * 单冲周期
	 */
	private int singlePeriod = 3;
	/**
	 * 累充周期
	 */
	private int cumPeriod = 7;
	/**
	 * 打折周期
	 */
	private int salePeriod = 5;

	/**
	 * 夺宝周期
	 */
	private int dbPeriod = 14;

	/**
	 * 个人统计事件长度
	 */
	private int staCount = 1000;

	/**
	 * 缓存读取，间隔1秒回盘
	 */
	private int access_delay = 1000;

	/**
	 * 缓存写入，间隔1秒回盘
	 */
	private int write_delay = 1000;

	/**
	 * 快速战斗恢复的能量
	 */
	private int scene_fast_recover = 60;

	/**
	 * 快速战斗模拟的收获时间 7200
	 */
	private int scene_fast_time = 7200;


	/**
	 * 好友个数
	 */
	private int friend_count = 30;

	/**
	 * 申请好友审批表中的暂存数量
	 */
	private int friend_ready_count = 30;

	/**
	 * 一天内好友送活力次数
	 */
	private int friend_send = 30;

	/**
	 * 一天内好友收活力次数
	 */
	private int friend_recv = 30;

	/**
	 * 玩家改名需要的钻石
	 */
	private int user_chagnename_gem = 500;

	/**
	 * 人物活力自然恢复最高达到30点
	 */
	private int user_spirit_max = 30;

	/**
	 * 活力 30分钟回复1点
	 */
	private int sp_recover_interval = 1800;

	/**
	 * 神秘宝藏一天内挖掘的最高次数
	 */
	private int treasure_dig_limit = 53;

	private boolean mine_open = true;

	/**
	 * 矿区内能量的顶值
	 */
	private int mine_energy_max = 100;

	/**
	 * 多久回复能量
	 * 600
	 */
	private int mine_energy_recover = 1;

	/**
	 * 每次回复能量的点数
	 */
	private int mine_energy_recover_count = 1;

	/**
	 * 每一步行动消耗的能量
	 */
	private int mine_consume = 3;

	/**
	 * 地图视野半径
	 */
	private int mine_view = 8;

	/**
	 * 每次购买矿区体力需要的钻石
	 */
	private int[] mine_buyenergy_cost = new int[]{50, 50, 50, 100, 100, 100, 200, 200, 200, 400, 400, 400, 600, 600, 600, 600};

	/**
	 * 矿区购买行动力，一次获得的行动力
	 */
	private int mine_buyenergy_count = 50;

	public ConsulConfig() {}

	/**
	 * 重新加载
	 *
	 * @param rs
	 * @param key
	 * @return
	 */
	public static ConsulConfig reload(RouterService rs, String key) {
		String data = rs.getKeyValues(key);
		try {
			return JsonUtil.Unmarshal(data, ConsulConfig.class);
		} catch (Exception e) {
			return new ConsulConfig();
		}
	}

	public int getAccess_delay() {
		return access_delay;
	}

	public void setAccess_delay(int access_delay) {
		this.access_delay = access_delay;
	}

	public int getWrite_delay() {
		return write_delay;
	}

	public void setWrite_delay(int write_delay) {
		this.write_delay = write_delay;
	}

	public int getScene_fast_recover() {
		return scene_fast_recover;
	}

	public void setScene_fast_recover(int scene_fast_recover) {
		this.scene_fast_recover = scene_fast_recover;
	}

	public int getScene_fast_time() {
		return scene_fast_time;
	}

	public void setScene_fast_time(int scene_fast_time) {
		this.scene_fast_time = scene_fast_time;
	}

	public int getFriend_count() {
		return friend_count;
	}

	public void setFriend_count(int friend_count) {
		this.friend_count = friend_count;
	}

	public int getFriend_ready_count() {
		return friend_ready_count;
	}

	public void setFriend_ready_count(int friend_ready_count) {
		this.friend_ready_count = friend_ready_count;
	}

	public int getFriend_send() {
		return friend_send;
	}

	public void setFriend_send(int friend_send) {
		this.friend_send = friend_send;
	}

	public int getFriend_recv() {
		return friend_recv;
	}

	public void setFriend_recv(int friend_recv) {
		this.friend_recv = friend_recv;
	}

	public int getUser_chagnename_gem() {
		return user_chagnename_gem;
	}

	public void setUser_chagnename_gem(int user_chagnename_gem) {
		this.user_chagnename_gem = user_chagnename_gem;
	}

	public int getUser_spirit_max() {
		return user_spirit_max;
	}

	public void setUser_spirit_max(int user_spirit_max) {
		this.user_spirit_max = user_spirit_max;
	}

	public int getSp_recover_interval() {
		return sp_recover_interval;
	}

	public void setSp_recover_interval(int sp_recover_interval) {
		this.sp_recover_interval = sp_recover_interval;
	}

	public int getTreasure_dig_limit() {
		return treasure_dig_limit;
	}

	public void setTreasure_dig_limit(int treasure_dig_limit) {
		this.treasure_dig_limit = treasure_dig_limit;
	}

	public int getMine_energy_max() {
		return mine_energy_max;
	}

	public void setMine_energy_max(int mine_energy_max) {
		this.mine_energy_max = mine_energy_max;
	}

	public int getMine_energy_recover() {
		return mine_energy_recover;
	}

	public void setMine_energy_recover(int mine_energy_recover) {
		this.mine_energy_recover = mine_energy_recover;
	}

	public int getMine_energy_recover_count() {
		return mine_energy_recover_count;
	}

	public void setMine_energy_recover_count(int mine_energy_recover_count) {
		this.mine_energy_recover_count = mine_energy_recover_count;
	}

	public int getMine_consume() {
		return mine_consume;
	}

	public void setMine_consume(int mine_consume) {
		this.mine_consume = mine_consume;
	}

	public int getMine_view() {
		return mine_view;
	}

	public void setMine_view(int mine_view) {
		this.mine_view = mine_view;
	}

	public int[] getMine_buyenergy_cost() {
		return mine_buyenergy_cost;
	}

	public void setMine_buyenergy_cost(int[] mine_buyenergy_cost) {
		this.mine_buyenergy_cost = mine_buyenergy_cost;
	}

	public int getMine_buyenergy_count() {
		return mine_buyenergy_count;
	}

	public void setMine_buyenergy_count(int mine_buyenergy_count) {
		this.mine_buyenergy_count = mine_buyenergy_count;
	}

	public boolean getMine_open() {
		return mine_open;
	}

	public void setMine_open(boolean mine_open) {
		this.mine_open = mine_open;
	}

	public int getStaCount() {
		return staCount;
	}

	public void setStaCount(int staCount) {
		this.staCount = staCount;
	}

	public int getSinglePeriod() {
		return singlePeriod;
	}

	public void setSinglePeriod(int singlePeriod) {
		this.singlePeriod = singlePeriod;
	}

	public int getCumPeriod() {
		return cumPeriod;
	}

	public void setCumPeriod(int cumPeriod) {
		this.cumPeriod = cumPeriod;
	}

	public int getSalePeriod() {
		return salePeriod;
	}

	public void setSalePeriod(int salePeriod) {
		this.salePeriod = salePeriod;
	}

	public int getDbPeriod() {
		return dbPeriod;
	}

	public void setDbPeriod(int dbPeriod) {
		this.dbPeriod = dbPeriod;
	}

	@Override
	public String toString() {
		return "ConsulConfig{" + "singlePeriod=" + singlePeriod + ", cumPeriod=" + cumPeriod + ", salePeriod=" + salePeriod + ", staCount=" + staCount + ", access_delay=" + access_delay + ", write_delay=" + write_delay + ", scene_fast_recover=" + scene_fast_recover + ", scene_fast_time=" + scene_fast_time + ", friend_count=" + friend_count + ", friend_ready_count=" + friend_ready_count + ", friend_send=" + friend_send + ", friend_recv=" + friend_recv + ", user_chagnename_gem=" + user_chagnename_gem + ", user_spirit_max=" + user_spirit_max + ", sp_recover_interval=" + sp_recover_interval + ", treasure_dig_limit=" + treasure_dig_limit + ", mine_open=" + mine_open + ", mine_energy_max=" + mine_energy_max + ", mine_energy_recover=" + mine_energy_recover + ", mine_energy_recover_count=" + mine_energy_recover_count + ", mine_consume=" + mine_consume + ", mine_view=" + mine_view + ", mine_buyenergy_cost=" + Arrays.toString(mine_buyenergy_cost) + ", mine_buyenergy_count=" + mine_buyenergy_count + '}';
	}
}
