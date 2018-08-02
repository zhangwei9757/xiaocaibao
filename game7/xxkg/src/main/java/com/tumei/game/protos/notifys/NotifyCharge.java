package com.tumei.game.protos.notifys;

import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class NotifyCharge extends BaseProtocol {
	// 单据号
	public String tid;
	// 商品名
	public String product;
	// 单位分 (人民币)
	public int rmb;
	// 获得的钻石
	public int gem;
	public int vip;
	public int vipexp;

	// 充值成功后可能获得的奖励
	public List<AwardBean> awards = new ArrayList<>();
}
