package com.tumei;

import com.ecwid.consul.v1.ConsulClient;
import com.tumei.common.utils.JsonUtil;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

/**
 * Created by Leon on 2017/9/5 0005.
 */
@Data
public class ConsulConfig {
	private static final Log log = LogFactory.getLog(ConsulConfig.class);

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
	private int mine_energy_recover = 180;

	/**
	 * 每次回复能量的点数
	 */
	private int mine_energy_recover_count = 1;

	/**
	 * 每一步行动消耗的能量
	 */
	private int mine_consume = 1;

	/**
	 * 地图视野半径
	 */
	private int mine_view = 16;

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
	 * @param key
	 * @return
	 */
	public static ConsulConfig reload(ConsulClient cc, String key, String token) {
		try {
			String data = cc.getKVValue(key, token).getValue().getDecodedValue();
			return JsonUtil.Unmarshal(data, ConsulConfig.class);
		} catch (Exception e) {
			log.info("+++++ consul server中没有配置,使用默认数据.");
			return new ConsulConfig();
		}
	}

	@Override
	public String toString() {
		return "ConsulConfig{" + "singlePeriod=" + singlePeriod + ", cumPeriod=" + cumPeriod + ", salePeriod=" + salePeriod + ", dbPeriod=" + dbPeriod + ", staCount=" + staCount + ", scene_fast_recover=" + scene_fast_recover + ", scene_fast_time=" + scene_fast_time + ", friend_count=" + friend_count + ", friend_ready_count=" + friend_ready_count + ", friend_send=" + friend_send + ", friend_recv=" + friend_recv + ", user_chagnename_gem=" + user_chagnename_gem + ", user_spirit_max=" + user_spirit_max + ", sp_recover_interval=" + sp_recover_interval + ", treasure_dig_limit=" + treasure_dig_limit + ", mine_open=" + mine_open + ", mine_energy_max=" + mine_energy_max + ", mine_energy_recover=" + mine_energy_recover + ", mine_energy_recover_count=" + mine_energy_recover_count + ", mine_consume=" + mine_consume + ", mine_view=" + mine_view + ", mine_buyenergy_cost=" + Arrays.toString(mine_buyenergy_cost) + ", mine_buyenergy_count=" + mine_buyenergy_count + '}';
	}
}
