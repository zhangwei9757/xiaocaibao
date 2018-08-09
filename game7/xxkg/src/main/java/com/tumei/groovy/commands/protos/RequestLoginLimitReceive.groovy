package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.game.GameUser
import com.tumei.model.LimitReceiveBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser
/**
 * Created by Administrator on 2018-7-24
 *
 * 12-13
 * 18-19
 * 21-22
 * 均领取一次奖励
 *
 */
class RequestLoginLimitReceive extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        // 返回结果
        public String result = ""

        public int[] counts = new int[3]

    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        LimitReceiveBean limitReceiveBean  = DaoGame.instance.findLimitReceive(user.getUid())

        // 刷新时间
        limitReceiveBean.flush()
        r.counts = Arrays.copyOf(limitReceiveBean.counts, 3)

        user.send(r)
    }
}

