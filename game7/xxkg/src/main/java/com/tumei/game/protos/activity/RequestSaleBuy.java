package com.tumei.game.protos.activity;

import com.tumei.game.services.LocalService;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.ExchangeConf;
import com.tumei.common.Readonly;
import com.tumei.modelconf.SaleConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 * 购买 半价限购
 */
@Component
public class RequestSaleBuy extends BaseProtocol {
    public int seq;
	/**
	 * 0:半价限购
	 * 1:折扣兑换
	 */
	public int mode;
	/**
	 * 序号 [0,...
	 */
	public int index;
	/**
	 * 折扣兑换是可以选择目标的 这个范围为[0, ...]
	 */
	public int selection;
    class ReturnSaleBuy extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnSaleBuy rl = new ReturnSaleBuy();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flushDc();

		Readonly r = Readonly.getInstance();
		if (mode == 0) {
			SaleConf sc = r.getSaleConfs().get(LocalService.getInstance().getDcBeginIdx() + index);
			int count = ab.getDcStatus().get(index);
			if (count >= sc.limit) {
				rl.result = "已经达到购买上限";
			} else {
				PackBean pb = user.getDao().findPack(user.getUid());
				if (!pb.contains(钻石, sc.price)) {
					rl.result = "钻石不足";
				} else {
					user.payItem(钻石, sc.price, "半价");
					ab.getDcStatus().set(index, count + 1);
					rl.awards.addAll(user.addItems(sc.goods, "半价兑换"));
				}
			}
		} else {
			ExchangeConf ec = r.getExchangeConfs().get(LocalService.getInstance().getEcBeginIdx() + index);
			int count = ab.getEcStatus().get(index);
			if (count >= ec.limit) {
				rl.result = "已经达到购买上限";
			} else {
				PackBean pb = user.getDao().findPack(user.getUid());
				if (!pb.contains(钻石, ec.price)) {
					rl.result = "钻石不足";
				} else {
					user.payItem(钻石, ec.price, "半价");
					ab.getEcStatus().set(index, count + 1);
					int[] goods;
					if (ec.goods.length > 1) {
						goods = ec.goods[selection];
					} else {
						goods = ec.goods[0];
					}
					rl.awards.addAll(user.addItems(goods, "折扣"));
				}
			}
		}

        user.send(rl);
    }
}
