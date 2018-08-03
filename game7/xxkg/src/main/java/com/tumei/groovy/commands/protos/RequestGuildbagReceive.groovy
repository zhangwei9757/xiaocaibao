package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.game.GameUser
import com.tumei.model.GuildbagBean
import com.tumei.model.beans.guildbag.GuildbagStruct
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser
import org.springframework.stereotype.Component

import java.util.stream.Collectors

/**
 * Created by Administrator on 2018-7-31
 * 公会红包
 *
 */
@Component
class RequestGuildbagReceive extends BaseProtocol {
    public int seq
    /**
     * 红包key
     * **/
    public int key

    class Return extends BaseProtocol {
        public int seq
        // 返回结果
        public String result = ""
        /**
         * 未开启的所有红包
         **/
        private List<GuildbagStruct> waitOpen
        /**
         * 开启后可领取的所有红包
         **/
        private List<GuildbagStruct> waitReceive
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        // 红包key存在的范围
        List<Integer> keys = Readonly.instance.getGuildbagConfs().stream().map { s -> s.key }.collect(Collectors.toList())

        if (!keys.contains(key)) {
            r.result = "参数不合法"
            user.send(r)
            return
        }

        GuildbagBean gbb = DaoGame.instance.findGuildbagBean(user.getUid())
        if (gbb.waitReceive.size() > 0) {
            if (gbb.receive(user, key)) {
                r.waitOpen = gbb.waitOpen
                r.waitReceive = gbb.waitReceive
            } else {
                r.result = "领取红包失败"
            }
        } else {
            r.result = "无可领取的红包"
        }

        user.send(r)
    }
}

