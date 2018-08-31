package com.tumei.game.protos.scene;

import com.tumei.game.GameUser;
import com.tumei.model.SceneBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestSceneInfo extends BaseProtocol {
    public int seq;

    class ReturnSceneInfo extends BaseProtocol {
		public int seq;
		/**
		 * 当前挂机的场景
		 */
		public int scene;
		/**
		 * 当前的能量槽
		 */
		public int energy;

		/**
		 * 加速次数
		 */
		public int speedCount;
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnSceneInfo rl = new ReturnSceneInfo();
		rl.seq = seq;

		SceneBean sb = user.getDao().findScene(user.getUid());
		sb.flush();

		rl.scene = sb.getScene();
		rl.energy = sb.getEnergy();
		rl.speedCount = sb.getSpeedCount();

        user.send(rl);
    }
}
