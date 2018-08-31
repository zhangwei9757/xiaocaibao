package com.tumei.game.protos.activity;

import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.modelconf.GoldtreeConf;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.金币;
import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 *
 */
@Component
public class RequestGoldtree extends BaseProtocol {
    public int seq;

    class ReturnGoldtree extends BaseProtocol {
		public int seq;
		public String result = "";
		// 本次扣除钻石
		public int gem;
		public long coin;
		public int bonus;
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnGoldtree rl = new ReturnGoldtree();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		ab.flush();

		Readonly r = Readonly.getInstance();

		int used = ab.getGoldTree();
		VipConf vc = r.findVip(user.getVip());
		if (used >= vc.tree) {
			rl.result = "今日摇钱树次数已达上限";
			user.send(rl);
			return;
		}

		GoldtreeConf gc = r.findGoldtree(1);

		if (used > 0) {
			rl.gem = gc.cost[used - 1];
		}

		if (rl.gem > 0) {
			PackBean pb = user.getDao().findPack(user.getUid());
			if (!pb.contains(钻石, rl.gem)) {
				rl.result = ErrCode.钻石不足.name();
				user.send(rl);
				return;
			}
			user.payItem(钻石, rl.gem, "摇钱树");
		}

		RoleBean rb = user.getDao().findRole(user.getUid());
		gc = r.findGoldtree(rb.getLevel());
		++used;
		rl.coin = gc.gold1;
		if (used < 6) {
			rl.coin = gc.gold1;
		} else if (used < 11) {
			rl.coin = gc.gold2;
		} else if (used < 16) {
			rl.coin = gc.gold3;
		} else if (used < 21) {
			rl.coin = gc.gold4;
		} else if (used < 26) {
			rl.coin = gc.gold5;
		} else if (used < 31) {
			rl.coin = gc.gold6;
		} else if (used < 36) {
			rl.coin = gc.gold7;
		} else if (used < 41) {
			rl.coin = gc.gold8;
		} else if (used < 46) {
			rl.coin = gc.gold9;
		} else {
			rl.coin = gc.gold10;
		}

//		user.info("第" + used + "次抽奖, 获得金币:" + rl.coin);

		// 10%的机率暴击
		if ((RandomUtil.getBetween(1, 100)) <= 10) {
			rl.bonus = RandomUtil.getBetween(vc.treebonus[0], vc.treebonus[1]);
			rl.coin = (long)(rl.coin * (rl.bonus / 100f));
		}

		user.addItem(金币, rl.coin, false, "摇钱树");
		ab.setGoldTree(used);

        user.send(rl);
    }
}
