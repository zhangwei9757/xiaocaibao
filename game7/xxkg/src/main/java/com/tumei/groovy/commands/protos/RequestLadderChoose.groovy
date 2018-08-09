package com.tumei.groovy.commands.protos

import com.tumei.common.RemoteService
import com.tumei.game.GameUser
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

import java.time.LocalDateTime

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
class RequestLadderChoose extends BaseProtocol {
    public int seq

    public int slot

    class Return extends BaseProtocol {
        public int seq

        public String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        if (slot < 0) {
            rci.result = "错误的参数"
            user.send(rci)
            return
        }

        LocalDateTime ldt = LocalDateTime.now()
        if (ldt.dayOfWeek == 0 && ldt.hour < 10) {
            rci.result = "天梯怪物正在反攻天梯榜单,当前不可进入."
            user.send(rci)
            return
        }

        int rtn = RemoteService.getInstance().arenaChooseSlot(user.getUid(), slot)
        if (rtn < 0) {
            rci.result = "分区暂未开放,请尝试其他分区"
        }

        user.send(rci)
    }
}

