package com.tumei.game.protos.fireraid;

import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.FireRaidBean;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 *
 * 燃烧远征信息读取
 *
 */
@Component
public class RequestFireRaidReset extends BaseProtocol {
    public int seq;

    class ReturnFireRaidReset extends BaseProtocol {
		public int seq;
		public int gem;
		public String result = "";
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnFireRaidReset rl = new ReturnFireRaidReset();
		rl.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		FireRaidBean frb = user.getDao().findFireRaid(user.getUid());

		int left = frb.getResetCount();
		VipConf vc = Readonly.getInstance().findVip(rb.getVip());

		if (vc.edre <= left) {
			rl.result = "今日重置次数已经达到上限";
		} else {
			rl.gem = left * 50;

			PackBean pb = user.getDao().findPack(user.getUid());
			if (rl.gem > 0 && !pb.contains(钻石, rl.gem)) {
				rl.result = ErrCode.钻石不足.name();
			} else {
				if (rl.gem > 0) {
					user.payItem(钻石, rl.gem, "重置消耗");
				}
				if (!frb.reset()) {
					rl.result = "重置次数不足";
				}
			}
		}

        user.send(rl);
    }
}
