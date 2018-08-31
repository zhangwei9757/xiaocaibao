package com.tumei.game.protos.robtreasure;

import com.tumei.game.protos.structs.FightRoleStruct;
import com.tumei.game.services.RobService;
import com.tumei.model.HerosBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.ItemConf;
import com.tumei.websocket.SessionUser;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.RobStruct;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestRobInfo extends BaseProtocol {
	public int seq;
	/**
	 * 宝物碎片 id
	 */
	public int item;

	class ReturnRobInfo extends BaseProtocol {
		public int seq;
		public String result = "";
		// 同步一下活力值
		public int spirit;
		public List<RobStruct> robs = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnRobInfo rci = new ReturnRobInfo();
		rci.seq = seq;

		PackBean pb = user.getDao().findPack(user.getUid());

		/**
		 * 检查对应item的宝物是否存在至少一个碎片
		 */
		ItemConf ic = Readonly.getInstance().findItem(item);
		if (ic.quality != 2 && (item/10) != 2051) { // 蓝色品质的不需要至少一个碎片这个条件，但是排除蓝色品质中的 粗糙的宝物这系列
			int count = ic.quality + 1;
			boolean flag = false;
			int tid = (item - (item % 10));
			for (int i = 1; i <= count; ++i) {
				if (pb.contains(tid + i, 1)) {
					flag = true;
					break;
				}
			}

			if (!flag) {
				rci.result = "至少有一个对应的碎片才能进行抢夺！";
				user.send(rci);
				return;
			}
		}

		rci.spirit = pb.flushSpirit(0);

		// 根据随便获得一个不被保护的玩家id
		long uid = RobService.getInstance().findByItem(user.getUid(), item);
		if (uid != 0) {
//			user.warn("对于宝物碎片(" + item + ")，搜寻到玩家(" + uid + ").");
			RoleBean _rb = user.getDao().findRole(uid);
			RobStruct rs = new RobStruct(_rb.getId(), ic.quality, _rb);
			HerosBean hsb = user.getDao().findHeros(uid);

			for (HeroBean hb : hsb.getHeros()) {
				if (hb != null) {

					if (hb.isLord()) {
						rs.getHeros().add(new FightRoleStruct(hb.getId(), hsb.getSkin()));
					} else {
						rs.getHeros().add(new FightRoleStruct(hb.getId(), hb.getGift() >= 26 ? 1 : 0));
					}
				} else {
					rs.getHeros().add(new FightRoleStruct(0, 0));
				}
			}

			rci.robs.add(rs);
		}

		{
			RoleBean rb = user.getDao().findRole(user.getUid());
			/**
			 * 随机构建npc信息
			 */
			int c =	5 - rci.robs.size();
			for (int i = 0; i < c; ++i) {
				rci.robs.add(new RobStruct(0, ic.quality, rb));
			}
		}

		user.tmpRobs = rci.robs;
		user.tmpRobItem = item;

		user.send(rci);
	}
}
