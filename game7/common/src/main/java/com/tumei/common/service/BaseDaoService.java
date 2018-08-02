package com.tumei.common.service;

import com.tumei.common.utils.GenUtil;
import com.tumei.common.utils.RandomUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by Administrator on 2016/12/23 0023.
 * <p>
 * 数据库工具
 */
public class BaseDaoService {
	protected Log log = LogFactory.getLog(BaseDaoService.class);

	protected GenUtil gen;

	@Autowired
	protected GameCache gameCache;


	/**
	 * 账户数据库的Template操作句柄
	 */
	@Autowired
	protected MongoTemplate mongoTemplate;

	public void init() {
		gen = new GenUtil(1, 1, 0);
	}

	/***
	 * 根据玩家id创建一个属于玩家的随机串
	 * @param id
	 * @return
	 */
	protected String createRandomChars(Long id) {
		StringBuilder sb = new StringBuilder();
		String letters = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < 2; ++i) {
			char c = letters.charAt(RandomUtil.getRandom() % letters.length());
			sb.append(c);
		}
		sb.append((id / 10000));
		return sb.toString();
	}

	/**
	 * 根据名字获取对应的下一个数值,用于获取新用户可用的ID
	 *
	 * @return
	 */
	public long nextGenVal() {
		return gen.getId();
	}

}
