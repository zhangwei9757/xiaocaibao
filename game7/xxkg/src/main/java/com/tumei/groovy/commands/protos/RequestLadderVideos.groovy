package com.tumei.groovy.commands.protos

import com.tumei.common.RemoteService
import com.tumei.dto.arena.LadderVideoDto
import com.tumei.game.GameUser
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

import java.time.LocalDateTime

/**
 * Created by Administrator on 2017/3/13 0013.
 * 获取荣誉
 */
class RequestLadderVideos extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        public List<LadderVideoDto> videos;
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


        rci.videos = RemoteService.getInstance().arenaVideos(user.getUid())
        if (rci.videos == null) {
            rci.result = "跨服竞技场维护中,请稍候再进入."
        }

        user.send(rci)
    }
}

