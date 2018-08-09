package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.game.GameUser
import com.tumei.model.WarBean
import com.tumei.model.beans.war.WarTask
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 取消任务，只是简单的把任务还原到初始状态
 *
 */
class RequestWarCancel extends BaseProtocol {
    public int seq
    // 任务的递增id
    public int tid

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        /**
         *
         * 如果取消的任务是一个限时任务，但是取消后，当前时间已经大于expire了，则这个任务直接被删除，实际客户端是可以知道这个逻辑的，
         * 服务器为了简化客户端，返回del如果大于0，表示该任务的取消即可删除。
         *
         */
        public int del
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        WarBean wb = DaoGame.instance.findWar(user.uid)
        wb.flush(user.level)

        WarTask wt = wb.tasks.getOrDefault(tid, null)
        if (wt == null) {
            rci.result = "任务不存在"
            user.send(rci)
            return
        }

        if (wt.complete <= 0) {
            rci.result = "任务还没有开始"
            user.send(rci)
            return
        }

        wt.complete = 0
        long now = System.currentTimeMillis() / 1000
        if (wt.expire > 0 && now >= wt.expire) {
            wb.tasks.remove(tid)
            rci.del = 1
        }

        user.send(rci)
    }
}

