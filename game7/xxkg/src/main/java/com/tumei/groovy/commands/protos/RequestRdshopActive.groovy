package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.game.GameUser
import com.tumei.model.RdshopBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2018-7-27
 * 神秘商店事件激活
 *
 */
class RequestRdshopActive extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        // 返回结果
        public String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        RdshopBean rb = DaoGame.instance.findRdshopBean(user.getUid())
        long current = System.currentTimeMillis() / 1000

        if (rb.rs != null) {
            long diff = (current - rb.rs.begin)
            if (diff >= 3600) {
                rb.rs == null
                rb.rs = rb.flush(user)
            }
        } else {
            r.result = "当前无可激活事件"
        }

        user.send(r)
    }
}

