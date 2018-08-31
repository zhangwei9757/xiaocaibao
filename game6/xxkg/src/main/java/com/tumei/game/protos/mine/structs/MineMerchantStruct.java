package com.tumei.game.protos.mine.structs;

import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.modelconf.MineMerchantConf;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Leon on 2017/7/25 0025.
 */
public class MineMerchantStruct {
	public int pos;
	/**
	 * 商品配置表中的key,用于客户端查询价格
	 */
	public int key;

	public MineMerchantStruct() {}

	public MineMerchantStruct(int pos, int level) {
		this.pos = pos;
		List<MineMerchantConf> mmcs = Readonly.getInstance().getMineMerchantConfs().stream().filter(mmc -> mmc.rflevel <= level).collect(Collectors.toList());
		MineMerchantConf mmc = RandomUtil.getInList(mmcs);
		this.key = mmc.key;
	}

}
