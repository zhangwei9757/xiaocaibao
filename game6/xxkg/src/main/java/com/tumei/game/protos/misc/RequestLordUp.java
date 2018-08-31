package com.tumei.game.protos.misc;

import com.tumei.common.DaoService;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.websocket.SessionUser;
import com.tumei.modelconf.ChainConf;
import com.tumei.common.Readonly;
import com.tumei.common.utils.ErrCode;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.tumei.common.utils.Defs.战争印记;

/**
 * Created by leon on 2016/12/31.
 *
 * 关键词: 领主品质提升  战神像
 */
@Component
public class RequestLordUp extends BaseProtocol {
    public int seq;

    class ReturnLordUp extends BaseProtocol {
		public int seq;
		/**
		 * 英雄是否进阶，进阶后, 新的id会返回
		 * 默认是90010，每次进阶+10
		 */
		public int newid;
		public List<AwardBean> award = new ArrayList<>();
		public String result = "";
    }

    @Override
    public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;
		ReturnLordUp rl = new ReturnLordUp();
		rl.seq = seq;

		HerosBean hsb = user.getDao().findHeros(user.getUid());
		Optional<HeroBean> op = Arrays.stream(hsb.getHeros()).filter(hb -> hb.isLord()).findFirst();
		op.ifPresent((HeroBean hb) -> {
			int chain = hsb.getChain();
			int chainattr = hsb.getChainattr(); // 默认从0开始

			ChainConf cc = Readonly.getInstance().findChain(chain);

			PackBean pb = user.getDao().findPack(user.getUid());

			if (!pb.contains(战争印记, cc.cost[chainattr])) {
				rl.result = ErrCode.陨星水晶不足.name();
				return;
			}
			user.payItem(战争印记, cc.cost[chainattr], "战神像提升到(" + chain + ":" + (chainattr+1) + ")");

			if (++chainattr == 5) {
				int[] awd = cc.rewardadd[chainattr - 1];
				if (awd[0] != 0) {
					rl.award.addAll(user.addItem(awd[0], awd[1], false, "战神像"));
				}

				chainattr = 0;
				++chain;

				if (cc.quality > 0) {
					RoleBean rb = DaoService.getInstance().findRole(user.getUid());
					int newid = (cc.quality - 2) * 10;
					if (rb.getSex() == 0) {
						newid += 90010;
					} else {
						newid += 90110;
					}

					if (newid != hb.getId()) {
						hb.setId(newid);
						rl.newid = newid;

						rb.setIcon(newid);
						rb.setGrade(cc.quality);
					}
				}
			}


			hsb.setChain(chain);
			hsb.setChainattr(chainattr);
			hsb.updateBuffs();
		});

		user.send(rl);
	}
}
