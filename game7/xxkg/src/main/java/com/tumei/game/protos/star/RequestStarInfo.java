package com.tumei.game.protos.star;

import com.tumei.common.utils.Defs;
import com.tumei.game.GameUser;
import com.tumei.model.beans.StarHeroFragsBean;
import com.tumei.websocket.WebSocketUser;
import com.tumei.model.SceneBean;
import com.tumei.model.StarBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求占星台数据
 */
@Component
public class RequestStarInfo extends BaseProtocol {
	public int seq;

	class ReturnStarInfo extends BaseProtocol {
		public int seq;
		public int scene;
		public List<StarHeroFragsBean> stars = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser)session;

		ReturnStarInfo rsi = new ReturnStarInfo();
		rsi.seq = seq;
		if (user.getLevel() < Defs.占星台等级) {
			user.send(rsi);
			return;
		}

		SceneBean scene = user.getDao().findScene(user.getUid());

		StarBean sb = user.getDao().findStar(user.getUid());
		sb.updateStars(scene.getScene());

		rsi.scene = scene.getScene();
		rsi.stars = sb.getStars();

		user.send(rsi);
	}

}
