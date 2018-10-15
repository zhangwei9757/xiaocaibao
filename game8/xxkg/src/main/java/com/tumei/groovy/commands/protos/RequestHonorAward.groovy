package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.game.GameUser
import com.tumei.model.ActivityBean
import com.tumei.model.beans.AwardBean
import com.tumei.modelconf.GloryrewardConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 用荣誉币兑换奖励
 */
class RequestHonorAward extends BaseProtocol {
    public int seq

    /**
     * key 对应的配置表中荣誉奖励的key
     */
    public int key;

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        public List<AwardBean> awards = new ArrayList<>()
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        ActivityBean ab = DaoGame.getInstance().findActivity(user.getUid());
        ab.flushHotArenas()

        if (ab.getHonorAwards().stream().anyMatch({h -> h == key})) {
            rci.result = "本周已经领取"
        } else {
            GloryrewardConf gc = Readonly.getInstance().findGloryrewardConf(key - 1)
            if (gc == null) {
                rci.result = "奖励不存在"
            } else {
                if (ab.getHonor() < gc.cost) {
                    rci.result = "荣誉不足"
                } else {
                    rci.awards.addAll(user.addItems(gc.rewards, "荣誉奖励"))
                    ab.getHonorAwards().add(key);
                }
            }
        }

        user.send(rci)
    }
}

