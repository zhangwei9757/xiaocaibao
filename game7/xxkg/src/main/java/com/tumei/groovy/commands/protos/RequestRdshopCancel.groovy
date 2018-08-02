package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.game.GameUser
import com.tumei.model.RdshopBean
import com.tumei.model.beans.rdshop.RdshopStruct
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser
/**
 * Created by Administrator on 2018-7-27
 * 神秘商店事件取消
 *
 */
class RequestRdshopCancel extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        // 返回结果
        public String result = ""
        // 生成的事件个数
        public int count
        // 返回新的事件
        public RdshopStruct rs
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        RdshopBean rb = DaoGame.instance.findRdshopBean(user.getUid())
        if (rb.rs != null) {
            r.rs = rb.cancel(user)
            r.count = rb.count
        } else {
            r.result = "当前无可取消的事件"
        }

        user.send(r)
    }
}

