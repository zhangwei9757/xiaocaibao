package com.tumei.game.protos.store;

import com.tumei.common.DaoService;
import com.tumei.game.GameUser;
import com.tumei.model.StoreBean;
import com.tumei.websocket.SessionUser;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.tumei.common.utils.Defs.刷新令;
import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestFlushHeroStore extends BaseProtocol {
	public int seq;

	class ReturnFlushHeroStore extends BaseProtocol {
		public int seq;
		/**
		 * 商店购买
		 */
		public List<StoreStruct> normal;

		public String result = "";
		/**
		 * 下次刷新的时间
		 */
//		public long flushtime;
		/**
		 * 还可以刷新的次数
		 */
		public int flushcount;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnFlushHeroStore rci = new ReturnFlushHeroStore();
		rci.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		VipConf vc = Readonly.getInstance().findVip(rb.getVip());
		StoreBean sb = user.getDao().findStore(user.getUid());

		if (vc.herostore - sb.getFlushHeroCount() < 1) {
			rci.result = "今日的刷新次数已经达到上限";
			user.send(rci);
			return;
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		if (!pb.contains(刷新令, 1)) {
			if (!pb.contains(钻石, 20)) {
				rci.result = "没有足够的刷新令和钻石";
				user.send(rci);
				return;
			}
			user.payItem(钻石, 20, "刷新");
		} else {
			user.payItem(刷新令, 1, "刷新");
		}

		// 已经购买的记录
		rci.normal = sb.flushHeroStore(rb.getLevel(), true);
		sb.setFlushHeroCount(sb.getFlushHeroCount() + 1);
		rci.flushcount = vc.herostore - sb.getFlushHeroCount();

		DaoService.getInstance().findSta(user.getUid()).incHeroStoreFlush();
		user.send(rci);
	}
}
