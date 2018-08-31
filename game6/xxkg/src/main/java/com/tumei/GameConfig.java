package com.tumei;

import com.tumei.common.service.RouterService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Administrator on 2017/4/10 0010.
 */
@Component
public class GameConfig {
	private static GameConfig _instance;

	public static GameConfig getInstance() {
		return _instance;
	}

	private Log log = LogFactory.getLog(GameConfig.class);

	// 是否正式环境
	@Value("${gameserver.real}")
	private int real;

	/**
	 * 服务器id
	 */
	@Value("${gameserver.zone}")
	private int zone;

	private ConsulConfig config;

	public int getReal() {
		return real;
	}

	public void setReal(int real) {
		this.real = real;
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public int getSinglePeriod() { return config.getSinglePeriod(); }
	public int getCumPeriod() { return config.getCumPeriod(); }
	public int getSalePeriod() { return config.getSalePeriod(); }
	public int getDbPeriod() { return config.getDbPeriod(); }

	// 个人操作数据缓存数量
	public int getStaCount() {
		return config.getStaCount();
	}

	public int getAccess_delay() {
		return config.getAccess_delay();
	}

	public int getWrite_delay() {
		return config.getWrite_delay();
	}

	public int getSceneFastRecover() {
		return config.getScene_fast_recover();
	}

	public int getSceneFastTime() {
		return config.getScene_fast_time();
	}

	public int getFriend_count() {
		return config.getFriend_count();
	}

	public int getFriend_ready_count() {
		return config.getFriend_ready_count();
	}

	public int getFriend_send() {
		return config.getFriend_send();
	}

	public int getFriend_recv() {
		return config.getFriend_recv();
	}

	public boolean getMineOpen() {
		return config.getMine_open();
	}

	public int getMine_energy_recover() {
		return config.getMine_energy_recover();
	}

	public int getMine_energy_recover_count() {
		return config.getMine_energy_recover_count();
	}

	public int getMine_consume() {
		return config.getMine_consume();
	}

	public int getMine_energy_max() {
		return config.getMine_energy_max();
	}

	public int getMine_view() {
		return config.getMine_view();
	}

	public int[] getMine_buyenergy_cost() {
		return config.getMine_buyenergy_cost();
	}

	public int getMine_buyenergy_count() {
		return config.getMine_buyenergy_count();
	}

	public int getTreasure_dig_limit() {
		return config.getTreasure_dig_limit();
	}

	public int getUser_spirit_max() {
		return config.getUser_spirit_max();
	}

	public int getUser_chagnename_gem() {
		return config.getUser_chagnename_gem();
	}

	public int getSp_recover_interval() {
		return config.getSp_recover_interval();
	}

	@Override
	public String toString() {
		return "GameConfig{" + "zone=" + zone + ", config=" + config.toString() + '}';
	}

	@Autowired
	private RouterService rs;

	@PostConstruct
	void init() {
		_instance = this;
		config = ConsulConfig.reload(rs, "game");
	}

	public void reload() {
		ConsulConfig _config = ConsulConfig.reload(rs, "game");
		config = _config;
		log.warn("+++++ 重新加载Consul Configs +++++");
	}
}
