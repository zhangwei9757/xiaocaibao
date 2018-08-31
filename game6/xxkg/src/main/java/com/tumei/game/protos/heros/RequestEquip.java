package com.tumei.game.protos.heros;

import com.tumei.common.Readonly;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.EquipBean;
import com.tumei.modelconf.EquipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级英雄
 */
@Component
public class RequestEquip extends BaseProtocol {
	public int seq;
	/**
	 * rmb
	 * 0: 安装交换装备: src是背包中装备的eid, dst是部位[1,6]
	 * 1: 卸载装备: src是当前身上装备的部位[1,6]
	 */
	public int mode;
	/**
	 * 英雄位置 [1,6]表示战队
	 */
	public int index;
	public int src;
	public int dst;

	class ReturnEquip extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnEquip rci = new ReturnEquip();
		rci.seq = seq;

		long uid = user.getUid();
		HerosBean hsb = user.getDao().findHeros(uid);
		EquipBean[] eqs = hsb.getHeros()[index - 1].getEquips();

		if (mode == 0) { // 从背包安装，或者交换
			--dst;
			PackBean pb = user.getDao().findPack(uid);

			EquipBean tmp = pb.getEquips().getOrDefault(src, null);
			if (tmp == null) {
				rci.result = "您没有装备:[" + src + "].";
			} else {
				EquipConf ec = Readonly.getInstance().findEquip(tmp.getId());
				if (ec == null || ec.position != (dst + 1)) {
					rci.result = "装备不能安装在该部位";
				} else {
					EquipBean dt = eqs[dst];
					if (dt != null) {
						pb.getEquips().put(dt.getEid(), dt);
					}

					eqs[dst] = tmp;
					pb.getEquips().remove(src);
				}
			}
		} else if (mode == 1) {
			src -= 1;
			EquipBean eb = eqs[src];
			if (eb != null) {
				EquipBean tmp = eqs[src];
				PackBean pb = user.getDao().findPack(uid);
				pb.getEquips().put(tmp.getEid(), tmp);
			} else {
				user.info("eqs is nil");
			}
			eqs[src] = null;
		}

		user.send(rci);
	}
}
