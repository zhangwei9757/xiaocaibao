package com.tumei.game.protos.store;

import com.tumei.websocket.SessionUser;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.RoleBean;
import com.tumei.model.StoreBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestHeroStore extends BaseProtocol {
	public int seq;

	class ReturnHeroStore extends BaseProtocol {
		public int seq;
		/**
		 * 商店购买
		 */
		public List<StoreStruct> normal;
		/**
		 * 下次刷新的时间
		 */
		public long flushtime;
		/**
		 * 还可以刷新的次数
		 */
		public int flushcount;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnHeroStore rci = new ReturnHeroStore();
		rci.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		StoreBean sb = user.getDao().findStore(user.getUid());

		// 已经购买的记录
		rci.normal = sb.flushHeroStore(rb.getLevel(), false);
		VipConf vc = Readonly.getInstance().findVip(rb.getVip());
		rci.flushtime = sb.getLastFlushHeroStore() + 7200;
		rci.flushcount = vc.herostore - sb.getFlushHeroCount();

		user.send(rci);
	}
}
