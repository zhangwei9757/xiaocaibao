package com.tumei.game.protos.star;

import com.tumei.websocket.SessionUser;
import com.tumei.game.GameUser;
import com.tumei.model.PackBean;
import com.tumei.model.StarBean;
import com.tumei.model.beans.StarHeroFragsBean;
import com.tumei.common.Readonly;
import com.tumei.modelconf.StarConf;
import com.tumei.common.utils.ErrCode;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.活力;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 选择
 */
@Component
public class RequestStarSelect extends BaseProtocol {
	public int seq;

	/**
	 * 序号 [1,13], 根据返回的条目索引
	 */
	public int index;

	/**
	 * 0: 6小时
	 * 1: 8小时
	 * 2: 10小时
	 */
	public int mode;


	class ReturnStarSelect extends BaseProtocol {
		public int seq;
		public String result = "";
		public int sp;
		public List<StarHeroFragsBean> stars = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnStarSelect rsi = new ReturnStarSelect();
		rsi.seq = seq;

		StarBean sb = user.getDao().findStar(user.getUid());
		sb.updateStars(0);

		List<StarHeroFragsBean> sfbs = sb.getStars();
		if (sfbs.stream().filter((sfb) -> {
			return sfb.status != 0;
		}).count() >= 3) {
			rsi.result = "已经达到点将上限";
			user.send(rsi);
			return;
		}

		StarHeroFragsBean sfb = sfbs.get(index - 1);

		StarConf sc = Readonly.getInstance().findStars().get(index - 1);
		PackBean pb = user.getDao().findPack(user.getUid());
		int spirit = sc.cost[mode][1];
		if (!pb.contains(活力, spirit)) {
			rsi.result = ErrCode.活力不足.name();
			user.send(rsi);
			return;
		}

		if (!sfb.select(mode)) {
			rsi.result = ErrCode.未知参数.name();
			user.send(rsi);
			return;
		}

		user.payItem(活力, spirit, "活力兑换");
		rsi.stars = sb.getStars();
		rsi.sp = spirit;

		user.send(rsi);
	}

}
