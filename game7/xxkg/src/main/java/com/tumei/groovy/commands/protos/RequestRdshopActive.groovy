package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.game.GameUser
import com.tumei.model.RdshopBean
import com.tumei.model.beans.rdshop.RdshopStruct
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
        // 返回当前激活的事件
        public RdshopStruct rs
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        RdshopBean rb = DaoGame.instance.findRdshopBean(user.getUid())

        if (rb != null) {
            // 先刷新事件再激活
            rb.flush(user)

            if (rb.rs != null) {
                // 未激活，则激活
                if (rb.rs.complete == 0) {
                    long current = System.currentTimeMillis() / 1000
                    rb.rs.complete = current + 3600
                } else {
                    r.result = "当前事件已激活"
                }
            } else {
                r.result = "当前无可激活事件"
            }
        } else {
            r.result = "当前无可激活事件"
        }
        r.rs = rb.rs
        user.send(r)
    }
}

