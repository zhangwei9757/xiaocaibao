package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.RemoteService
import com.tumei.dto.arena.LadderInfoDto
import com.tumei.game.GameUser
import com.tumei.model.ActivityBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

import java.time.LocalDateTime

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
class RequestLadderInfo extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        public LadderInfoDto info

        // 今日已经攻打的次数
        public int used;

        public List<Integer> honors
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        LocalDateTime ldt = LocalDateTime.now()
        if (ldt.dayOfWeek.value == 1 && ldt.hour < 10) {
            rci.result = "天梯怪物正在反攻天梯榜单,当前不可进入."
            user.send(rci)
            return
        }

        rci.info = RemoteService.getInstance().arenaLadder(user.getUid())
        if (rci.info == null) {
            rci.result = "跨服竞技场维护中,请稍候再进入."
        } else {
            ActivityBean ab = DaoGame.getInstance().findActivity(user.getUid())
            ab.flushHotArenas()
            ab.honor += rci.info.honor
            rci.used = ab.getLadderCount();
            rci.info.honor = ab.honor
            rci.honors = ab.getHonorAwards()
        }

        user.send(rci)
    }
}

