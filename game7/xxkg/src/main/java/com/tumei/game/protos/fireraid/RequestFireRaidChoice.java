package com.tumei.game.protos.fireraid;

import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.ChoiceStruct;
import com.tumei.model.FireRaidBean;
import com.tumei.model.RoleBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 *
 * 燃烧远征信息读取
 *
 */
@Component
public class RequestFireRaidChoice extends BaseProtocol {
    public int seq;

	/**
	 * 选择加成 [0,2]
	 */
	public int index;

    class ReturnFireRaidChoice extends BaseProtocol {
		public int seq;
		public String result = "";
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnFireRaidChoice rl = new ReturnFireRaidChoice();
		rl.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		FireRaidBean frb = user.getDao().findFireRaid(user.getUid());
		frb.flush();

		List<ChoiceStruct> cs = frb.getChoise();
		if (cs == null || cs.size() != 3) {
			rl.result = "没有可选的增加光环";
			user.send(rl);
			return;
		}


		int left = frb.getLeftStars();
		int need = 9;
		switch (index) {
			case 0:
				need = 3;
				break;
			case 1:
				need = 6;
				break;
		}
		if (left >= need) {
			left -= need;
			frb.setLeftStars(left);
			HashMap<Integer, Integer> buffs = frb.getBuffs();
			ChoiceStruct c = cs.get(index);
			buffs.put(c.key, buffs.getOrDefault(c.key, 0) + c.value);
			cs.clear();
		} else {
			rl.result = "没有足够的星数";
		}

        user.send(rl);
    }
}
