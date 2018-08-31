package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.RemoteService
import com.tumei.common.utils.Defs
import com.tumei.dto.arena.ArenaInfo
import com.tumei.game.GameUser
import com.tumei.model.PackBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
class RequestHotArenaInfo extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        /**
         * 当前勇士币
         */
        public int yscoin

        public ArenaInfo info

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq
        if (user.level < Defs.跨服竞技等级) {
            rci.result = "领主等级不足"
            user.send(rci)
            return
        }

        user.submitArenaInfo()

        rci.info = RemoteService.getInstance().arenaGetInfo(user.getUid())
        if (rci.info == null) {
            rci.result = "跨服竞技场本赛区暂未开启."
            user.send(rci)
            return
        }

        PackBean pb = DaoService.getInstance().findPack(user.getUid());
        rci.yscoin = pb.getItemCount(Defs.勇士币);

        user.send(rci)
    }
}
