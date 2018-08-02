package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.game.GameUser
import com.tumei.model.RdshopBean
import com.tumei.model.beans.rdshop.RdshopStruct
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2018-7-27
 * 进入神秘商店界面
 *
 */
class RequestRdshop extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        // 返回结果
        public String result = ""
        // 生成的事件个数
        public int count
        // 返回事件
        public RdshopStruct rs
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        RdshopBean rb = DaoGame.instance.findRdshopBean(user.getUid())
        r.rs = rb.flush(user)

        if (r.rs == null ) {
            r.result = "当前无可参加的事件"
        }

        user.send(r)
    }
}

