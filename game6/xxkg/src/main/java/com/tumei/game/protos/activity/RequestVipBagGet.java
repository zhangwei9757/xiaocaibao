package com.tumei.game.protos.activity;

import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 * 请求月卡信息
 *
 */
@Component
public class RequestVipBagGet extends BaseProtocol {
    public int seq;
	/**
	 * 0: 每日
	 * 1: 每周
	 * 2: 达到等级只能领取一次的礼包，需要购买
	 */
	public int mode;

	/**
	 * [0, ...]
	 */
	public int vip;

    class ReturnVipBagGet extends BaseProtocol {
		public int seq;

		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnVipBagGet rl = new ReturnVipBagGet();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flush();

		if (mode == 0) {
			VipConf vc = Readonly.getInstance().findVip(user.getVip());
			if (ab.getVipDailyBag() > 0) {
				rl.result = "今日已经领取VIP礼包";
				user.send(rl);
				return;
			}

			rl.awards.addAll(user.addItems(vc.dailybag, "VIP日礼包"));
			ab.setVipDailyBag(1);
		} else if (mode == 1) {
			if (vip > user.getVip()) {
				rl.result = "VIP等级不足";
				user.send(rl);
				return;
			}

			VipConf vc = Readonly.getInstance().findVip(vip);
			int week = ab.getVipWeekBags()[vip];
			if (week > 0) {
				rl.result = "本周已经领取VIP礼包";
				user.send(rl);
				return;
			}

			PackBean pb = user.getDao().findPack(user.getUid());
			if (!pb.contains(钻石, vc.weekbag[2])) {
				rl.result = ErrCode.钻石不足.name();
				user.send(rl);
				return;
			}

			user.payItem(钻石, vc.weekbag[2], "购买VIP周礼包");
			rl.awards.addAll(user.addItem(vc.weekbag[0], vc.weekbag[1], false, "VIP周礼包"));
			ab.getVipWeekBags()[vip] = 1;

		} else if (mode == 2) {
			if (user.getVip() < vip) {
				rl.result = "VIP等级不够";
				user.send(rl);
				return;
			}

			int []bds = ab.getVipBundles();
			if (bds[vip] != 0) {
				rl.result = "已经购买该等级的VIP礼包";
				user.send(rl);
				return;
			}

			VipConf vc = Readonly.getInstance().findVip(vip);
			int price = vc.cost;
			PackBean pb = user.getDao().findPack(user.getUid());
			if (!pb.contains(钻石, price)) {
				rl.result = ErrCode.钻石不足.name();
				user.send(rl);
				return;
			}
			user.payItem(钻石, price, "购买vip礼包");

			rl.awards.addAll(user.addItems(vc.vipbag, "VIP礼包"));

			bds[vip] = 1;
		}

        user.send(rl);
    }
}
