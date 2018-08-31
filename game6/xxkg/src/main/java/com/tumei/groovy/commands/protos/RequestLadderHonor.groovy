package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.RemoteService
import com.tumei.dto.arena.LadderHonorDto
import com.tumei.game.GameUser
import com.tumei.model.ActivityBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

import java.time.LocalDateTime

/**
 * Created by Administrator on 2017/3/13 0013.
 * 获取荣誉
 */
class RequestLadderHonor extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        // 收获的honor
        public int honor

        // 下次可收获时间, 如果为0没有意义
        public long next
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        LocalDateTime ldt = LocalDateTime.now()
        if (ldt.dayOfWeek.value == 1 && ldt.hour < 10) {
            rci.result = "天梯怪物正在反攻天梯榜单,当前不可进入."
            user.send(rci)
            return
        }

        LadderHonorDto dto = RemoteService.getInstance().arenaHonor(user.getUid())
        if (dto == null) {
            rci.result = "跨服竞技场维护中,请稍候再进入."
        } else {
            ActivityBean ab = DaoService.getInstance().findActivity(user.getUid())
            ab.flushHotArenas()

            if (dto.honor > 0) {
                ab.honor += dto.honor
            }
            rci.honor = ab.honor
            rci.next = dto.next
//            println("当前荣誉:" + rci.honor + " next:" + Date.from(
//                    Instant.ofEpochMilli(dto.next * 1000)
//            ))
        }

        user.send(rci)
    }
}

