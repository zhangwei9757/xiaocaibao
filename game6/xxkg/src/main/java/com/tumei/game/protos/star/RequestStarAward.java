package com.tumei.game.protos.star;

import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.StarBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.StarHeroFragsBean;
import com.tumei.modelconf.StarConf;
import com.tumei.websocket.SessionUser;
import com.tumei.common.Readonly;
import com.tumei.common.utils.ErrCode;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 占星台奖励收获
 */
@Component
public class RequestStarAward extends BaseProtocol {
	public int seq;

	/**
	 * 序号 [1,13], 根据返回的条目索引
	 */
	public int index;

	/**
	 * 0: 正常领取
	 * 1: 加速
	 */
	public int mode;

	class ReturnStarAward extends BaseProtocol {
		public int seq;
		public String result = "";
		public int gem;
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnStarAward rsi = new ReturnStarAward();
		rsi.seq = seq;

		StarBean sb = user.getDao().findStar(user.getUid());
		sb.updateStars(0);

		PackBean pb = user.getDao().findPack(user.getUid());

		StarHeroFragsBean sfb = sb.getStars().get(index - 1);
		if (sfb.status != 2) {
			if (sfb.status == 1 && mode == 1) {
				StarConf sc = Readonly.getInstance().findStars().get(sfb.index-1);
				int secs = (int)(sfb.elapse - System.currentTimeMillis() / 1000);

				int gem =  (secs / 3600 + (((secs % 3600) == 0) ? 0 : 1)) * sc.speedup;
				if (gem > 0) {
					rsi.gem = gem;
					if (!pb.contains(钻石, gem)) {
						rsi.result = ErrCode.钻石不足.name();
						user.send(rsi);
						return;
					} else {
						user.payItem(钻石, gem, "占星台");
					}
				}
			} else {
				rsi.result = "占星时间未到";
				user.send(rsi);
				return;
			}
		}

		sfb.status = 0;
		Arrays.stream(sfb.heros).forEach(hfb -> {
			rsi.awards.addAll(user.addItem(hfb.hero, hfb.count, false, "占星台"));
		});

		user.send(rsi);
	}

}
