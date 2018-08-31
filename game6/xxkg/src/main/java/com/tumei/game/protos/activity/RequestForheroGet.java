package com.tumei.game.protos.activity;

import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.ForheroConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.突破石;
import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 * 英雄福利
 *
 */
@Component
public class RequestForheroGet extends BaseProtocol {
    public int seq;
	// 0,等级福利
	// 1,登录福利
	public int mode;

	// 配置对应的key
	public int index;

    class ReturnForheroGet extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnForheroGet rl = new ReturnForheroGet();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		if (ab.getHeroFuli() == 0) {
			rl.result = "请先购买神将福利";
			user.send(rl);
			return;
		}

		if (mode == 0) {
			if (ab.getLevelFuli().getOrDefault(index, 0) != 1) {
				rl.result = "已经领取,无法领取";
				user.send(rl);
				return;
			}

			RoleBean rb = user.getDao().findRole(user.getUid());
			List<ForheroConf> fcs = Readonly.getInstance().getForheroConfs();
			ForheroConf fc = fcs.get(index - 1);
			if (fc != null) {
				if (rb.getLevel() < fc.level[0]) {
					rl.result = "未满足条件,无法领取";
					user.send(rl);
					return;
				}

				ab.getLevelFuli().put(index, 2);

				rl.awards.addAll(user.addItem(突破石, fc.level[1], false, "英雄福利"));
			}
		} else {
			if (ab.getLogoFuli().getOrDefault(index, 0) != 1) {
				rl.result = "已经领取，无法领取";
				user.send(rl);
				return;
			}

			List<ForheroConf> fcs = Readonly.getInstance().getForheroConfs();
			ForheroConf fc = fcs.get(index - 1);

			if (fc != null) {
				ab.getLogoFuli().put(index, 2);
				rl.awards.addAll(user.addItem(突破石, fc.day[1], false, "英雄福利"));
			}
		}

        user.send(rl);
	}
}
