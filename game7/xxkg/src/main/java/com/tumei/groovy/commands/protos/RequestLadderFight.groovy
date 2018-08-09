package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.dto.arena.LadderFightResult
import com.tumei.game.GameUser
import com.tumei.model.ActivityBean
import com.tumei.modelconf.VipConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

import java.time.LocalDateTime

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
class RequestLadderFight extends BaseProtocol {
    public int seq

    // 对方的userid. 如果小于10000就是npc
    public long pid

    // 分组 [0,5] 王者开始到青铜，备战不能打
    public int group

    // 每个分组的序号[0,...] 每组的人数不一样
    public int index

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        // 1是胜利，其他失败
        public int win

        // 战斗数据
        public String data = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq
        if (group < 0 || group > 5) {
            rci.result = "错误的参数"
            user.send(rci)
            return
        }

        LocalDateTime ldt = LocalDateTime.now()

        if (ldt.dayOfWeek.value == 1 && ldt.hour < 10) {
            rci.result = "天梯怪物正在反攻天梯榜单,当前不可进入."
            user.send(rci)
            return
        }

        ActivityBean ab = DaoGame.getInstance().findActivity(user.getUid())
        ab.flushHotArenas()

        VipConf vc = Readonly.getInstance().findVip(user.vip)
        if (vc != null) {
            if (ab.ladderCount >= vc.trtime) {
                rci.result = "今日天梯赛攻打次数达到上限"
                user.send(rci)
                return
            }
        }

        LadderFightResult rtn = RemoteService.getInstance().arenaLadderFight(user.getUid(), pid, group, index)
        if (rtn == null) {
            rci.result = "跨服竞技场维护中,请稍候再进入."
        } else {
            rci.result = rtn.reason

            rci.win = rtn.win ? 1 : 2
            rci.data = rtn.data

            ++ab.ladderCount;
        }

        user.send(rci)
    }
}

