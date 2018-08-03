package com.tumei.game.protos.daily;

import com.tumei.game.GameUser;
import com.tumei.model.DailySceneBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestDailyScene extends BaseProtocol {
    public int seq;

    class ReturnDailyScene extends BaseProtocol {
        public int seq;
        /**
         * 已经挑战过的副本
         */
        public List<Integer> killed = new ArrayList<>();
        /**
         * 当前已经挑战过的类型
         *
         * key: [1,6]
         * value: challenge count
         *
         */
        public HashMap<Integer, Integer> scenes = new HashMap<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session;
        ReturnDailyScene rl = new ReturnDailyScene();
        rl.seq = seq;

        DailySceneBean dsb = user.getDao().findDailyScene(user.getUid());
        dsb.flush();
        dsb.getScenes().keySet().forEach(id -> rl.killed.add(id));
        dsb.getScenes().entrySet().forEach(entry -> {
            if (rl.killed.contains(entry.getKey())) {
                rl.scenes.put(entry.getKey(), entry.getValue());
            }
        });
        user.send(rl);
    }
}
